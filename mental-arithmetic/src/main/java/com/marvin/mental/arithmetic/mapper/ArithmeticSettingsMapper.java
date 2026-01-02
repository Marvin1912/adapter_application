package com.marvin.mental.arithmetic.mapper;

import com.marvin.mental.arithmetic.entity.ArithmeticSettingsEntity;
import com.marvin.mental.arithmetic.entity.SettingsOperationEntity;
import com.marvin.mental.arithmetic.enums.OperationType;
import com.marvin.mental.arithmetic.model.ArithmeticSettings;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ArithmeticSettingsMapper {

    default ArithmeticSettingsEntity toEntity(ArithmeticSettings model) {
        if (model == null) {
            return null;
        }
        ArithmeticSettingsEntity entity = new ArithmeticSettingsEntity();
        entity.setDifficulty(model.getDifficulty());
        entity.setProblemCount(model.getProblemCount());
        entity.setTimeLimit(model.getTimeLimit());
        entity.setShowImmediateFeedback(model.getShowImmediateFeedback());
        entity.setAllowPause(model.getAllowPause());
        entity.setShowProgress(model.getShowProgress());
        entity.setShowTimer(model.getShowTimer());
        entity.setEnableSound(model.getEnableSound());
        entity.setUseKeypad(model.getUseKeypad());
        entity.setSessionName(model.getSessionName());
        entity.setShuffleProblems(model.getShuffleProblems());
        entity.setRepeatIncorrectProblems(model.getRepeatIncorrectProblems());
        entity.setMaxRetries(model.getMaxRetries());
        entity.setShowCorrectAnswer(model.getShowCorrectAnswer());
        if (model.getDisplaySettings() != null) {
            entity.setFontSize(model.getDisplaySettings().getFontSize());
            entity.setHighContrast(model.getDisplaySettings().getHighContrast());
        }
        if (model.getOperations() != null) {
            Set<SettingsOperationEntity> ops = model.getOperations().stream()
                    .map(op -> {
                        SettingsOperationEntity opEntity = new SettingsOperationEntity();
                        opEntity.setSettings(entity);
                        opEntity.setOperationType(op);
                        return opEntity;
                    })
                    .collect(Collectors.toSet());
            entity.setOperations(ops);
        }
        return entity;
    }

    default ArithmeticSettings toModel(ArithmeticSettingsEntity entity) {
        if (entity == null) {
            return null;
        }
        java.util.List<OperationType> operations = entity.getOperations() != null
                ? entity.getOperations().stream()
                        .map(SettingsOperationEntity::getOperationType)
                        .toList()
                : java.util.List.of();

        ArithmeticSettings.DisplaySettings displaySettings = null;
        if (entity.getFontSize() != null || entity.getHighContrast() != null) {
            displaySettings = new ArithmeticSettings.DisplaySettings(
                    entity.getFontSize(),
                    entity.getHighContrast()
            );
        }

        return new ArithmeticSettings(
                operations,
                entity.getDifficulty(),
                entity.getProblemCount(),
                entity.getTimeLimit(),
                entity.getShowImmediateFeedback(),
                entity.getAllowPause(),
                entity.getShowProgress(),
                entity.getShowTimer(),
                entity.getEnableSound(),
                entity.getUseKeypad(),
                entity.getSessionName(),
                entity.getShuffleProblems(),
                entity.getRepeatIncorrectProblems(),
                entity.getMaxRetries(),
                entity.getShowCorrectAnswer(),
                displaySettings
        );
    }
}
