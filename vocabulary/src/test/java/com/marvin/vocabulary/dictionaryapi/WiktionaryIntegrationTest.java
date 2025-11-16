package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.DictionaryEntry;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class WiktionaryIntegrationTest {

    @Test
    void getWordWithValidWordShouldReturnEntriesFromWiktionary() {
        final HtmlCleaner htmlCleaner = new HtmlCleaner();
        final WiktionaryResponseMapper responseMapper = new WiktionaryResponseMapper(htmlCleaner);
        final DictionaryClient dictionaryClient = new DictionaryClient(responseMapper);

        final String testWord = "hello";

        StepVerifier.create(dictionaryClient.getWord(testWord))
                .expectNextMatches(entries -> {
                    if (entries.isEmpty()) {
                        return false;
                    }

                    final DictionaryEntry entry = entries.get(0);
                    return entry.word().equals(testWord)
                            && !entry.meanings().isEmpty()
                            && entry.meanings().get(0).partOfSpeech() != null
                            && !entry.meanings().get(0).definitions().isEmpty()
                            && entry.meanings().get(0).definitions().get(0).definition() != null;
                })
                .verifyComplete();
    }
}
