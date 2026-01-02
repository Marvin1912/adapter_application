package com.marvin.mental.arithmetic.model;

import com.marvin.mental.arithmetic.enums.Difficulty;
import com.marvin.mental.arithmetic.enums.OperationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArithmeticSettings {

    private List<OperationType> operations;
    private Difficulty difficulty;
    private Integer problemCount;
    private Integer timeLimit;
    private Boolean showImmediateFeedback;
    private Boolean allowPause;
    private Boolean showProgress;
    private Boolean showTimer;
    private Boolean enableSound;
    private Boolean useKeypad;
    private String sessionName;
    private Boolean shuffleProblems;
    private Boolean repeatIncorrectProblems;
    private Integer maxRetries;
    private Boolean showCorrectAnswer;
    private DisplaySettings displaySettings;

    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DisplaySettings {
        private String fontSize;
        private Boolean highContrast;
    }
}
