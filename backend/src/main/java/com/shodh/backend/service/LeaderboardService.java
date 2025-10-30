package com.shodh.backend.service;

import com.shodh.backend.dto.LeaderboardEntry;
import com.shodh.backend.dto.LeaderboardResponse;
import com.shodh.backend.model.*;
import com.shodh.backend.repository.ContestRepository;
import com.shodh.backend.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {
    private final ContestRepository contestRepository;
    private final SubmissionRepository submissionRepository;

    public LeaderboardResponse getLeaderboard(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
            .orElseThrow(() -> new RuntimeException("Contest not found"));

        // Get all submissions for the contest
        List<Submission> submissions = submissionRepository.findByContestId(contestId);

        // Group submissions by user and problem to calculate scores
        Map<User, UserScore> userScores = calculateUserScores(submissions, contest);

        // Convert to leaderboard entries and sort
        List<LeaderboardEntry> entries = userScores.entrySet().stream()
            .map(entry -> createLeaderboardEntry(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> {
                // Sort by: 1. Problems solved (desc), 2. Total points (desc), 3. Total time (asc)
                int problemsCompare = b.getProblemsSolved().compareTo(a.getProblemsSolved());
                if (problemsCompare != 0) return problemsCompare;
                
                int pointsCompare = b.getTotalPoints().compareTo(a.getTotalPoints());
                if (pointsCompare != 0) return pointsCompare;
                
                return a.getTotalTime().compareTo(b.getTotalTime());
            })
            .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return LeaderboardResponse.builder()
            .contestId(contest.getId())
            .contestTitle(contest.getTitle())
            .lastUpdated(LocalDateTime.now())
            .entries(entries)
            .build();
    }

    private Map<User, UserScore> calculateUserScores(List<Submission> submissions, Contest contest) {
        Map<User, UserScore> userScores = new HashMap<>();
        
        // Get contest start time for penalty calculation
        LocalDateTime contestStart = contest.getStartTime();

        for (Submission submission : submissions) {
            if (submission.getStatus() != SubmissionStatus.ACCEPTED) {
                continue;
            }

            User user = submission.getUser();
            Problem problem = submission.getProblem();
            
            UserScore userScore = userScores.computeIfAbsent(user, k -> new UserScore());
            
            // Check if user already solved this problem
            if (!userScore.solvedProblems.contains(problem.getId())) {
                userScore.solvedProblems.add(problem.getId());
                userScore.totalPoints += problem.getPoints();
                
                // Calculate time penalty (minutes from contest start)
                long minutesFromStart = java.time.Duration.between(contestStart, submission.getSubmittedAt()).toMinutes();
                userScore.totalTime += minutesFromStart;
                
                // Update last accepted submission time
                if (userScore.lastAcceptedAt == null || submission.getSubmittedAt().isAfter(userScore.lastAcceptedAt)) {
                    userScore.lastAcceptedAt = submission.getSubmittedAt();
                }
            }
        }

        return userScores;
    }

    private LeaderboardEntry createLeaderboardEntry(User user, UserScore score) {
        return LeaderboardEntry.builder()
            .username(user.getUsername())
            .problemsSolved(score.solvedProblems.size())
            .totalPoints(score.totalPoints)
            .totalTime(score.totalTime * 60 * 1000) // Convert minutes to milliseconds
            .lastAcceptedAt(score.lastAcceptedAt)
            .build();
    }

    private static class UserScore {
        Set<Long> solvedProblems = new HashSet<>();
        int totalPoints = 0;
        long totalTime = 0; // in minutes
        LocalDateTime lastAcceptedAt = null;
    }
}

