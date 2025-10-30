package com.shodh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRequest {
    @NotBlank
    private String username;
    
    @NotNull
    private Long contestId;
    
    @NotNull
    private Long problemId;
    
    @NotBlank
    private String code;
    
    @NotBlank
    private String language;
}

