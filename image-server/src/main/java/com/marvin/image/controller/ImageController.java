package com.marvin.image.controller;

import com.marvin.image.service.ImageService;
import java.net.URI;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(path = "/images")
public class ImageController {

  private final ImageService imageService;

  public ImageController(ImageService imageService) {
    this.imageService = imageService;
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
          log.error("", e);
          return Mono.just(new byte[0]);
        });
  }

  @PostMapping
  public Mono<ResponseEntity<Object>> createImage(
      @RequestPart("image") Mono<FilePart> image,
      @RequestParam(name = "content-type") String contentType
  ) {
    return getFileAsByteArray(image)
        .doOnError(throwable -> log.error("", throwable))
        .flatMap(rawBytes ->
            imageService.saveImage(rawBytes, contentType)
                .map(uuid -> ResponseEntity.created(
                    URI.create("/images/%s".formatted(uuid))).build())
        );
  }

  @GetMapping(path = "/{uuid}")
  public Mono<ResponseEntity<byte[]>> getImage(@PathVariable UUID uuid) {
    return imageService.getImage(uuid)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

}
