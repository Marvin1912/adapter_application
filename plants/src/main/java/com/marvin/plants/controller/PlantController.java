package com.marvin.plants.controller;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.service.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

@RestController
@RequestMapping(path = "/plants")
public class PlantController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    private static Mono<byte[]> getFileAsByteArray(Mono<FilePart> file) {
        return file
                .flatMap(filePart ->
                        DataBufferUtils.join(filePart.content())
                                .flatMap(dataBuffer -> {
                                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(bytes);
                                    DataBufferUtils.release(dataBuffer);
                                    return Mono.just(bytes);
                                })
                )
                .switchIfEmpty(Mono.just(new byte[0]))
                .onErrorResume(e -> {
                    LOGGER.error("", e);
                    return Mono.just(new byte[0]);
                });
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> createPlant(
            @RequestPart(value = "image", required = false) Mono<FilePart> file,
            @RequestPart("plant") Mono<PlantDTO> plant
    ) {
        return Mono.from(Flux.zip(plant, getFileAsByteArray(file))
                .doOnError(throwable -> LOGGER.error("", throwable))
                .flatMap(objects ->
                        Mono.fromCallable(() -> {
                                    long plantId = plantService.createPlant(objects.getT1());
                                    plantService.uploadImage(plantId, objects.getT2());
                                    return ResponseEntity.created(URI.create("/plants/%s".formatted(plantId))).build();
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                )
        );
    }

    @GetMapping
    public Flux<PlantDTO> getPlants() {
        return plantService.getPlants();
    }

    @GetMapping(path = "/{id}")
    public Mono<PlantDTO> getPlant(@PathVariable long id) {
        return Mono.just(plantService.getPlant(id));
    }

    @PutMapping(path = "/{id}/image")
    public Mono<ResponseEntity<Object>> uploadImage(
            @PathVariable long id,
            @RequestPart("file") Mono<FilePart> fileMono
    ) {
        return getFileAsByteArray(fileMono)
                .flatMap(bytes ->
                        Mono.fromCallable(() -> {
                                    plantService.uploadImage(id, bytes);
                                    return ResponseEntity.noContent().build();
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                );
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePlant(@PathVariable long id) {
        plantService.deletePlant(id);
        return Mono.just(ResponseEntity.noContent().build());
    }

}
