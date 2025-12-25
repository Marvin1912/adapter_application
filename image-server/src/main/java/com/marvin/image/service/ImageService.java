package com.marvin.image.service;

import com.marvin.image.entity.Image;
import com.marvin.image.repository.ImageRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public Mono<UUID> saveImage(byte[] rawImage, String contentType) {

        final Image image = new Image(rawImage, contentType);

        return Mono.fromCallable(() -> imageRepository.save(image))
                .subscribeOn(Schedulers.boundedElastic())
                .map(Image::getId);
    }

    public Mono<byte[]> getImage(UUID id) {
        return Mono.fromCallable(() -> imageRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(image -> image.map(value -> Mono.just(value.getContent()))
                        .orElseGet(Mono::empty));
    }
}
