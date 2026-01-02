package com.marvin.mental.arithmetic.model;

import com.marvin.mental.arithmetic.enums.SessionStatus;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
