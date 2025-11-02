package com.marvin.vocabulary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.marvin.vocabulary.dictionaryapi.DictionaryClient;
import com.marvin.vocabulary.dto.DictionaryEntry;
import com.marvin.vocabulary.dto.Flashcard;
import com.marvin.vocabulary.model.FlashcardEntity;
import com.marvin.vocabulary.service.FlashcardService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class FlashcardControllerTest {

    @Mock
    private DictionaryClient dictionaryClient;

    @Mock
    private FlashcardService flashcardService;

    @InjectMocks
    private FlashcardController flashcardController;

    private WebTestClient webTestClient;

    private FlashcardEntity testFlashcardEntity;
    private Flashcard testFlashcard;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(flashcardController).build();

        testFlashcardEntity = new FlashcardEntity(
                1,
                "test-deck",
                "anki-123",
                "test front",
                "test back",
                "test description",
                true
        );

        testFlashcard = new Flashcard(
                1,
                "test-deck",
                "anki-123",
                "test front",
                "test back",
                "test description",
                true
        );
    }

    @Test
    void getWord_ShouldReturnDictionaryEntries() {
        List<DictionaryEntry> expectedEntries = List.of(new DictionaryEntry(
                "hello",
                "/həˈloʊ/",
                List.of(),
                List.of(),
                null,
                List.of("https://example.com/hello")
        ));

        when(dictionaryClient.getWord("hello")).thenReturn(Mono.just(expectedEntries));

        webTestClient.get()
                .uri("/vocabulary/words/hello")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DictionaryEntry.class)
                .isEqualTo(expectedEntries);
    }

    @Test
    void getFlashcard_WhenFlashcardExists_ShouldReturnFlashcard() {
        when(flashcardService.get(1)).thenReturn(testFlashcardEntity);

        webTestClient.get()
                .uri("/vocabulary/flashcards/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Flashcard.class)
                .isEqualTo(testFlashcard);
    }

    @Test
    void getFlashcard_WhenFlashcardNotExists_ShouldReturnNotFound() {
        when(flashcardService.get(999)).thenReturn(null);

        webTestClient.get()
                .uri("/vocabulary/flashcards/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getFlashcards_WithoutParams_ShouldReturnAllFlashcards() {
        List<FlashcardEntity> flashcardEntities = List.of(testFlashcardEntity);
        List<Flashcard> expectedFlashcards = List.of(testFlashcard);

        when(flashcardService.get(null, null)).thenReturn(flashcardEntities);

        webTestClient.get()
                .uri("/vocabulary/flashcards")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Flashcard.class)
                .isEqualTo(expectedFlashcards);
    }

    @Test
    void getFlashcards_WithMissingAnkiIdParam_ShouldReturnFlashcardsWithoutAnkiId() {
        List<FlashcardEntity> flashcardEntities = List.of(testFlashcardEntity);
        List<Flashcard> expectedFlashcards = List.of(testFlashcard);

        when(flashcardService.get("ankiId", null)).thenReturn(flashcardEntities);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/vocabulary/flashcards")
                        .queryParam("missing", "ankiId")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Flashcard.class)
                .isEqualTo(expectedFlashcards);
    }

    @Test
    void getFlashcards_WithUpdatedParam_ShouldReturnUpdatedFlashcards() {
        List<FlashcardEntity> flashcardEntities = List.of(testFlashcardEntity);
        List<Flashcard> expectedFlashcards = List.of(testFlashcard);

        when(flashcardService.get(null, true)).thenReturn(flashcardEntities);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/vocabulary/flashcards")
                        .queryParam("updated", "true")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Flashcard.class)
                .isEqualTo(expectedFlashcards);
    }

    @Test
    void getFile_ShouldReturnCsvFile() throws Exception {
        byte[] expectedFileContent = "#separator:tab\n#html:false\n#guid column:1\ntest-content".getBytes(
                StandardCharsets.UTF_8);

        when(flashcardService.getFile()).thenReturn(expectedFileContent);

        webTestClient.get()
                .uri("/vocabulary/flashcards/file")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/csv")
                .expectHeader().valueEquals("Content-Disposition", "attachment")
                .expectHeader().valueEquals("filename", "Standard.csv")
                .expectHeader()
                .valueEquals("Content-Length", String.valueOf(expectedFileContent.length))
                .expectBody(byte[].class)
                .isEqualTo(expectedFileContent);
    }

    @Test
    void getFile_WhenExceptionThrown_ShouldReturnInternalServerError() throws Exception {
        when(flashcardService.getFile()).thenThrow(new RuntimeException("File generation failed"));

        webTestClient.get()
                .uri("/vocabulary/flashcards/file")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.type").isEqualTo("RuntimeException")
                .jsonPath("$.message").isEqualTo("File generation failed");
    }

    @Test
    void addFlashcard_ShouldCreateFlashcardAndReturnLocation() {
        Flashcard newFlashcard = new Flashcard(
                null,
                "new-deck",
                "anki-456",
                "new front",
                "new back",
                "new description",
                false
        );

        FlashcardEntity savedEntity = new FlashcardEntity(
                2,
                "new-deck",
                "anki-456",
                "new front",
                "new back",
                "new description",
                false
        );

        when(flashcardService.save(any(FlashcardEntity.class))).thenReturn(savedEntity);

        webTestClient.post()
                .uri("/vocabulary/flashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newFlashcard)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/flashcards/2")
                .expectBody().isEmpty();
    }

    @Test
    void updateFlashcard_ShouldUpdateFlashcardAndReturnNoContent() {
        when(flashcardService.update(any(FlashcardEntity.class))).thenReturn(1);

        webTestClient.put()
                .uri("/vocabulary/flashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testFlashcard)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void updateFlashcards_WithValidFile_ShouldImportAndUpdate() {
        String csvContent = "deck1\tanki-123\tfront1\tback1\tdescription1\n";
        byte[] fileBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        lenient().when(flashcardService.importFlashcards(fileBytes)).thenReturn(1);

        FilePart filePart = createMockFilePart("test.csv", fileBytes);

        webTestClient.put()
                .uri("/vocabulary/flashcards/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", filePart))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void updateFlashcards_WithEmptyFile_ShouldHandleGracefully() {
        FilePart filePart = createMockFilePart("empty.csv", new byte[0]);

        lenient().when(flashcardService.importFlashcards(new byte[0])).thenReturn(0);

        webTestClient.put()
                .uri("/vocabulary/flashcards/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", filePart))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void updateFlashcards_WhenExceptionThrown_ShouldHandleError() {
        String csvContent = "invalid content";
        byte[] fileBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        lenient().when(flashcardService.importFlashcards(fileBytes))
                .thenThrow(new RuntimeException("Import failed"));

        FilePart filePart = createMockFilePart("test.csv", fileBytes);

        webTestClient.put()
                .uri("/vocabulary/flashcards/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", filePart))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void handleException_ShouldReturnErrorResponse() {
        // Force an exception by mocking the service to throw an exception
        when(dictionaryClient.getWord(any())).thenThrow(new RuntimeException("Test exception"));

        webTestClient.get()
                .uri("/vocabulary/words/test")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(Map.class)
                .value(response -> {
                    assert response.containsKey("type");
                    assert response.containsKey("message");
                });
    }

    private FilePart createMockFilePart(String filename, byte[] content) {
        return new FilePart() {
            @Override
            public String filename() {
                return filename;
            }

            @Override
            public String name() {
                return "file";
            }

            @Override
            public Flux<DataBuffer> content() {
                DataBuffer buffer = new DefaultDataBufferFactory().wrap(content);
                return Flux.just(buffer);
            }

            @Override
            public org.springframework.http.HttpHeaders headers() {
                return org.springframework.http.HttpHeaders.EMPTY;
            }

            @Override
            public reactor.core.publisher.Mono<Void> transferTo(java.nio.file.Path dest) {
                return reactor.core.publisher.Mono.empty();
            }
        };
    }
}
