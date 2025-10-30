package com.shodh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry {
    private Integer rank;
    private String username;
    private Integer problemsSolved;
    private Integer totalPoints;
    private Long totalTime; // in milliseconds
    private LocalDateTime lastAcceptedAt;
}

