package com.marvin.mental.arithmetic.repository.reactive;

import com.marvin.mental.arithmetic.entity.ArithmeticProblemEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSessionEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSettingsEntity;
import com.marvin.mental.arithmetic.entity.SettingsOperationEntity;
import com.marvin.mental.arithmetic.enums.Difficulty;
import com.marvin.mental.arithmetic.enums.OperationType;
import com.marvin.mental.arithmetic.mapper.ArithmeticSessionMapper;
import com.marvin.mental.arithmetic.mapper.ArithmeticSettingsMapper;
import com.marvin.mental.arithmetic.model.ArithmeticProblem;
import com.marvin.mental.arithmetic.model.ArithmeticSession;
import com.marvin.mental.arithmetic.model.ArithmeticSettings;
import com.marvin.mental.arithmetic.repository.ArithmeticProblemRepository;
import com.marvin.mental.arithmetic.repository.ArithmeticSessionRepository;
import com.marvin.mental.arithmetic.repository.ArithmeticSettingsRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Set;

@Component
public class ReactiveArithmeticRepository {

    private final ArithmeticSessionRepository sessionRepository;
    private final ArithmeticProblemRepository problemRepository;
    private final ArithmeticSettingsRepository settingsRepository;
    private final ArithmeticSessionMapper sessionMapper;
    private final ArithmeticSettingsMapper settingsMapper;

    public ReactiveArithmeticRepository(ArithmeticSessionRepository sessionRepository,
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
    public Mono<ArithmeticSession> save(ArithmeticSession session) {
        return Mono.fromCallable(() -> {
                    ArithmeticSessionEntity entity = sessionMapper.toEntity(session);
                    ArithmeticSessionEntity saved = sessionRepository.save(entity);
                    return sessionMapper.toModel(saved);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ArithmeticSession> update(String sessionId, ArithmeticSession updates) {
        return Mono.fromCallable(() -> {
                    ArithmeticSessionEntity entity = sessionRepository.findById(sessionId).orElse(null);
                    if (entity == null) {
                        return null;
                    }
                    if (updates.getCurrentProblemIndex() != null) {
                        entity.setCurrentProblemIndex(updates.getCurrentProblemIndex());
                    }
                    if (updates.getProblems() != null) {
                        entity.getProblems().clear();
                        for (ArithmeticProblem problem : updates.getProblems()) {
                            ArithmeticProblemEntity problemEntity = problemRepository.findById(problem.getId())
                                    .orElseGet(() -> {
                                        ArithmeticProblemEntity newEntity = new ArithmeticProblemEntity();
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
                    ArithmeticSessionEntity saved = sessionRepository.save(entity);
                    return sessionMapper.toModel(saved);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ArithmeticSession> findById(String id) {
        return Mono.fromCallable(() ->
                        sessionRepository.findById(id)
                                .map(sessionMapper::toModel)
                                .orElse(null)
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Flux<ArithmeticSession> findAll() {
        return Flux.defer(() ->
                        Flux.fromIterable(sessionRepository.findAll())
                                .map(sessionMapper::toModel)
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> sessionRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
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

    @Transactional
    public Mono<ArithmeticSettings> getSettings() {
        return Mono.fromCallable(() -> {
                    List<ArithmeticSettingsEntity> all = settingsRepository.findAllWithOperations();
                    if (all.isEmpty()) {
                        ArithmeticSettingsEntity entity = createDefaultSettingsEntity();
                        ArithmeticSettingsEntity saved = settingsRepository.save(entity);
                        return settingsMapper.toModel(saved);
                    }
                    return settingsMapper.toModel(all.get(0));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ArithmeticSettings> updateSettings(ArithmeticSettings settings) {
        return Mono.fromCallable(() -> {
                    List<ArithmeticSettingsEntity> all = settingsRepository.findAllWithOperations();
                    ArithmeticSettingsEntity entity;
                    if (all.isEmpty()) {
                        entity = settingsRepository.save(settingsMapper.toEntity(settings));
                    } else {
                        entity = all.get(0);
                        ArithmeticSettingsEntity updated = settingsMapper.toEntity(settings);
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
                            SettingsOperationEntity newOp = new SettingsOperationEntity();
                            newOp.setSettings(entity);
                            newOp.setOperationType(op.getOperationType());
                            entity.getOperations().add(newOp);
                        }
                        entity = settingsRepository.save(entity);
                    }
                    return settingsMapper.toModel(entity);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static ArithmeticSettingsEntity createDefaultSettingsEntity() {
        ArithmeticSettingsEntity entity = new ArithmeticSettingsEntity();
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

        SettingsOperationEntity op = new SettingsOperationEntity();
        op.setSettings(entity);
        op.setOperationType(OperationType.ADDITION);
        entity.setOperations(Set.of(op));

        return entity;
    }
}
