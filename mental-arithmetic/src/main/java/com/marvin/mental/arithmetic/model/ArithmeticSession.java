package com.marvin.mental.arithmetic.model;

import com.marvin.mental.arithmetic.enums.SessionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArithmeticSession {

    private String id;
    private Instant createdAt;
    private Instant startTime;
    private Instant endTime;
    private SessionStatus status;
    private ArithmeticSettings settings;
    private List<ArithmeticProblem> problems;
    private Integer currentProblemIndex;
    private Integer score;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Long totalTimeSpent;
    private Integer problemsCompleted;
    private Integer totalProblems;
    private Double accuracy;
    private Double averageTimePerProblem;
    private Boolean isCompleted;
    private Boolean isTimedOut;
    private String notes;
}
