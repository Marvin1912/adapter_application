package com.marvin.mental.arithmetic.entity;

import com.marvin.mental.arithmetic.enums.Difficulty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "arithmetic_settings", schema = "mental_arithmetic")
public class ArithmeticSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "arithmetic_settings_id_gen")
    @SequenceGenerator(name = "arithmetic_settings_id_gen", sequenceName = "mental_arithmetic.arithmetic_settings_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private Difficulty difficulty;

    @Column(name = "problem_count", nullable = false)
    private Integer problemCount;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @Column(name = "show_immediate_feedback", nullable = false)
    private Boolean showImmediateFeedback;

    @Column(name = "allow_pause", nullable = false)
    private Boolean allowPause;

    @Column(name = "show_progress", nullable = false)
    private Boolean showProgress;

    @Column(name = "show_timer", nullable = false)
    private Boolean showTimer;

    @Column(name = "enable_sound", nullable = false)
    private Boolean enableSound;

    @Column(name = "use_keypad", nullable = false)
    private Boolean useKeypad;

    @Column(name = "session_name")
    private String sessionName;

    @Column(name = "shuffle_problems", nullable = false)
    private Boolean shuffleProblems;

    @Column(name = "repeat_incorrect_problems", nullable = false)
    private Boolean repeatIncorrectProblems;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "show_correct_answer", nullable = false)
    private Boolean showCorrectAnswer;

    @Column(name = "font_size")
    private String fontSize;

    @Column(name = "high_contrast")
    private Boolean highContrast;

    @OneToMany(mappedBy = "settings", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SettingsOperationEntity> operations = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ArithmeticSettingsEntity that = (ArithmeticSettingsEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
