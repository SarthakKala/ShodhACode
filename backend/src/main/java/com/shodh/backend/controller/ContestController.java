package com.shodh.backend.controller;

import com.shodh.backend.dto.ContestResponse;
import com.shodh.backend.dto.LeaderboardResponse;
import com.shodh.backend.service.ContestService;
import com.shodh.backend.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173"}, allowCredentials = "true")
public class ContestController {
    private final ContestService contestService;
    private final LeaderboardService leaderboardService;

    @GetMapping("/{contestId}")
    public ResponseEntity<ContestResponse> getContest(@PathVariable Long contestId) {
        ContestResponse response = contestService.getContestById(contestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{contestId}/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(@PathVariable Long contestId) {
        LeaderboardResponse response = leaderboardService.getLeaderboard(contestId);
        return ResponseEntity.ok(response);
    }
}
