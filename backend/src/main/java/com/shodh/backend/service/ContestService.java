package com.shodh.backend.service;

import com.shodh.backend.dto.ContestResponse;
import com.shodh.backend.dto.ProblemResponse;
import com.shodh.backend.model.Contest;
import com.shodh.backend.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestService {
    private final ContestRepository contestRepository;

    public ContestResponse getContestById(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
            .orElseThrow(() -> new RuntimeException("Contest not found with id: " + contestId));
        
        return mapToContestResponse(contest);
    }

    private ContestResponse mapToContestResponse(Contest contest) {
        LocalDateTime now = LocalDateTime.now();
        boolean isActive = now.isAfter(contest.getStartTime()) && now.isBefore(contest.getEndTime());
        
        return ContestResponse.builder()
            .id(contest.getId())
            .title(contest.getTitle())
            .description(contest.getDescription())
            .startTime(contest.getStartTime())
            .endTime(contest.getEndTime())
            .problems(contest.getProblems().stream()
                .map(this::mapToProblemResponse)
                .collect(Collectors.toList()))
            .participantsCount(contest.getParticipants().size())
            .isActive(isActive)
            .build();
    }

    private ProblemResponse mapToProblemResponse(com.shodh.backend.model.Problem problem) {
        int totalSubmissions = problem.getSubmissions().size();
        int acceptedSubmissions = (int) problem.getSubmissions().stream()
            .filter(s -> s.getStatus() == com.shodh.backend.model.SubmissionStatus.ACCEPTED)
            .count();
        
        return ProblemResponse.builder()
            .id(problem.getId())
            .title(problem.getTitle())
            .description(problem.getDescription())
            .constraints(problem.getConstraints())
            .sampleInput(problem.getSampleInput())
            .sampleOutput(problem.getSampleOutput())
            .timeLimit(problem.getTimeLimit())
            .memoryLimit(problem.getMemoryLimit())
            .points(problem.getPoints())
            .totalSubmissions(totalSubmissions)
            .acceptedSubmissions(acceptedSubmissions)
            .build();
    }
}
