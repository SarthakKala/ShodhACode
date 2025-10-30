package com.shodh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponse {
    private Long contestId;
    private String contestTitle;
    private LocalDateTime lastUpdated;
    private List<LeaderboardEntry> entries;
}

