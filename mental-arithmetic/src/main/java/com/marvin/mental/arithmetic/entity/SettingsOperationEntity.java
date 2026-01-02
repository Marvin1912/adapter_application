package com.marvin.mental.arithmetic.entity;

import com.marvin.mental.arithmetic.enums.OperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "settings_operations", schema = "mental_arithmetic")
@IdClass(SettingsOperationId.class)
public class SettingsOperationEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settings_id", nullable = false)
    private ArithmeticSettingsEntity settings;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsOperationEntity that = (SettingsOperationEntity) o;
        return Objects.equals(settings, that.settings) && operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(settings, operationType);
    }
}
