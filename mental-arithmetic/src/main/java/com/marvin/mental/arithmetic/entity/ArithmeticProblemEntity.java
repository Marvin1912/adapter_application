package com.marvin.mental.arithmetic.entity;

import com.marvin.mental.arithmetic.enums.Difficulty;
import com.marvin.mental.arithmetic.enums.OperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "arithmetic_problem", schema = "mental_arithmetic")
public class ArithmeticProblemEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ArithmeticSessionEntity session;

    @Column(name = "expression", nullable = false)
    private String expression;

    @Column(name = "answer", nullable = false)
    private Integer answer;

    @Column(name = "user_answer")
    private Integer userAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "time_spent", nullable = false)
    private Long timeSpent;

    @Column(name = "presented_at", nullable = false)
    private Instant presentedAt;

    @Column(name = "answered_at")
    private Instant answeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private Difficulty difficulty;

    @Column(name = "operand1", nullable = false)
    private Integer operand1;

    @Column(name = "operand2", nullable = false)
    private Integer operand2;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArithmeticProblemEntity that = (ArithmeticProblemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
