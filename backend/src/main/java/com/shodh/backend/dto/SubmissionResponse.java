package com.shodh.backend.dto;

import com.shodh.backend.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long submissionId;
    private String username;
    private Long problemId;
    private String problemTitle;
    private String code;
    private String language;
    private SubmissionStatus status;
    private Long executionTime;
    private Long memoryUsed;
    private String output;
    private String error;
    private LocalDateTime submittedAt;
}

