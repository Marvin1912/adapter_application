package com.marvin.vocabulary.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.marvin.vocabulary.dto.Flashcard;
import com.marvin.vocabulary.dto.FlashcardCsvDTO;
import com.marvin.vocabulary.model.FlashcardEntity;
import com.marvin.vocabulary.repository.FlashcardRepository;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FlashcardService {

    private final CsvSchema schema;
    private final FlashcardRepository flashcardRepository;

    public FlashcardService(FlashcardRepository flashcardRepository) {
        this.flashcardRepository = flashcardRepository;

        this.schema = CsvSchema.builder()
                .setColumnSeparator('\t')
                .setUseHeader(false)
                .setAllowComments(true)
                .addColumn("guid")
                .addColumn("deck")
                .addColumn("front")
                .addColumn("back")
                .addColumn("description")
                .disableQuoteChar()
                .build();
    }

    public FlashcardEntity get(int id) {
        return flashcardRepository.findById(id).orElse(null);
    }

    public List<FlashcardEntity> get(String missing, Boolean updated) {
        if (missing == null && updated == null) {
            return flashcardRepository.findAll();
        }

        if ("ankiId".equals(missing)) {
            return flashcardRepository.findByAnkiIdIsNull();
        }

        if (Boolean.TRUE.equals(updated)) {
            return flashcardRepository.findByUpdated(true);
        }

        return flashcardRepository.findAll();
    }

    public FlashcardEntity save(FlashcardEntity flashcard) {
        return flashcardRepository.save(flashcard);
    }

    @Transactional
    public Integer update(FlashcardEntity flashcard) {
        flashcardRepository.findById(flashcard.getId())
                .ifPresentOrElse(
                        f -> {
                            f.setFront(flashcard.getFront());
                            f.setBack(flashcard.getBack());
                            f.setDescription(flashcard.getDescription());
                            f.setAnkiId(flashcard.getAnkiId());
                            f.setUpdated(flashcard.isUpdated());
                        },
                        () -> {
                            throw new IllegalArgumentException(
                                    "No flashcard found with id: " + flashcard.getId());
                        }
                );
        return flashcard.getId();
    }

    @Transactional
    public Integer importFlashcards(byte[] fileBytes) {
        return importFile(fileBytes);
    }

    public byte[] getFile() throws Exception {

        final CsvMapper csvMapper = new CsvMapper();

        byte[] file;

        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                SequenceWriter sequenceWriter = csvMapper
                        .writerFor(FlashcardCsvDTO.class)
                        .with(schema)
                        .writeValues(byteArrayOutputStream)
        ) {

            byteArrayOutputStream.write("#separator:tab\n".getBytes(StandardCharsets.UTF_8));
            byteArrayOutputStream.write("#html:false\n".getBytes(StandardCharsets.UTF_8));
            byteArrayOutputStream.write("#guid column:1\n".getBytes(StandardCharsets.UTF_8));
            byteArrayOutputStream.flush();

            for (final FlashcardEntity flashcardEntity : flashcardRepository.findAll()) {
                sequenceWriter.write(new FlashcardCsvDTO(
                        flashcardEntity.getDeck(),
                        flashcardEntity.getAnkiId(),
                        flashcardEntity.getFront(),
                        flashcardEntity.getBack(),
                        flashcardEntity.getDescription()
                ));
            }

            file = byteArrayOutputStream.toByteArray();

        }

        return file;
    }

    private int importFile(byte[] fileBytes) {

        final Set<String> allAnkiIds = flashcardRepository.getAllAnkiIds();

        final AtomicInteger count = new AtomicInteger(0);
        try (MappingIterator<FlashcardCsvDTO> iterator = new CsvMapper()
                .readerFor(FlashcardCsvDTO.class)
                .with(schema)
                .readValues(fileBytes)
        ) {
            for (final FlashcardCsvDTO flashcardCsvDto : iterator.readAll()) {
                try {
                    importFlashcard(allAnkiIds, flashcardCsvDto, count);
                } catch (Exception e) {
                    log.error("Failed to import flashcard {}", flashcardCsvDto, e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return count.get();
    }

    private void importFlashcard(Set<String> allAnkiIds, FlashcardCsvDTO flashcardCsvDto,
            AtomicInteger count) {
        if (!allAnkiIds.contains(flashcardCsvDto.guid())) {
            flashcardRepository
                    .findByFrontAndBack(flashcardCsvDto.front(), flashcardCsvDto.back())
                    .ifPresentOrElse(
                            flashcard -> flashcard.setAnkiId(flashcardCsvDto.guid()),
                            () -> flashcardRepository.save(new FlashcardEntity(
                                            null,
                                            flashcardCsvDto.deck(),
                                            flashcardCsvDto.guid(),
                                            flashcardCsvDto.front(),
                                            flashcardCsvDto.back(),
                                            flashcardCsvDto.description(),
                                            false
                                    )
                            )
                    );
            count.incrementAndGet();
        }
    }

    public Stream<Flashcard> getAllFlashcardsForExport() {
        return flashcardRepository.findAll().stream()
                .map(entity -> new Flashcard(
                        entity.getId(),
                        entity.getDeck(),
                        entity.getAnkiId(),
                        entity.getFront(),
                        entity.getBack(),
                        entity.getDescription(),
                        entity.isUpdated()
                ));
    }

}
