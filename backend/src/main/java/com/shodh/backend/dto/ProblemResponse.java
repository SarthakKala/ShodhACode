package com.shodh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private String constraints;
    private String sampleInput;
    private String sampleOutput;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer points;
    private Integer totalSubmissions;
    private Integer acceptedSubmissions;
}

