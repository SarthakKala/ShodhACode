import { useState, useEffect } from "react";
import Editor from "@monaco-editor/react";
import { motion } from "framer-motion";
import toast from "react-hot-toast";
import { submissionAPI } from "../services/api";

const CodeEditor = ({ problemId, contestId, onSubmissionUpdate }) => {
  const getTemplate = (lang, pid) => {
    const id = Number(pid);
    if (lang === "java") {
      if (id === 1)
        return `// Implement function only\npublic int[] twoSum(int[] nums, int target) {\n    // TODO: write logic\n    return new int[]{0,0};\n}`;
      if (id === 2)
        return `// Implement function only\npublic boolean isPalindrome(int x) {\n    // TODO: write logic\n    return false;\n}`;
      if (id === 3)
        return `// Implement function only\npublic java.util.List<String> fizzBuzz(int n) {\n    java.util.List<String> res = new java.util.ArrayList<>();\n    // TODO: write logic\n    return res;\n}`;
      return `// Implement your function here`;
    }
    if (lang === "python") {
      if (id === 1)
        return `# def two_sum(nums: List[int], target: int) -> List[int]:\n#     pass`;
      if (id === 2) return `# def is_palindrome(x: int) -> bool:\n#     pass`;
      if (id === 3) return `# def fizz_buzz(n: int) -> List[str]:\n#     pass`;
      return `# Write your function`;
    }
    if (lang === "cpp") {
      if (id === 1)
        return `// vector<int> twoSum(vector<int>& nums, int target) {\n// }`;
      if (id === 2) return `// bool isPalindrome(int x) {\n// }`;
      if (id === 3) return `// vector<string> fizzBuzz(int n) {\n// }`;
      return `// Write your function`;
    }
    if (lang === "javascript") {
      if (id === 1) return `// function twoSum(nums, target) {\n// }`;
      if (id === 2) return `// function isPalindrome(x) {\n// }`;
      if (id === 3) return `// function fizzBuzz(n) {\n// }`;
      return `// Write your function`;
    }
    return `// Write your function`;
  };

  const [codes, setCodes] = useState({
    java: getTemplate("java", problemId),
    python: getTemplate("python", problemId),
    cpp: getTemplate("cpp", problemId),
    javascript: getTemplate("javascript", problemId),
  });
  const [language, setLanguage] = useState("java");
  const [submitting, setSubmitting] = useState(false);
  const [submissionStatus, setSubmissionStatus] = useState(null);
  const [submissionId, setSubmissionId] = useState(null);

  const languages = [
    { value: "java", label: "Java", mode: "java" },
    { value: "python", label: "Python", mode: "python" },
    { value: "cpp", label: "C++", mode: "cpp" },
    { value: "javascript", label: "JavaScript", mode: "javascript" },
  ];

  // Update template when problem or language changes (only if user hasn't typed custom code yet)
  const isDefault = (text) =>
    /Implement|Write your function|def two_sum|vector<int> twoSum|function twoSum/.test(
      text || ""
    );

  const onChangeLanguage = (val) => {
    setLanguage(val);
    setCodes((prev) => ({
      ...prev,
      [val]: prev[val] ?? getTemplate(val, problemId),
    }));
  };

  // Initialize entry for selected language if missing
  useEffect(() => {
    setCodes((prev) => {
      if (prev[language] == null) {
        return { ...prev, [language]: getTemplate(language, problemId) };
      }
      return prev;
    });
  }, [language, problemId]);

  // Refresh template for current language when problem changes IF user hasn't edited
  useEffect(() => {
    setCodes((prev) => {
      const current = prev[language] ?? "";
      if (isDefault(current)) {
        return { ...prev, [language]: getTemplate(language, problemId) };
      }
      return prev;
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [problemId]);

  // Persist codes and language in localStorage for refresh survival
  const storageKey = `codes:${contestId}:${problemId}`;
  const langKey = `lang:${contestId}:${problemId}`;

  // Load stored state
  useEffect(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed && typeof parsed === "object") {
          setCodes((prev) => ({ ...prev, ...parsed }));
        }
      }
      const savedLang = localStorage.getItem(langKey);
      if (savedLang) setLanguage(savedLang);
    } catch {}
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [storageKey]);

  // Save on changes
  useEffect(() => {
    try {
      localStorage.setItem(storageKey, JSON.stringify(codes));
      localStorage.setItem(langKey, language);
    } catch {}
  }, [codes, language, storageKey, langKey]);

  // Clear status when switching problems (avoid showing ACCEPTED on other problems)
  useEffect(() => {
    setSubmissionStatus(null);
    setSubmissionId(null);
  }, [problemId]);

  const handleSubmit = async () => {
    const username = localStorage.getItem("username");

    const curr = (codes[language] ?? "").trim();
    if (!curr) {
      toast.error("Please write some code before submitting");
      return;
    }

    if (!problemId) {
      toast.error("Please select a problem first");
      return;
    }

    setSubmitting(true);
    setSubmissionStatus("PENDING");

    try {
      // Submit code
      const response = await submissionAPI.submitCode({
        username,
        contestId,
        problemId,
        code: codes[language] ?? "",
        language,
      });

      const newSubmissionId = response.data.submissionId;
      setSubmissionId(newSubmissionId);
      toast.success("Code submitted! Judging in progress...");

      // Start polling for status
      pollSubmissionStatus(newSubmissionId);
    } catch (error) {
      toast.error("Failed to submit code");
      setSubmitting(false);
      setSubmissionStatus(null);
    }
  };

  const pollSubmissionStatus = async (subId) => {
    let attempts = 0;
    const maxAttempts = 30; // Poll for max 60 seconds (30 * 2)

    const poll = async () => {
      try {
        const response = await submissionAPI.getSubmission(subId);
        const status = response.data.status;

        setSubmissionStatus(status);

        if (status !== "PENDING" && status !== "RUNNING") {
          // Final status reached
          setSubmitting(false);

          if (status === "ACCEPTED") {
            toast.success("🎉 Solution Accepted!");
            if (onSubmissionUpdate) onSubmissionUpdate();
          } else if (status === "WRONG_ANSWER") {
            toast.error("❌ Wrong Answer");
          } else if (status === "TIME_LIMIT_EXCEEDED") {
            toast.error("⏱️ Time Limit Exceeded");
          } else if (status === "RUNTIME_ERROR") {
            toast.error("💥 Runtime Error");
          } else if (status === "COMPILATION_ERROR") {
            toast.error("🔨 Compilation Error");
          }

          return; // Stop polling
        }

        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(poll, 2000); // Poll every 2 seconds
        } else {
          setSubmitting(false);
          toast.error("Judging timeout. Please try again.");
        }
      } catch (error) {
        console.error("Error polling submission:", error);
        setSubmitting(false);
        toast.error("Failed to get submission status");
      }
    };

    poll();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "PENDING":
        return "text-yellow-400";
      case "RUNNING":
        return "text-blue-400";
      case "ACCEPTED":
        return "text-green-400";
      case "WRONG_ANSWER":
      case "TIME_LIMIT_EXCEEDED":
      case "RUNTIME_ERROR":
      case "COMPILATION_ERROR":
        return "text-red-400";
      default:
        return "text-gray-400";
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case "PENDING":
        return "⏳";
      case "RUNNING":
        return "🔄";
      case "ACCEPTED":
        return "✅";
      case "WRONG_ANSWER":
        return "❌";
      case "TIME_LIMIT_EXCEEDED":
        return "⏱️";
      case "RUNTIME_ERROR":
        return "💥";
      case "COMPILATION_ERROR":
        return "🔨";
      default:
        return "❓";
    }
  };

  return (
    <div className="h-full flex flex-col space-y-4">
      {/* Editor Header */}
      <div className="glass-card p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div className="relative">
              <select
                value={language}
                onChange={(e) => onChangeLanguage(e.target.value)}
                className="appearance-none bg-dark-surface/70 border border-glass-border rounded-lg pl-4 pr-10 py-2 text-white focus:outline-none focus:border-accent-blue hover:bg-dark-surface transition-colors"
                disabled={submitting}
              >
                {languages.map((lang) => (
                  <option
                    key={lang.value}
                    value={lang.value}
                    className="bg-dark-card"
                  >
                    {lang.label}
                  </option>
                ))}
              </select>
              <svg
                className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M5.23 7.21a.75.75 0 011.06.02L10 11.065l3.71-3.834a.75.75 0 111.08 1.04l-4.24 4.38a.75.75 0 01-1.08 0L5.25 8.27a.75.75 0 01-.02-1.06z"
                  clipRule="evenodd"
                />
              </svg>
            </div>

            {submissionStatus && (
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                className={`flex items-center space-x-2 px-3 py-1 rounded-full bg-dark-surface/50 border border-glass-border`}
              >
                <span className="text-lg">
                  {getStatusIcon(submissionStatus)}
                </span>
                <span
                  className={`text-sm font-medium ${getStatusColor(
                    submissionStatus
                  )}`}
                >
                  {submissionStatus.replace("_", " ")}
                </span>
              </motion.div>
            )}
          </div>

          <button
            onClick={handleSubmit}
            disabled={submitting || !problemId}
            className="px-6 py-2 bg-gradient-to-r from-green-500 to-green-600 text-white font-semibold rounded-lg
                     hover:from-green-600 hover:to-green-700 disabled:opacity-50 disabled:cursor-not-allowed
                     transition-all duration-300 flex items-center space-x-2"
          >
            {submitting ? (
              <>
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                    fill="none"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                <span>Judging...</span>
              </>
            ) : (
              <>
                <span>▶</span>
                <span>Submit</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Code Editor */}
      <div className="flex-1 code-editor-container">
        <Editor
          height="100%"
          language={languages.find((l) => l.value === language)?.mode || "java"}
          value={codes[language] ?? ""}
          onChange={(val) =>
            setCodes((prev) => ({ ...prev, [language]: val ?? "" }))
          }
          theme="vs-dark"
          options={{
            minimap: { enabled: false },
            fontSize: 14,
            lineNumbers: "on",
            roundedSelection: true,
            scrollBeyondLastLine: false,
            automaticLayout: true,
            tabSize: 4,
            wordWrap: "on",
            padding: { top: 16, bottom: 16 },
          }}
        />
      </div>

      {/* Editor Footer */}
      <div className="glass-card p-3">
        <div className="flex items-center justify-between text-sm text-gray-500">
          <div className="flex items-center space-x-4">
            <span>Lines: {(codes[language] ?? "").split("\n").length}</span>
            <span>Characters: {(codes[language] ?? "").length}</span>
          </div>
          <div className="flex items-center space-x-2">
            <span className="inline-block w-2 h-2 bg-green-400 rounded-full animate-pulse"></span>
            <span>Ready</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CodeEditor;
