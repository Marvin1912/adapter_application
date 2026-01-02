package com.marvin.mental.arithmetic.controller;

import com.marvin.mental.arithmetic.model.ArithmeticSession;
import com.marvin.mental.arithmetic.model.ArithmeticSettings;
import com.marvin.mental.arithmetic.service.ArithmeticService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api")
public class ArithmeticController {

    private final ArithmeticService service;

    public ArithmeticController(ArithmeticService service) {
        this.service = service;
    }

    // Sessions

    @PostMapping("/sessions")
    public Mono<ResponseEntity<ArithmeticSession>> createSession(@RequestBody ArithmeticSettings settings) {
        return Mono.fromCallable(() -> service.createSession(settings))
                .subscribeOn(Schedulers.boundedElastic())
                .map(session -> ResponseEntity.status(HttpStatus.CREATED).body(session));
    }

    @PutMapping("/sessions/{id}")
    public Mono<ResponseEntity<ArithmeticSession>> updateSession(@PathVariable String id, @RequestBody ArithmeticSession session) {
        return Mono.fromCallable(() -> service.updateSession(id, session))
                .subscribeOn(Schedulers.boundedElastic())
                .map(result -> result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build());
    }

    @GetMapping("/sessions")
    public Flux<ArithmeticSession> getAllSessions() {
        return Flux.defer(() -> Flux.fromIterable(service.getAllSessions()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/sessions/{id}")
    public Mono<ResponseEntity<ArithmeticSession>> getSession(@PathVariable String id) {
        return Mono.fromCallable(() -> service.getSession(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(result -> result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build());
    }

    @DeleteMapping("/sessions/{id}")
    public Mono<ResponseEntity<Void>> deleteSession(@PathVariable String id) {
        return Mono.fromRunnable(() -> service.deleteSession(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    // Session State Transitions

    @PostMapping("/sessions/{id}/start")
    public Mono<ResponseEntity<ArithmeticSession>> startSession(@PathVariable String id) {
        return Mono.fromCallable(() -> service.startSession(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(result -> result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/pause")
    public Mono<ResponseEntity<ArithmeticSession>> pauseSession(@PathVariable String id) {
        return Mono.fromCallable(() -> service.pauseSession(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(result -> result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/resume")
    public Mono<ResponseEntity<ArithmeticSession>> resumeSession(@PathVariable String id) {
        return Mono.fromCallable(() -> service.resumeSession(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(result -> result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/complete")
    public Mono<ResponseEntity<ArithmeticSession>> completeSession(@PathVariable String id) {
        return Mono.fromCallable(() -> service.completeSession(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(result -> result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build());
    }

    // Settings

    @GetMapping("/settings")
    public Mono<ArithmeticSettings> getSettings() {
        return Mono.fromCallable(service::getSettings)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/settings")
    public Mono<ArithmeticSettings> updateSettings(@RequestBody ArithmeticSettings settings) {
        return Mono.fromCallable(() -> service.updateSettings(settings))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
