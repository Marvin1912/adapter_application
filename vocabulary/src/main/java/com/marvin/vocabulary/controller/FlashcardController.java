package com.marvin.vocabulary.controller;

import com.generated.deepl.ApiClient;
import com.generated.deepl.api.TranslateTextApi;
import com.generated.deepl.model.SourceLanguageText;
import com.generated.deepl.model.TargetLanguageText;
import com.generated.deepl.model.TranslateTextRequest;
import com.marvin.vocabulary.dictionaryapi.DictionaryClient;
import com.marvin.vocabulary.dto.DictionaryEntry;
import com.marvin.vocabulary.dto.Flashcard;
import com.marvin.vocabulary.dto.Translation;
import com.marvin.vocabulary.model.FlashcardEntity;
import com.marvin.vocabulary.service.FlashcardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
@RestController
@RequestMapping("/vocabulary")
public class FlashcardController {

    private final String url;
    private final String apiKey;
    private final DictionaryClient dictionaryClient;
    private final FlashcardService flashcardService;
    private final TranslateTextApi translateTextApi;

    public FlashcardController(
            @Value("${vocabulary.deepl.url:https://api-free.deepl.com}") String url,
            @Value("${vocabulary.deepl.api-key:}") String apiKey,
            DictionaryClient dictionaryClient,
            FlashcardService flashcardService,
            TranslateTextApi translateTextApi
    ) {
        this.url = url;
        this.apiKey = apiKey;
        this.dictionaryClient = dictionaryClient;
        this.flashcardService = flashcardService;
        this.translateTextApi = translateTextApi;
    }

    private static <T> Mono<T> boundElastic(Callable<T> callable) {
        return Mono.fromCallable(callable).subscribeOn(Schedulers.boundedElastic());
    }

    private static ResponseEntity<Map<String, String>> logException(Throwable throwable, String message) {
        log.error("", throwable);
        return ResponseEntity.internalServerError().body(
                Map.of(
                        "type", throwable.getClass().getSimpleName(),
                        "message", message
                )
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        return logException(ex, ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleWebClientResponseException(WebClientResponseException ex) {
        return logException(ex, ex.getResponseBodyAsString());
    }

    @GetMapping("/words/{word}")
    public Mono<List<DictionaryEntry>> getWord(@PathVariable String word) {
        return dictionaryClient.getWord(word);
    }

    @GetMapping("/flashcards/{id}")
    public Mono<ResponseEntity<Flashcard>> getFlashcard(@PathVariable int id) {
        final FlashcardEntity flashcardEntity = flashcardService.get(id);
        return flashcardEntity == null
                ? Mono.just(ResponseEntity.notFound().build())
                : Mono.just(flashcardEntity)
                        .map(e -> new Flashcard(e.getId(), e.getDeck(), e.getAnkiId(), e.getFront(), e.getBack(), e.getDescription(), e.isUpdated()))
                        .map(ResponseEntity::ok);
    }

    @GetMapping("/flashcards")
    public Flux<Flashcard> getFlashcards(
            @RequestParam(required = false) String missing,
            @RequestParam(required = false) Boolean updated
    ) {
        return Flux.fromIterable(flashcardService.get(missing, updated))
                .map(e -> new Flashcard(e.getId(), e.getDeck(), e.getAnkiId(), e.getFront(), e.getBack(), e.getDescription(), e.isUpdated()));
    }

    @GetMapping("/flashcards/file")
    public Mono<ResponseEntity<byte[]>> getFile() {
        try {
            final byte[] file = flashcardService.getFile();
            final ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                    .header("filename", "Standard.csv")
                    .contentLength(file.length)
                    .body(file);
            return Mono.just(responseEntity);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @PostMapping("/flashcards")
    public Mono<ResponseEntity<Void>> addFlashcard(@RequestBody Mono<Flashcard> flashcard) {
        return flashcard.map(f -> new FlashcardEntity(null, f.deck(), f.ankiId(), f.front(), f.back(), f.description(), f.updated()))
                .flatMap(f -> boundElastic(() -> flashcardService.save(f)))
                .map(r -> ResponseEntity.created(URI.create("/flashcards/" + r.getId())).build());
    }

    @PutMapping("/flashcards")
    public Mono<ResponseEntity<Void>> updateFlashcard(@RequestBody Mono<Flashcard> flashcard) {
        return flashcard
                .map(f -> new FlashcardEntity(f.id(), f.deck(), f.ankiId(), f.front(), f.back(), f.description(), f.updated()))
                .flatMap(f -> boundElastic(() -> flashcardService.update(f)))
                .map(integer -> ResponseEntity.noContent().build());
    }

    @PutMapping("/flashcards/file")
    public Mono<ResponseEntity<Void>> updateFlashcards(@RequestPart(value = "file") Mono<FilePart> file) {
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
                .flatMap((fileBytes) -> boundElastic(() -> flashcardService.importFlashcards(fileBytes)))
                .onErrorResume(e -> {
                    log.error("", e);
                    return Mono.just(0);
                })
                .map(bytes -> ResponseEntity.noContent().build());
    }

    @GetMapping("/flashcards/translations")
    public Flux<Translation> getTranslation(
            @RequestParam String word,
            @RequestParam String context,
            @RequestParam(defaultValue = "EN") SourceLanguageText sourceLanguage,
            @RequestParam(defaultValue = "DE") TargetLanguageText targetLanguage
    ) {

        final TranslateTextRequest translateTextRequest = new TranslateTextRequest();
        translateTextRequest.setText(List.of(word));
        translateTextRequest.setContext(context);
        translateTextRequest.setSourceLang(sourceLanguage);
        translateTextRequest.setTargetLang(targetLanguage);

        final ApiClient apiClient = translateTextApi.getApiClient();
        apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "DeepL-Auth-Key " + apiKey);
        apiClient.setBasePath(url);

        return translateTextApi.translateText(translateTextRequest)
                .flatMapMany(t -> {
                    final var translations = t.getTranslations();
                    return translations == null || translations.isEmpty()
                            ? Flux.empty()
                            : Flux.fromIterable(translations).map(it -> new Translation(it.getText()));
                });
    }

}
