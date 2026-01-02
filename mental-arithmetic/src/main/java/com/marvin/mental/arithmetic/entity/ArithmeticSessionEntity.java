package com.marvin.mental.arithmetic.entity;

import com.marvin.mental.arithmetic.enums.SessionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "arithmetic_session", schema = "mental_arithmetic")
public class ArithmeticSessionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "settings_id", nullable = false)
    private ArithmeticSettingsEntity settings;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ArithmeticProblemEntity> problems = new HashSet<>();

    @Column(name = "current_problem_index", nullable = false)
    private Integer currentProblemIndex;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "incorrect_answers", nullable = false)
    private Integer incorrectAnswers;

    @Column(name = "total_time_spent", nullable = false)
    private Long totalTimeSpent;

    @Column(name = "problems_completed", nullable = false)
    private Integer problemsCompleted;

    @Column(name = "total_problems", nullable = false)
    private Integer totalProblems;

    @Column(name = "accuracy", nullable = false)
    private Double accuracy;

    @Column(name = "avg_time_per_problem", nullable = false)
    private Double avgTimePerProblem;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "is_timed_out", nullable = false)
    private Boolean isTimedOut;

    @Column(name = "notes")
    private String notes;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ArithmeticSessionEntity that = (ArithmeticSessionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
