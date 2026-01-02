package com.marvin.mental.arithmetic.repository.reactive;

import com.marvin.mental.arithmetic.entity.ArithmeticProblemEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSessionEntity;
import com.marvin.mental.arithmetic.entity.ArithmeticSettingsEntity;
import com.marvin.mental.arithmetic.mapper.ArithmeticSessionMapper;
import com.marvin.mental.arithmetic.model.ArithmeticProblem;
import com.marvin.mental.arithmetic.model.ArithmeticSession;
import com.marvin.mental.arithmetic.repository.ArithmeticProblemRepository;
import com.marvin.mental.arithmetic.repository.ArithmeticSessionRepository;
import com.marvin.mental.arithmetic.repository.ArithmeticSettingsRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class ReactiveArithmeticRepository {

    private final ArithmeticSessionRepository sessionRepository;
    private final ArithmeticProblemRepository problemRepository;
    private final ArithmeticSettingsRepository settingsRepository;
    private final ArithmeticSessionMapper sessionMapper;

    public ReactiveArithmeticRepository(ArithmeticSessionRepository sessionRepository,
                                        ArithmeticProblemRepository problemRepository,
                                        ArithmeticSettingsRepository settingsRepository,
                                        ArithmeticSessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.problemRepository = problemRepository;
        this.settingsRepository = settingsRepository;
        this.sessionMapper = sessionMapper;
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
}
