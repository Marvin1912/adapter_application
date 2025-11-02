package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.DictionaryEntry;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class WiktionaryIntegrationTest {

  @Test
  void getWord_WithValidWord_ShouldReturnEntriesFromWiktionary() {
    HtmlCleaner htmlCleaner = new HtmlCleaner();
    WiktionaryResponseMapper responseMapper = new WiktionaryResponseMapper(htmlCleaner);
    DictionaryClient dictionaryClient = new DictionaryClient(responseMapper);

    String testWord = "hello";

    StepVerifier.create(dictionaryClient.getWord(testWord))
        .expectNextMatches(entries -> {
          if (entries.isEmpty()) {
            return false;
          }

          DictionaryEntry entry = entries.get(0);
          return entry.word().equals(testWord) &&
              !entry.meanings().isEmpty() &&
              entry.meanings().get(0).partOfSpeech() != null &&
              !entry.meanings().get(0).definitions().isEmpty() &&
              entry.meanings().get(0).definitions().get(0).definition() != null;
        })
        .verifyComplete();
  }
}
