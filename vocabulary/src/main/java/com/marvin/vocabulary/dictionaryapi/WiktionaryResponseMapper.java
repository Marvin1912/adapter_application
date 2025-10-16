package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WiktionaryResponseMapper {

    private final HtmlCleaner htmlCleaner;

    public WiktionaryResponseMapper(HtmlCleaner htmlCleaner) {
        this.htmlCleaner = htmlCleaner;
    }

    public List<DictionaryEntry> mapToDictionaryEntries(String word, Map<String, List<WiktionaryDefinition>> wiktionaryResponse) {
        if (wiktionaryResponse == null || !wiktionaryResponse.containsKey("en")) {
            return Collections.emptyList();
        }

        List<WiktionaryDefinition> englishDefinitions = wiktionaryResponse.get("en");
        if (englishDefinitions == null || englishDefinitions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Meaning> meanings = englishDefinitions.stream()
                .map(this::mapToMeaning)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (meanings.isEmpty()) {
            return Collections.emptyList();
        }

        DictionaryEntry entry = new DictionaryEntry(
                word,
                null, // phonetic - not provided by Wiktionary API
                Collections.emptyList(), // phonetics - not provided by Wiktionary API
                meanings,
                null, // license - not provided by Wiktionary API
                Collections.singletonList("https://en.wiktionary.org/wiki/" + word) // source URL
        );

        return Collections.singletonList(entry);
    }

    private Meaning mapToMeaning(WiktionaryDefinition wiktionaryDefinition) {
        if (wiktionaryDefinition == null || wiktionaryDefinition.getDefinitions() == null) {
            return null;
        }

        List<Word> definitions = wiktionaryDefinition.getDefinitions().stream()
                .map(this::mapToWord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (definitions.isEmpty()) {
            return null;
        }

        return new Meaning(
                wiktionaryDefinition.getPartOfSpeech(),
                definitions,
                Collections.emptyList(), // synonyms - not provided by Wiktionary API
                Collections.emptyList()  // antonyms - not provided by Wiktionary API
        );
    }

    private Word mapToWord(WiktionaryDefinition.Definition definition) {
        if (definition == null || definition.getDefinition() == null) {
            return null;
        }

        String cleanedDefinition = htmlCleaner.cleanHtml(definition.getDefinition());
        String example = definition.getExamples() != null && !definition.getExamples().isEmpty()
                ? htmlCleaner.cleanHtml(definition.getExamples().get(0))
                : null;

        return new Word(
                cleanedDefinition,
                example,
                Collections.emptyList(), // synonyms - not provided by Wiktionary API
                Collections.emptyList()  // antonyms - not provided by Wiktionary API
        );
    }

    // Inner classes to represent Wiktionary API response structure
    public static class WiktionaryDefinition {
        private String partOfSpeech;
        private String language;
        private List<Definition> definitions;

        public String getPartOfSpeech() {
            return partOfSpeech;
        }

        public void setPartOfSpeech(String partOfSpeech) {
            this.partOfSpeech = partOfSpeech;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public List<Definition> getDefinitions() {
            return definitions;
        }

        public void setDefinitions(List<Definition> definitions) {
            this.definitions = definitions;
        }

        public static class Definition {
            private String definition;
            private List<String> examples;

            public String getDefinition() {
                return definition;
            }

            public void setDefinition(String definition) {
                this.definition = definition;
            }

            public List<String> getExamples() {
                return examples;
            }

            public void setExamples(List<String> examples) {
                this.examples = examples;
            }
        }
    }
}