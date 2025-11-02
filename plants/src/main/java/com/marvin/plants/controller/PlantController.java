package com.marvin.plants.controller;

import com.marvin.image.service.ImageService;
import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.service.PlantService;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
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

/**
 * REST Controller for managing plants. Provides endpoints for CRUD operations and plant care
 * activities.
 */
@RestController
@RequestMapping(path = "/plants")
public class PlantController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlantController.class);
    private static final String PLANTS_LOCATION_PREFIX = "/plants/";
    private static final String EMPTY_STRING = "";

    private final PlantService plantService;
    private final ImageService imageService;

    public PlantController(PlantService plantService, ImageService imageService) {
        this.plantService = plantService;
        this.imageService = imageService;
    }

    /**
     * Converts a FilePart to a byte array. Handles empty files and errors gracefully by returning
     * empty byte array.
     *
     * @param filePartMono Mono containing the file part to convert
     * @return Mono containing the byte array representation of the file
     */
    private static Mono<byte[]> getFileAsByteArray(Mono<FilePart> filePartMono) {
        return filePartMono
                .flatMap(PlantController::extractBytesFromFilePart)
                .switchIfEmpty(Mono.just(new byte[0]))
                .onErrorResume(PlantController::handleFileReadError);
    }

    /**
     * Extracts bytes from a FilePart by reading its content.
     *
     * @param filePart the file part to extract bytes from
     * @return Mono containing the extracted bytes
     */
    private static Mono<byte[]> extractBytesFromFilePart(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return Mono.just(bytes);
                });
    }

    /**
     * Handles file read errors by logging and returning empty byte array.
     *
     * @param error the error that occurred
     * @return Mono containing empty byte array
     */
    private static Mono<byte[]> handleFileReadError(Throwable error) {
        LOGGER.error("Error reading file", error);
        return Mono.just(new byte[0]);
    }

    /**
     * Creates a new plant with optional image.
     *
     * @param filePartMono Optional image file
     * @param plantMono    Plant data
     * @param contentType  Content type of the image (optional)
     * @return Mono containing ResponseEntity with location of created plant
     */
    @PostMapping
    public Mono<ResponseEntity<Object>> createPlant(
            @RequestPart(value = "image", required = false) Mono<FilePart> filePartMono,
            @RequestPart("plant") Mono<PlantDTO> plantMono,
            @RequestParam(name = "content-type", required = false) String contentType
    ) {
        return Mono.zip(plantMono, getFileAsByteArray(filePartMono))
                .doOnError(this::logPlantCreationError)
                .flatMap(plantAndImage -> processImageAndCreatePlant(plantAndImage, contentType));
    }

    /**
     * Processes image and creates plant using the provided data.
     *
     * @param plantAndImage Tuple containing PlantDTO and image bytes
     * @param contentType   Content type of the image
     * @return Mono containing ResponseEntity with location of created plant
     */
    private Mono<ResponseEntity<Object>> processImageAndCreatePlant(
            reactor.util.function.Tuple2<PlantDTO, byte[]> plantAndImage, String contentType) {

        PlantDTO plantDTO = plantAndImage.getT1();
        byte[] imageBytes = plantAndImage.getT2();

        return saveImageIfNotEmpty(imageBytes, contentType)
                .flatMap(imageUuid -> createPlantWithImage(plantDTO, imageUuid));
    }

    /**
     * Saves image if bytes are not empty, otherwise returns empty UUID string.
     *
     * @param imageBytes  Image bytes to save
     * @param contentType Content type of the image
     * @return Mono containing UUID string of saved image or empty string
     */
    private Mono<String> saveImageIfNotEmpty(byte[] imageBytes, String contentType) {
        if (imageBytes.length == 0) {
            return Mono.just(EMPTY_STRING);
        }

        return imageService.saveImage(imageBytes, contentType)
                .map(UUID::toString);
    }

    /**
     * Creates plant with the provided image UUID.
     *
     * @param plantDTO  Plant data to create
     * @param imageUuid UUID of the saved image (empty string if no image)
     * @return Mono containing ResponseEntity with location of created plant
     */
    private Mono<ResponseEntity<Object>> createPlantWithImage(PlantDTO plantDTO, String imageUuid) {
        return Mono.fromCallable(() -> {
            long plantId = plantService.createPlant(plantDTO, imageUuid);
            return createCreatedResponse(plantId);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Creates a ResponseEntity with CREATED status and Location header.
     *
     * @param plantId ID of the created plant
     * @return ResponseEntity with location header
     */
    private ResponseEntity<Object> createCreatedResponse(long plantId) {
        URI location = URI.create(PLANTS_LOCATION_PREFIX + plantId);
        return ResponseEntity.created(location).build();
    }

    /**
     * Logs errors that occur during plant creation.
     *
     * @param throwable the error that occurred
     */
    private void logPlantCreationError(Throwable throwable) {
        LOGGER.error("Error creating plant", throwable);
    }

    /**
     * Updates an existing plant.
     *
     * @param plantMono Mono containing updated plant data
     * @return Mono containing ResponseEntity with NO_CONTENT status
     */
    @PutMapping
    public Mono<ResponseEntity<Object>> updatePlant(@RequestBody Mono<PlantDTO> plantMono) {
        return plantMono.flatMap(this::updatePlantSynchronously);
    }

    /**
     * Updates plant in a blocking manner wrapped in a callable.
     *
     * @param plantDTO Plant data to update
     * @return Mono containing ResponseEntity with NO_CONTENT status
     */
    private Mono<ResponseEntity<Object>> updatePlantSynchronously(PlantDTO plantDTO) {
        return Mono.fromCallable(() -> {
                    plantService.updatePlant(plantDTO);
                    return ResponseEntity.noContent().build();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves all plants.
     *
     * @return Flux containing all plants
     */
    @GetMapping
    public Flux<PlantDTO> getPlants() {
        return plantService.getPlants();
    }

    /**
     * Retrieves a specific plant by ID. Returns empty Mono if plant is not found.
     *
     * @param id ID of the plant to retrieve
     * @return Mono containing the plant data or empty if not found
     */
    @GetMapping(path = "/{id}")
    public Mono<PlantDTO> getPlant(@PathVariable long id) {
        return Mono.justOrEmpty(plantService.getPlant(id));
    }

    /**
     * Deletes a plant by ID.
     *
     * @param id ID of the plant to delete
     * @return Mono containing ResponseEntity with NO_CONTENT status
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePlant(@PathVariable long id) {
        plantService.deletePlant(id);
        return Mono.just(ResponseEntity.noContent().build());
    }

    /**
     * Records that a plant has been watered and updates watering schedule.
     *
     * @param id          ID of the plant to water
     * @param lastWatered Date when the plant was last watered
     * @return Mono containing ResponseEntity with updated plant data
     */
    @PatchMapping("/{id}/watered")
    public Mono<ResponseEntity<PlantDTO>> waterPlant(
            @PathVariable long id,
            @RequestParam("last-watered") LocalDate lastWatered
    ) {
        return Mono.just(id)
                .flatMap(plantId -> updateWateringDate(plantId, lastWatered))
                .map(ResponseEntity::ok);
    }

    /**
     * Updates the watering date for a plant. Runs on boundedElastic scheduler to avoid blocking the
     * event loop.
     *
     * @param plantId     ID of the plant to update
     * @param lastWatered Date when the plant was last watered
     * @return Mono containing updated plant data
     */
    private Mono<PlantDTO> updateWateringDate(long plantId, LocalDate lastWatered) {
        return Mono.fromCallable(() -> plantService.waterPlant(plantId, lastWatered))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
