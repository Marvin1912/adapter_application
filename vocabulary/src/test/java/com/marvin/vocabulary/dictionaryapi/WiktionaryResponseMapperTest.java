package com.marvin.vocabulary.dictionaryapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.marvin.vocabulary.dto.DictionaryEntry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WiktionaryResponseMapperTest {

  private HtmlCleaner htmlCleaner;
  private WiktionaryResponseMapper responseMapper;

  @BeforeEach
  void setUp() {
    htmlCleaner = new HtmlCleaner();
    responseMapper = new WiktionaryResponseMapper(htmlCleaner);
  }

  @Test
  void mapToDictionaryEntriesWithEmptyDefinitionsShouldFilterThemOut() {
    // Create test data with empty definitions
    final Map<String, List<WiktionaryResponseMapper.WiktionaryDefinition>> response = Map.of("en",
        List.of(
            createDefinitionWithMixedContent()
        ));

    final List<DictionaryEntry> entries = responseMapper.mapToDictionaryEntries("test", response);

    assertEquals(1, entries.size());
    final DictionaryEntry entry = entries.get(0);
    assertEquals(1, entry.meanings().size());

    // Should only have 4 valid definitions (not 9 total with empty ones)
    assertEquals(4, entry.meanings().get(0).definitions().size());

    // Verify no empty definition objects
    entry.meanings().get(0).definitions().forEach(word -> {
      assertNotNull(word.definition());
      assertFalse(word.definition().trim().isEmpty());
    });
  }

  private WiktionaryResponseMapper.WiktionaryDefinition createDefinitionWithMixedContent() {
    final WiktionaryResponseMapper.WiktionaryDefinition definition =
        new WiktionaryResponseMapper.WiktionaryDefinition();
    definition.setPartOfSpeech("Noun");

    // Mix of valid and empty definitions
    final List<WiktionaryResponseMapper.WiktionaryDefinition.Definition> definitions = List.of(
        createDefinition("A valid definition", null),
        createDefinition("", null), // empty
        createDefinition("   ", null), // whitespace only
        createDefinition("Another valid definition", "Valid example"),
        createDefinition("", ""), // empty with empty example
        createDefinition("<b>HTML</b> definition", "<i>HTML</i> example"),
        createDefinition(null, null), // null definition
        createDefinition("Third valid definition", null),
        createDefinition("", "Example without definition") // example but no definition
    );

    definition.setDefinitions(definitions);
    return definition;
  }

  private WiktionaryResponseMapper.WiktionaryDefinition.Definition createDefinition(
      String definition, String example) {
    final WiktionaryResponseMapper.WiktionaryDefinition.Definition def =
        new WiktionaryResponseMapper.WiktionaryDefinition.Definition();
    def.setDefinition(definition);
    if (example != null) {
      def.setExamples(List.of(example));
    }
    return def;
  }
}
