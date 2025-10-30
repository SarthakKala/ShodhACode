package com.shodh.backend.service;

import com.shodh.backend.dto.SubmissionRequest;
import com.shodh.backend.dto.SubmissionResponse;
import com.shodh.backend.model.*;
import com.shodh.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final JudgeService judgeService;

    public SubmissionResponse submitCode(SubmissionRequest request) {
        // Basic language validation and normalization
        String lang = request.getLanguage() == null ? "" : request.getLanguage().trim().toLowerCase();
        if (!("java".equals(lang) || "python".equals(lang) || "cpp".equals(lang) || "javascript".equals(lang))) {
            throw new RuntimeException("Unsupported language. Allowed: java, python, cpp, javascript");
        }

        // Find or create user
        User user = userRepository.findByUsername(request.getUsername())
            .orElseGet(() -> {
                User newUser = User.builder()
                    .username(request.getUsername())
                    .build();
                return userRepository.save(newUser);
            });

        // Find contest
        Contest contest = contestRepository.findById(request.getContestId())
            .orElseThrow(() -> new RuntimeException("Contest not found"));

        // Ensure user is recorded as participant of the contest
        boolean alreadyParticipant = contest.getParticipants().stream()
            .anyMatch(u -> u.getId().equals(user.getId()));
        if (!alreadyParticipant) {
            contest.getParticipants().add(user);
            contestRepository.save(contest);
        }

        // Find problem
        Problem problem = problemRepository.findById(request.getProblemId())
            .orElseThrow(() -> new RuntimeException("Problem not found"));

        // Create submission
        Submission submission = Submission.builder()
            .code(request.getCode())
            .language(lang)
            .status(SubmissionStatus.PENDING)
            .user(user)
            .contest(contest)
            .problem(problem)
            .build();

        submission = submissionRepository.save(submission);

        // Process submission asynchronously
        processSubmissionAsync(submission.getId());

        return mapToSubmissionResponse(submission);
    }

    @Async
    public CompletableFuture<Void> processSubmissionAsync(Long submissionId) {
        try {
            // This will be called asynchronously
            judgeService.judgeSubmission(submissionId);
        } catch (Exception e) {
            log.error("Error processing submission {}: {}", submissionId, e.getMessage());
            // Update submission status to error
            Submission submission = submissionRepository.findById(submissionId)
                .orElse(null);
            if (submission != null) {
                submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                submission.setError(e.getMessage());
                submissionRepository.save(submission);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    public SubmissionResponse getSubmissionById(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        return mapToSubmissionResponse(submission);
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        return SubmissionResponse.builder()
            .submissionId(submission.getId())
            .username(submission.getUser().getUsername())
            .problemId(submission.getProblem().getId())
            .problemTitle(submission.getProblem().getTitle())
            .code(submission.getCode())
            .language(submission.getLanguage())
            .status(submission.getStatus())
            .executionTime(submission.getExecutionTime())
            .memoryUsed(submission.getMemoryUsed())
            .output(submission.getOutput())
            .error(submission.getError())
            .submittedAt(submission.getSubmittedAt())
            .build();
    }
}

