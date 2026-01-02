package com.marvin.mental.arithmetic.service;

import com.marvin.mental.arithmetic.enums.Difficulty;
import com.marvin.mental.arithmetic.enums.OperationType;
import com.marvin.mental.arithmetic.enums.SessionStatus;
import com.marvin.mental.arithmetic.model.ArithmeticProblem;
import com.marvin.mental.arithmetic.model.ArithmeticSession;
import com.marvin.mental.arithmetic.model.ArithmeticSettings;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ArithmeticService {

    private final Map<String, ArithmeticSession> sessions = new HashMap<>();
    private ArithmeticSettings defaultSettings = createDefaultSettings();

    private static ArithmeticSettings createDefaultSettings() {
        ArithmeticSettings settings = new ArithmeticSettings();
        settings.setOperations(List.of(OperationType.ADDITION));
        settings.setDifficulty(Difficulty.EASY);
        settings.setProblemCount(10);
        settings.setTimeLimit(null);
        settings.setShowImmediateFeedback(true);
        settings.setAllowPause(true);
        settings.setShowProgress(true);
        settings.setShowTimer(true);
        settings.setEnableSound(false);
        settings.setUseKeypad(false);
        settings.setShuffleProblems(false);
        settings.setRepeatIncorrectProblems(false);
        settings.setMaxRetries(3);
        settings.setShowCorrectAnswer(true);
        return settings;
    }

    public Mono<ArithmeticSession> createSession(ArithmeticSettings settings) {
        String sessionId = java.util.UUID.randomUUID().toString();
        List<ArithmeticProblem> problems = generateProblems(settings);

        ArithmeticSession session = new ArithmeticSession();
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

        sessions.put(sessionId, session);
        return Mono.just(session);
    }

    public Mono<ArithmeticSession> updateSession(String id, ArithmeticSession updates) {
        ArithmeticSession session = sessions.get(id);
        if (session == null) {
            return Mono.empty();
        }

        if (updates.getProblems() != null) {
            session.setProblems(updates.getProblems());
        }
        if (updates.getCurrentProblemIndex() != null) {
            session.setCurrentProblemIndex(updates.getCurrentProblemIndex());
        }

        recalculateMetrics(session);

        if (session.getProblemsCompleted().equals(session.getTotalProblems())) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setEndTime(Instant.now());
            session.setIsCompleted(true);
        }

        sessions.put(id, session);
        return Mono.just(session);
    }

    public Flux<ArithmeticSession> getAllSessions() {
        return Flux.fromIterable(sessions.values());
    }

    public Mono<ArithmeticSession> getSession(String id) {
        return Mono.justOrEmpty(sessions.get(id));
    }

    public Mono<Void> deleteSession(String id) {
        sessions.remove(id);
        return Mono.empty();
    }

    public Mono<ArithmeticSession> startSession(String id) {
        ArithmeticSession session = sessions.get(id);
        if (session == null) {
            return Mono.empty();
        }
        session.setStartTime(Instant.now());
        session.setStatus(SessionStatus.ACTIVE);
        return Mono.just(session);
    }

    public Mono<ArithmeticSession> pauseSession(String id) {
        ArithmeticSession session = sessions.get(id);
        if (session == null) {
            return Mono.empty();
        }
        session.setStatus(SessionStatus.PAUSED);
        return Mono.just(session);
    }

    public Mono<ArithmeticSession> resumeSession(String id) {
        ArithmeticSession session = sessions.get(id);
        if (session == null) {
            return Mono.empty();
        }
        session.setStatus(SessionStatus.ACTIVE);
        return Mono.just(session);
    }

    public Mono<ArithmeticSession> completeSession(String id) {
        ArithmeticSession session = sessions.get(id);
        if (session == null) {
            return Mono.empty();
        }
        session.setEndTime(Instant.now());
        session.setStatus(SessionStatus.COMPLETED);
        session.setIsCompleted(true);
        return Mono.just(session);
    }

    public Mono<ArithmeticSettings> getSettings() {
        return Mono.just(defaultSettings);
    }

    public Mono<ArithmeticSettings> updateSettings(ArithmeticSettings settings) {
        this.defaultSettings = settings;
        return Mono.just(settings);
    }

    private List<ArithmeticProblem> generateProblems(ArithmeticSettings settings) {
        return new java.util.ArrayList<>() {{
            for (int i = 0; i < settings.getProblemCount(); i++) {
                add(generateProblem(settings));
            }
        }};
    }

    private ArithmeticProblem generateProblem(ArithmeticSettings settings) {
        Random random = new Random();
        List<OperationType> ops = settings.getOperations();
        OperationType operation = ops.isEmpty() ? OperationType.ADDITION
                : ops.get(random.nextInt(ops.size()));
        Difficulty difficulty = settings.getDifficulty();

        int min = getMinForDifficulty(difficulty);
        int max = getMaxForDifficulty(difficulty);

        int operand1 = random.nextInt(max - min + 1) + min;
        int operand2 = random.nextInt(max - min + 1) + min;
        int answer;
        String expression;

        if (operation == OperationType.ADDITION) {
            answer = operand1 + operand2;
            expression = operand1 + " + " + operand2;
        } else if (operation == OperationType.SUBTRACTION) {
            int minuend = Math.max(operand1, operand2);
            int subtrahend = Math.min(operand1, operand2);
            answer = minuend - subtrahend;
            expression = minuend + " - " + subtrahend;
            operand1 = minuend;
            operand2 = subtrahend;
        } else {
            operation = OperationType.ADDITION;
            answer = operand1 + operand2;
            expression = operand1 + " + " + operand2;
        }

        ArithmeticProblem problem = new ArithmeticProblem();
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

    private void recalculateMetrics(ArithmeticSession session) {
        List<ArithmeticProblem> problems = session.getProblems();
        int correctAnswers = 0;
        int incorrectAnswers = 0;
        int problemsCompleted = 0;
        long totalTimeSpent = 0;

        for (ArithmeticProblem problem : problems) {
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
        session.setAverageTimePerProblem(problemsCompleted > 0 ? (totalTimeSpent * 1.0 / problemsCompleted) : 0.0);
    }
}
