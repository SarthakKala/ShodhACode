package com.shodh.backend.service;

import com.shodh.backend.model.*;
import com.shodh.backend.repository.SubmissionRepository;
import com.shodh.backend.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class JudgeService {
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;

    public void judgeSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Update status to RUNNING
        submission.setStatus(SubmissionStatus.RUNNING);
        submissionRepository.save(submission);

        try {
            // Get all test cases for the problem
            List<TestCase> testCases = testCaseRepository.findByProblemId(submission.getProblem().getId());
            
            if (testCases.isEmpty()) {
                // If no test cases, mark as accepted (for testing purposes)
                submission.setStatus(SubmissionStatus.ACCEPTED);
                submission.setExecutionTime(0L);
                submission.setMemoryUsed(0L);
                submissionRepository.save(submission);
                return;
            }

            boolean allTestsPassed = true;
            long maxExecutionTime = 0L;
            long maxMemoryUsed = 0L;

            // Execute code using Docker containers
            for (TestCase testCase : testCases) {
                ExecutionResult result = executeCode(
                    submission.getCode(),
                    submission.getLanguage(),
                    testCase.getInput(),
                    submission.getProblem().getTimeLimit(),
                    submission.getProblem().getMemoryLimit(),
                    submission.getProblem()
                );

                if (result.timedOut) {
                    submission.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
                    allTestsPassed = false;
                    break;
                }

                if (result.error != null) {
                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setError(result.error);
                    allTestsPassed = false;
                    break;
                }

                // Compare output with expected output
                if (!compareOutput(result.output, testCase.getExpectedOutput())) {
                    submission.setStatus(SubmissionStatus.WRONG_ANSWER);
                    submission.setOutput(result.output);
                    allTestsPassed = false;
                    break;
                }

                maxExecutionTime = Math.max(maxExecutionTime, result.executionTime);
                maxMemoryUsed = Math.max(maxMemoryUsed, result.memoryUsed);
            }

            if (allTestsPassed) {
                submission.setStatus(SubmissionStatus.ACCEPTED);
                submission.setExecutionTime(maxExecutionTime);
                submission.setMemoryUsed(maxMemoryUsed);
            }

        } catch (Exception e) {
            log.error("Error judging submission {}: {}", submissionId, e.getMessage());
            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setError(e.getMessage());
        }

        submissionRepository.save(submission);
    }

    private ExecutionResult executeCode(String code, String language, String input, int timeLimit, int memoryLimit, Problem problem) {
        // Normalize language values
        String lang = language == null ? "" : language.trim().toLowerCase();
        
        Path tempDir = null;
        String containerName = "judge_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            // Create temporary directory for code files
            tempDir = Files.createTempDirectory("judge");
            
            // Build executable code based on language
            String fileName;
            String compileCmd = null;
            String runCmd;
            
            if ("java".equals(lang)) {
                String className = "Solution";
                fileName = className + ".java";
                String fullCode = buildJavaProgramFromFunction(problem, className, code);
                Files.write(tempDir.resolve(fileName), fullCode.getBytes());
                compileCmd = "javac " + fileName;
                runCmd = "java " + className;
            } else if ("python".equals(lang)) {
                fileName = "solution.py";
                String fullCode = buildPythonProgramFromFunction(problem, code);
                Files.write(tempDir.resolve(fileName), fullCode.getBytes());
                runCmd = "python3 " + fileName;
            } else if ("cpp".equals(lang)) {
                fileName = "solution.cpp";
                String fullCode = buildCppProgramFromFunction(problem, code);
                Files.write(tempDir.resolve(fileName), fullCode.getBytes());
                compileCmd = "g++ -o solution " + fileName;
                runCmd = "./solution";
            } else if ("javascript".equals(lang)) {
                fileName = "solution.js";
                String fullCode = buildJavaScriptProgramFromFunction(problem, code);
                Files.write(tempDir.resolve(fileName), fullCode.getBytes());
                runCmd = "node " + fileName;
            } else {
                return new ExecutionResult(null, "Unsupported language: " + lang, 0L, 0L, false);
            }
            
            // Compile if needed (Java, C++)
            if (compileCmd != null) {
                ExecutionResult compileResult = runDockerCommand(
                    containerName + "_compile",
                    tempDir,
                    compileCmd,
                    null,
                    5000, // 5 second compile timeout
                    memoryLimit
                );
                
                if (compileResult.error != null) {
                    return new ExecutionResult(null, "Compilation Error: " + compileResult.error, 0L, 0L, false);
                }
            }
            
            // Execute the code
            long startTime = System.currentTimeMillis();
            ExecutionResult result = runDockerCommand(
                containerName,
                tempDir,
                runCmd,
                input,
                timeLimit * 1000L, // convert to milliseconds
                memoryLimit
            );
            
            result.executionTime = System.currentTimeMillis() - startTime;
            return result;
            
        } catch (Exception e) {
            log.error("Error executing code: {}", e.getMessage());
            return new ExecutionResult(null, e.getMessage(), 0L, 0L, false);
        } finally {
            // Cleanup temporary files
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                        .sorted((a, b) -> -a.compareTo(b)) // Delete files before directories
                        .map(Path::toFile)
                        .forEach(File::delete);
                } catch (IOException e) {
                    log.error("Error cleaning up temp files: {}", e.getMessage());
                }
            }
        }
    }
    
    private ExecutionResult runDockerCommand(String containerName, Path workDir, String command, String input, long timeoutMs, int memoryLimitMb) {
        try {
            // Build docker run command with resource limits
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "run",
                "--name", containerName,
                "--rm", // Auto-remove container after execution
                "--network", "none", // Disable network access for security
                "--memory", memoryLimitMb + "m", // Memory limit
                "--cpus", "1", // CPU limit
                "--user", "coderunner", // Run as non-root user
                "-v", workDir.toAbsolutePath() + ":/workspace", // Mount code directory
                "-w", "/workspace",
                "shodh-judge:latest", // Our custom judge image
                "sh", "-c", command
            );
            
            pb.redirectErrorStream(false);
            Process process = pb.start();
            
            // Write input to stdin if provided
            if (input != null && !input.isEmpty()) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.print(input);
                    writer.flush();
                }
            }
            
            // Wait for execution with timeout
            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            
            if (!finished) {
                // Kill container if timeout
                process.destroyForcibly();
                killContainer(containerName);
                return new ExecutionResult(null, null, timeoutMs, 0L, true);
            }
            
            // Read output and error
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            
            return new ExecutionResult(
                output,
                error.isEmpty() ? null : error,
                timeoutMs,
                0L, // Memory usage tracking would require additional Docker API calls
                false
            );
            
        } catch (Exception e) {
            log.error("Docker execution error: {}", e.getMessage());
            killContainer(containerName);
            return new ExecutionResult(null, "Execution failed: " + e.getMessage(), 0L, 0L, false);
        }
    }
    
    private void killContainer(String containerName) {
        try {
            new ProcessBuilder("docker", "kill", containerName)
                .start()
                .waitFor(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to kill container {}: {}", containerName, e.getMessage());
        }
    }

    private String buildJavaProgramFromFunction(Problem problem, String className, String userCode) {
        String title = problem.getTitle() == null ? "" : problem.getTitle().toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append("import java.io.*;\nimport java.util.*;\n\n");
        sb.append("public class ").append(className).append(" {\n");
        // Insert user function as-is
        sb.append(userCode).append("\n\n");
        sb.append("    public static void main(String[] args) throws Exception {\n");
        sb.append("        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n");
        if (title.contains("two sum")) {
            sb.append("        String[] p1 = br.readLine().trim().split(\"\\\\s+\");\n");
            sb.append("        int n = Integer.parseInt(p1[0]); int target = Integer.parseInt(p1[1]);\n");
            sb.append("        String[] arrS = br.readLine().trim().split(\"\\\\s+\"); int[] nums = new int[n];\n");
            sb.append("        for(int i=0;i<n;i++) nums[i]=Integer.parseInt(arrS[i]);\n");
            sb.append("        int[] res = new ").append(className).append("().twoSum(nums, target);\n");
            sb.append("        System.out.println(res[0] + \" \" + res[1]);\n");
        } else if (title.contains("palindrome")) {
            sb.append("        String line = br.readLine().trim(); int x = Integer.parseInt(line);\n");
            sb.append("        boolean ans = new ").append(className).append("().isPalindrome(x);\n");
            sb.append("        System.out.println(ans);\n");
        } else if (title.contains("fizzbuzz") || title.contains("fizz buzz")) {
            sb.append("        int n = Integer.parseInt(br.readLine().trim());\n");
            sb.append("        List<String> out = new ").append(className).append("().fizzBuzz(n);\n");
            sb.append("        for(String s: out) System.out.println(s);\n");
        } else {
            // Fallback: just print input
            sb.append("        System.out.print(br.readLine());\n");
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    private String buildPythonProgramFromFunction(Problem problem, String userCode) {
        String title = problem.getTitle() == null ? "" : problem.getTitle().toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append(userCode).append("\n\n");
        if (title.contains("two sum")) {
            sb.append("if __name__ == '__main__':\n");
            sb.append("    p1 = input().split()\n");
            sb.append("    n, target = int(p1[0]), int(p1[1])\n");
            sb.append("    nums = list(map(int, input().split()))\n");
            sb.append("    result = two_sum(nums, target)\n");
            sb.append("    print(result[0], result[1])\n");
        } else if (title.contains("palindrome")) {
            sb.append("if __name__ == '__main__':\n");
            sb.append("    x = int(input())\n");
            sb.append("    print('true' if is_palindrome(x) else 'false')\n");
        } else if (title.contains("fizzbuzz")) {
            sb.append("if __name__ == '__main__':\n");
            sb.append("    n = int(input())\n");
            sb.append("    for s in fizz_buzz(n): print(s)\n");
        }
        return sb.toString();
    }
    
    private String buildCppProgramFromFunction(Problem problem, String userCode) {
        String title = problem.getTitle() == null ? "" : problem.getTitle().toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append("#include <bits/stdc++.h>\nusing namespace std;\n\n");
        sb.append(userCode).append("\n\n");
        sb.append("int main(){\n    ios::sync_with_stdio(false); cin.tie(nullptr);\n");
        if (title.contains("two sum")) {
            sb.append("    int n,target; cin>>n>>target; vector<int> nums(n);\n");
            sb.append("    for(int&x:nums)cin>>x;\n");
            sb.append("    vector<int> res=twoSum(nums,target);\n");
            sb.append("    cout<<res[0]<<\" \"<<res[1]<<endl;\n");
        } else if (title.contains("palindrome")) {
            sb.append("    int x; cin>>x;\n");
            sb.append("    cout<<(isPalindrome(x)?\"true\":\"false\")<<endl;\n");
        } else if (title.contains("fizzbuzz")) {
            sb.append("    int n; cin>>n;\n");
            sb.append("    for(auto &s:fizzBuzz(n))cout<<s<<endl;\n");
        }
        sb.append("    return 0;\n}\n");
        return sb.toString();
    }
    
    private String buildJavaScriptProgramFromFunction(Problem problem, String userCode) {
        String title = problem.getTitle() == null ? "" : problem.getTitle().toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append(userCode).append("\n\n");
        if (title.contains("two sum")) {
            sb.append("const readline = require('readline');\n");
            sb.append("const rl = readline.createInterface({input: process.stdin});\n");
            sb.append("let lines=[]; rl.on('line',l=>lines.push(l)).on('close',()=>{\n");
            sb.append("  const [n,target]=lines[0].split(' ').map(Number);\n");
            sb.append("  const nums=lines[1].split(' ').map(Number);\n");
            sb.append("  const res=twoSum(nums,target);\n");
            sb.append("  console.log(res[0],res[1]);\n});\n");
        } else if (title.contains("palindrome")) {
            sb.append("const readline = require('readline');\n");
            sb.append("const rl = readline.createInterface({input: process.stdin});\n");
            sb.append("rl.on('line',l=>{console.log(isPalindrome(Number(l)));rl.close();});\n");
        } else if (title.contains("fizzbuzz")) {
            sb.append("const readline = require('readline');\n");
            sb.append("const rl = readline.createInterface({input: process.stdin});\n");
            sb.append("rl.on('line',l=>{fizzBuzz(Number(l)).forEach(s=>console.log(s));rl.close();});\n");
        }
        return sb.toString();
    }

    private String readStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private boolean compareOutput(String actual, String expected) {
        if (actual == null || expected == null) {
            return actual == expected;
        }
        
        // Normalize whitespace and compare
        String normalizedActual = actual.trim().replaceAll("\\s+", " ");
        String normalizedExpected = expected.trim().replaceAll("\\s+", " ");
        
        return normalizedActual.equals(normalizedExpected);
    }

    private static class ExecutionResult {
        String output;
        String error;
        long executionTime;
        long memoryUsed;
        boolean timedOut;

        ExecutionResult(String output, String error, long executionTime, long memoryUsed, boolean timedOut) {
            this.output = output;
            this.error = error;
            this.executionTime = executionTime;
            this.memoryUsed = memoryUsed;
            this.timedOut = timedOut;
        }
    }
}
