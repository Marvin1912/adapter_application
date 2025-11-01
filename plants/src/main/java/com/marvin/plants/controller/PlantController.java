package com.marvin.plants.controller;

import com.marvin.image.service.ImageService;
import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.service.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(path = "/plants")
public class PlantController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;
    private final ImageService imageService;

    public PlantController(PlantService plantService, ImageService imageService) {
        this.plantService = plantService;
        this.imageService = imageService;
    }

    private static Mono<byte[]> getFileAsByteArray(Mono<FilePart> file) {
        return file.flatMap(filePart ->
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
            @RequestPart("plant") Mono<PlantDTO> plant,
            @RequestParam(name = "content-type", required = false) String contentType
    ) {
        return Mono.zip(plant, getFileAsByteArray(file))
                .doOnError(throwable -> LOGGER.error("", throwable))
                .flatMap(objects -> {
                    final byte[] image = objects.getT2();
                    final Mono<String> tmp = image.length == 0
                            ? Mono.just("")
                            : imageService.saveImage(image, contentType)
                            .map(UUID::toString);
                    return tmp.zipWith(Mono.just(objects.getT1()));
                })
                .flatMap(objects ->
                        Mono.fromCallable(() -> {
                                    final long plantId = plantService.createPlant(objects.getT2(), objects.getT1());
                                    return ResponseEntity.created(URI.create("/plants/%s".formatted(plantId))).build();
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                );
    }

    @PutMapping
    public Mono<ResponseEntity<Object>> updatePlant(@RequestBody Mono<PlantDTO> plantMono) {
        return plantMono.flatMap(plant ->
                Mono.fromCallable(() -> {
                            plantService.updatePlant(plant);
                            return ResponseEntity.noContent().build();
                        })
                        .subscribeOn(Schedulers.boundedElastic())
        );
    }

    @GetMapping
    public Flux<PlantDTO> getPlants() {
        return plantService.getPlants();
    }

    @GetMapping(path = "/{id}")
    public Mono<PlantDTO> getPlant(@PathVariable long id) {
        return Mono.justOrEmpty(plantService.getPlant(id));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePlant(@PathVariable long id) {
        plantService.deletePlant(id);
        return Mono.just(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{id}/watered")
    public Mono<ResponseEntity<PlantDTO>> waterPlant(
            @PathVariable long id,
            @RequestParam("last-watered") LocalDate lastWatered
    ) {
        return Mono.just(id)
                .flatMap(aLong ->
                        Mono.fromCallable(
                                () -> plantService.waterPlant(id, lastWatered)
                        ).subscribeOn(Schedulers.boundedElastic())
                )
                .map(ResponseEntity::ok);
    }

}
