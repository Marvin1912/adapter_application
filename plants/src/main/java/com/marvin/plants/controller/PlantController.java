package com.marvin.plants.controller;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.service.PlantService;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> createPlant(@RequestBody PlantDTO plant) {
        long plantId = plantService.createPlant(plant);
        return Mono.just(ResponseEntity.created(URI.create("/plants/%s".formatted(plantId))).build());
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
            @RequestPart("file") Flux<FilePart> fileMono
    ) {
        return Mono.from(fileMono
                .flatMap(filePart ->
                        DataBufferUtils.join(filePart.content())
                                .flatMap(dataBuffer -> {
                                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(bytes);
                                    DataBufferUtils.release(dataBuffer);

                                    return Mono.fromCallable(() -> {
                                                plantService.uploadImage(id, bytes);
                                                return ResponseEntity.noContent().build();
                                            })
                                            .subscribeOn(Schedulers.boundedElastic());
                                })
                ));
    }

}
