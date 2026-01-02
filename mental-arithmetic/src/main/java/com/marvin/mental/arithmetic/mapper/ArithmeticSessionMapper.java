package com.marvin.mental.arithmetic.mapper;

import com.marvin.mental.arithmetic.entity.ArithmeticProblemEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSessionEntity;
import com.marvin.mental.arithmetic.model.ArithmeticProblem;
import com.marvin.mental.arithmetic.model.ArithmeticSession;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ArithmeticSettingsMapper.class, ArithmeticProblemMapper.class})
public interface ArithmeticSessionMapper {

    default ArithmeticSessionEntity toEntity(ArithmeticSession model) {
        if (model == null) {
            return null;
        }
        final ArithmeticSessionEntity entity = new ArithmeticSessionEntity();
        entity.setId(model.getId());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setStartTime(model.getStartTime());
        entity.setEndTime(model.getEndTime());
        entity.setStatus(model.getStatus());
        entity.setCurrentProblemIndex(model.getCurrentProblemIndex());
        entity.setScore(model.getScore());
        entity.setCorrectAnswers(model.getCorrectAnswers());
        entity.setIncorrectAnswers(model.getIncorrectAnswers());
        entity.setTotalTimeSpent(model.getTotalTimeSpent());
        entity.setProblemsCompleted(model.getProblemsCompleted());
        entity.setTotalProblems(model.getTotalProblems());
        entity.setAccuracy(model.getAccuracy());
        entity.setAvgTimePerProblem(model.getAverageTimePerProblem());
        entity.setIsCompleted(model.getIsCompleted());
        entity.setIsTimedOut(model.getIsTimedOut());
        entity.setNotes(model.getNotes());

        final ArithmeticSettingsMapper settingsMapper = new ArithmeticSettingsMapper() { };
        if (model.getSettings() != null) {
            entity.setSettings(settingsMapper.toEntity(model.getSettings()));
        }

        if (model.getProblems() != null) {
            final ArithmeticProblemMapper problemMapper = new ArithmeticProblemMapper() { };
            final Set<ArithmeticProblemEntity> problemEntities = model.getProblems().stream()
                    .map(p -> problemMapper.toEntity(p, entity.getId()))
                    .peek(e -> e.setSession(entity))
                    .collect(Collectors.toSet());
            entity.setProblems(problemEntities);
        }

        return entity;
    }

    default ArithmeticSession toModel(ArithmeticSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        final ArithmeticSettingsMapper settingsMapper = new ArithmeticSettingsMapper() { };
        final ArithmeticProblemMapper problemMapper = new ArithmeticProblemMapper() { };

        final java.util.List<ArithmeticProblem> problems = entity.getProblems() != null
                ? entity.getProblems().stream()
                        .map(problemMapper::toModel)
                        .toList()
                : java.util.List.of();

        return new ArithmeticSession(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                settingsMapper.toModel(entity.getSettings()),
                problems,
                entity.getCurrentProblemIndex(),
                entity.getScore(),
                entity.getCorrectAnswers(),
                entity.getIncorrectAnswers(),
                entity.getTotalTimeSpent(),
                entity.getProblemsCompleted(),
                entity.getTotalProblems(),
                entity.getAccuracy(),
                entity.getAvgTimePerProblem(),
                entity.getIsCompleted(),
                entity.getIsTimedOut(),
                entity.getNotes()
        );
    }
}
