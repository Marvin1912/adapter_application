package com.marvin.mental.arithmetic.mapper;

import com.marvin.mental.arithmetic.entity.ArithmeticProblemEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSessionEntity;
import com.marvin.mental.arithmetic.model.ArithmeticProblem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArithmeticProblemMapper {

    default ArithmeticProblemEntity toEntity(ArithmeticProblem model, String sessionId) {
        if (model == null) {
            return null;
        }
        ArithmeticProblemEntity entity = new ArithmeticProblemEntity();
        entity.setId(model.getId());
        entity.setExpression(model.getExpression());
        entity.setAnswer(model.getAnswer());
        entity.setUserAnswer(model.getUserAnswer());
        entity.setIsCorrect(model.getIsCorrect());
        entity.setTimeSpent(model.getTimeSpent());
        entity.setPresentedAt(model.getPresentedAt());
        entity.setAnsweredAt(model.getAnsweredAt());
        entity.setOperationType(model.getOperationType());
        entity.setDifficulty(model.getDifficulty());
        entity.setOperand1(model.getOperand1());
        entity.setOperand2(model.getOperand2());
        if (sessionId != null) {
            ArithmeticSessionEntity session = new ArithmeticSessionEntity();
            session.setId(sessionId);
            entity.setSession(session);
        }
        return entity;
    }

    default ArithmeticProblem toModel(ArithmeticProblemEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ArithmeticProblem(
                entity.getId(),
                entity.getExpression(),
                entity.getAnswer(),
                entity.getUserAnswer(),
                entity.getIsCorrect(),
                entity.getTimeSpent(),
                entity.getPresentedAt(),
                entity.getAnsweredAt(),
                entity.getOperationType(),
                entity.getDifficulty(),
                entity.getOperand1(),
                entity.getOperand2()
        );
    }
}
