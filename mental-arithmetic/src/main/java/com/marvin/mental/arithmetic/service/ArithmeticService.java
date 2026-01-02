package com.marvin.mental.arithmetic.service;

import com.marvin.mental.arithmetic.entity.ArithmeticProblemEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSessionEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSettingsEntity;
import com.marvin.mental.arithmetic.entity.SettingsOperationEntity;
import com.marvin.mental.arithmetic.enums.Difficulty;
import com.marvin.mental.arithmetic.enums.OperationType;
import com.marvin.mental.arithmetic.enums.SessionStatus;
import com.marvin.mental.arithmetic.mapper.ArithmeticSessionMapper;
import com.marvin.mental.arithmetic.mapper.ArithmeticSettingsMapper;
import com.marvin.mental.arithmetic.model.ArithmeticProblem;
import com.marvin.mental.arithmetic.model.ArithmeticSession;
import com.marvin.mental.arithmetic.model.ArithmeticSettings;
import com.marvin.mental.arithmetic.repository.ArithmeticProblemRepository;
import com.marvin.mental.arithmetic.repository.ArithmeticSessionRepository;
import com.marvin.mental.arithmetic.repository.ArithmeticSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class ArithmeticService {

    private final ArithmeticSessionRepository sessionRepository;
    private final ArithmeticProblemRepository problemRepository;
    private final ArithmeticSettingsRepository settingsRepository;
    private final ArithmeticSessionMapper sessionMapper;
    private final ArithmeticSettingsMapper settingsMapper;

    public ArithmeticService(ArithmeticSessionRepository sessionRepository,
                             ArithmeticProblemRepository problemRepository,
                             ArithmeticSettingsRepository settingsRepository,
                             ArithmeticSessionMapper sessionMapper,
                             ArithmeticSettingsMapper settingsMapper) {
        this.sessionRepository = sessionRepository;
        this.problemRepository = problemRepository;
        this.settingsRepository = settingsRepository;
        this.sessionMapper = sessionMapper;
        this.settingsMapper = settingsMapper;
    }

    @Transactional
    public ArithmeticSession createSession(ArithmeticSettings settings) {
        final String sessionId = java.util.UUID.randomUUID().toString();
        final List<ArithmeticProblem> problems = generateProblems(settings);

        final ArithmeticSession session = new ArithmeticSession();
        session.setId(sessionId);
        session.setCreatedAt(Instant.now());
        session.setStartTime(null);
        session.setEndTime(null);
        session.setStatus(SessionStatus.CREATED);
        session.setSettings(settings);
        session.setProblems(problems);
        session.setCurrentProblemIndex(0);
        session.setScore(0);
        session.setCorrectAnswers(0);
        session.setIncorrectAnswers(0);
        session.setTotalTimeSpent(0L);
        session.setProblemsCompleted(0);
        session.setTotalProblems(problems.size());
        session.setAccuracy(0.0);
        session.setAverageTimePerProblem(0.0);
        session.setIsCompleted(false);
        session.setIsTimedOut(false);

        final ArithmeticSessionEntity entity = sessionMapper.toEntity(session);
        final ArithmeticSessionEntity saved = sessionRepository.save(entity);
        return sessionMapper.toModel(saved);
    }

    @Transactional
    public ArithmeticSession updateSession(String id, ArithmeticSession updates) {
        final ArithmeticSessionEntity entity = sessionRepository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        if (updates.getCurrentProblemIndex() != null) {
            entity.setCurrentProblemIndex(updates.getCurrentProblemIndex());
        }
        if (updates.getProblems() != null) {
            entity.getProblems().clear();
            for (ArithmeticProblem problem : updates.getProblems()) {
                final ArithmeticProblemEntity problemEntity = problemRepository.findById(problem.getId())
                        .orElseGet(() -> {
                            final ArithmeticProblemEntity newEntity = new ArithmeticProblemEntity();
                            newEntity.setId(problem.getId());
                            newEntity.setSession(entity);
                            return newEntity;
                        });
                problemEntity.setUserAnswer(problem.getUserAnswer());
                problemEntity.setIsCorrect(problem.getIsCorrect());
                problemEntity.setTimeSpent(problem.getTimeSpent());
                problemEntity.setAnsweredAt(problem.getAnsweredAt());
                entity.getProblems().add(problemEntity);
            }
        }
        recalculateMetrics(entity);
        if (entity.getProblemsCompleted().equals(entity.getTotalProblems())) {
            entity.setEndTime(java.time.Instant.now());
            entity.setIsCompleted(true);
        }
        final ArithmeticSessionEntity saved = sessionRepository.save(entity);
        final ArithmeticSession session = sessionMapper.toModel(saved);

        if (session.getProblemsCompleted().equals(session.getTotalProblems())) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setEndTime(Instant.now());
            session.setIsCompleted(true);
        }
        return session;
    }

    public List<ArithmeticSession> getAllSessions() {
        return sessionRepository.findAllWithProblemsAndSettings().stream()
                .map(sessionMapper::toModel)
                .toList();
    }

    public ArithmeticSession getSession(String id) {
        return sessionRepository.findByIdWithProblemsAndSettings(id)
                .map(sessionMapper::toModel)
                .orElse(null);
    }

    @Transactional
    public void deleteSession(String id) {
        sessionRepository.deleteById(id);
    }

    @Transactional
    public ArithmeticSession startSession(String id) {
        final ArithmeticSessionEntity entity = sessionRepository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setStartTime(Instant.now());
        entity.setStatus(SessionStatus.ACTIVE);
        final ArithmeticSessionEntity saved = sessionRepository.save(entity);
        return sessionMapper.toModel(saved);
    }

    @Transactional
    public ArithmeticSession pauseSession(String id) {
        final ArithmeticSessionEntity entity = sessionRepository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setStatus(SessionStatus.PAUSED);
        final ArithmeticSessionEntity saved = sessionRepository.save(entity);
        return sessionMapper.toModel(saved);
    }

    @Transactional
    public ArithmeticSession resumeSession(String id) {
        final ArithmeticSessionEntity entity = sessionRepository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setStatus(SessionStatus.ACTIVE);
        final ArithmeticSessionEntity saved = sessionRepository.save(entity);
        return sessionMapper.toModel(saved);
    }

    @Transactional
    public ArithmeticSession completeSession(String id) {
        final ArithmeticSessionEntity entity = sessionRepository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setEndTime(Instant.now());
        entity.setStatus(SessionStatus.COMPLETED);
        entity.setIsCompleted(true);
        final ArithmeticSessionEntity saved = sessionRepository.save(entity);
        return sessionMapper.toModel(saved);
    }

    public ArithmeticSettings getSettings() {
        final List<ArithmeticSettingsEntity> all = settingsRepository.findAllWithOperations();
        if (all.isEmpty()) {
            final ArithmeticSettingsEntity entity = createDefaultSettingsEntity();
            final ArithmeticSettingsEntity saved = settingsRepository.save(entity);
            return settingsMapper.toModel(saved);
        }
        return settingsMapper.toModel(all.get(0));
    }

    @Transactional
    public ArithmeticSettings updateSettings(ArithmeticSettings settings) {
        final List<ArithmeticSettingsEntity> all = settingsRepository.findAllWithOperations();
        ArithmeticSettingsEntity entity;
        if (all.isEmpty()) {
            entity = settingsRepository.save(settingsMapper.toEntity(settings));
        } else {
            entity = all.get(0);
            final ArithmeticSettingsEntity updated = settingsMapper.toEntity(settings);
            entity.setDifficulty(updated.getDifficulty());
            entity.setProblemCount(updated.getProblemCount());
            entity.setTimeLimit(updated.getTimeLimit());
            entity.setShowImmediateFeedback(updated.getShowImmediateFeedback());
            entity.setAllowPause(updated.getAllowPause());
            entity.setShowProgress(updated.getShowProgress());
            entity.setShowTimer(updated.getShowTimer());
            entity.setEnableSound(updated.getEnableSound());
            entity.setUseKeypad(updated.getUseKeypad());
            entity.setSessionName(updated.getSessionName());
            entity.setShuffleProblems(updated.getShuffleProblems());
            entity.setRepeatIncorrectProblems(updated.getRepeatIncorrectProblems());
            entity.setMaxRetries(updated.getMaxRetries());
            entity.setShowCorrectAnswer(updated.getShowCorrectAnswer());
            entity.setFontSize(updated.getFontSize());
            entity.setHighContrast(updated.getHighContrast());
            entity.getOperations().clear();
            for (SettingsOperationEntity op : updated.getOperations()) {
                final SettingsOperationEntity newOp = new SettingsOperationEntity();
                newOp.setSettings(entity);
                newOp.setOperationType(op.getOperationType());
                entity.getOperations().add(newOp);
            }
            entity = settingsRepository.save(entity);
        }
        return settingsMapper.toModel(entity);
    }

    private List<ArithmeticProblem> generateProblems(ArithmeticSettings settings) {
        final List<ArithmeticProblem> problems = new java.util.ArrayList<>();
        for (int i = 0; i < settings.getProblemCount(); i++) {
            problems.add(generateProblem(settings));
        }
        return problems;
    }

    private ArithmeticProblem generateProblem(ArithmeticSettings settings) {
        final Random random = new Random();
        final List<OperationType> ops = settings.getOperations();
        final OperationType operation = ops.isEmpty() ? OperationType.ADDITION
                : ops.get(random.nextInt(ops.size()));
        final Difficulty difficulty = settings.getDifficulty();

        final int min = getMinForDifficulty(difficulty);
        final int max = getMaxForDifficulty(difficulty);

        int operand1 = random.nextInt(max - min + 1) + min;
        int operand2 = random.nextInt(max - min + 1) + min;
        final int answer;
        final String expression;

        if (operation == OperationType.ADDITION) {
            answer = operand1 + operand2;
            expression = operand1 + " + " + operand2;
        } else if (operation == OperationType.SUBTRACTION) {
            final int minuend = Math.max(operand1, operand2);
            final int subtrahend = Math.min(operand1, operand2);
            final int answerTemp = minuend - subtrahend;
            final String expressionTemp = minuend + " - " + subtrahend;
            answer = answerTemp;
            expression = expressionTemp;
            operand1 = minuend;
            operand2 = subtrahend;
        } else {
            final int operationAnswer = operand1 + operand2;
            answer = operationAnswer;
            expression = operand1 + " + " + operand2;
        }

        final ArithmeticProblem problem = new ArithmeticProblem();
        problem.setId("problem_" + System.currentTimeMillis() + "_" + Integer.toString(random.nextInt(1000000000), 36));
        problem.setExpression(expression);
        problem.setAnswer(answer);
        problem.setUserAnswer(null);
        problem.setIsCorrect(null);
        problem.setTimeSpent(0L);
        problem.setPresentedAt(Instant.now());
        problem.setAnsweredAt(null);
        problem.setOperationType(operation);
        problem.setDifficulty(difficulty);
        problem.setOperand1(operand1);
        problem.setOperand2(operand2);

        return problem;
    }

    private int getMinForDifficulty(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 100;
            case HARD -> 1000;
        };
    }

    private int getMaxForDifficulty(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 99;
            case MEDIUM -> 999;
            case HARD -> 9999;
        };
    }

    private void recalculateMetrics(ArithmeticSessionEntity session) {
        int correctAnswers = 0;
        int incorrectAnswers = 0;
        int problemsCompleted = 0;
        long totalTimeSpent = 0;

        for (ArithmeticProblemEntity problem : session.getProblems()) {
            if (problem.getIsCorrect() != null) {
                if (problem.getIsCorrect()) {
                    correctAnswers++;
                } else {
                    incorrectAnswers++;
                }
                problemsCompleted++;
            }
            totalTimeSpent += problem.getTimeSpent();
        }

        session.setCorrectAnswers(correctAnswers);
        session.setIncorrectAnswers(incorrectAnswers);
        session.setProblemsCompleted(problemsCompleted);
        session.setScore(correctAnswers);
        session.setTotalTimeSpent(totalTimeSpent);
        session.setAccuracy(problemsCompleted > 0 ? (correctAnswers * 100.0 / problemsCompleted) : 0.0);
        session.setAvgTimePerProblem(problemsCompleted > 0 ? (totalTimeSpent * 1.0 / problemsCompleted) : 0.0);
    }

    private static ArithmeticSettingsEntity createDefaultSettingsEntity() {
        final ArithmeticSettingsEntity entity = new ArithmeticSettingsEntity();
        entity.setDifficulty(Difficulty.EASY);
        entity.setProblemCount(10);
        entity.setTimeLimit(null);
        entity.setShowImmediateFeedback(true);
        entity.setAllowPause(true);
        entity.setShowProgress(true);
        entity.setShowTimer(true);
        entity.setEnableSound(false);
        entity.setUseKeypad(false);
        entity.setShuffleProblems(false);
        entity.setRepeatIncorrectProblems(false);
        entity.setMaxRetries(3);
        entity.setShowCorrectAnswer(true);
        entity.setFontSize(null);
        entity.setHighContrast(null);

        final SettingsOperationEntity op = new SettingsOperationEntity();
        op.setSettings(entity);
        op.setOperationType(OperationType.ADDITION);
        entity.setOperations(Set.of(op));

        return entity;
    }
}
