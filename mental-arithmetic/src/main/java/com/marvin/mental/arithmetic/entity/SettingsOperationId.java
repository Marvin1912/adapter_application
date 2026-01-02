package com.marvin.mental.arithmetic.entity;

import com.marvin.mental.arithmetic.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettingsOperationId implements Serializable {

    private Integer settings;
    private OperationType operationType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SettingsOperationId that = (SettingsOperationId) o;
        return Objects.equals(settings, that.settings) && operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(settings, operationType);
    }
}
