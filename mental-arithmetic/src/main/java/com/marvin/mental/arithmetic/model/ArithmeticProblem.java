package com.marvin.mental.arithmetic.model;

import com.marvin.mental.arithmetic.enums.Difficulty;
import com.marvin.mental.arithmetic.enums.OperationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArithmeticProblem {

    private String id;
    private String expression;
    private Integer answer;
    private Integer userAnswer;
    private Boolean isCorrect;
    private Long timeSpent;
    private Instant presentedAt;
    private Instant answeredAt;
    private OperationType operationType;
    private Difficulty difficulty;
    private Integer operand1;
    private Integer operand2;
}
