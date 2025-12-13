# Plan: Extend VocabularyExporter to Export JSON Data

## Overview
Extend VocabularyExporter.java to export actual vocabulary flashcard data to JSON instead of placeholder text.

## Current State
- VocabularyExporter returns placeholder: "vocabulary data placeholder"
- Uses AbstractExporterBase with ExportFileWriter that handles JSON serialization
- FlashcardEntity and Flashcard DTO already exist
- FlashcardRepository available for data access

## Implementation Steps

### 1. Add Dependencies
- Inject FlashcardRepository into VocabularyExporter constructor
- Keep existing ExportConfig and ExportFileWriter parameters

### 2. Create Entity to DTO Mapper
- Add static mapper function: FlashcardEntity → Flashcard
- Use existing Flashcard record (JSON-serializable)
- Map all fields: id, deck, ankiId, front, back, description, updated

### 3. Update createVocabularyStream() Method
- Change return type from Stream<String> to Stream<Flashcard>
- Use repository.findAll().stream().map(MAPPER) pattern
- Follow same pattern as CostExporter

### 4. Verify ExportFileWriter Compatibility
- ExportFileWriter already handles generic types with Jackson
- No changes needed to exportData() method

## Files to Modify
1. `/app/workspace/adapter_application/exporter/src/main/java/com/marvin/export/vocabulary/VocabularyExporter.java`

## Dependencies Used (already exist)
- `com.marvin.vocabulary.repository.FlashcardRepository`
- `com.marvin.vocabulary.model.FlashcardEntity`
- `com.marvin.vocabulary.dto.Flashcard`

## Result
- JSON file with one flashcard object per line (JSON Lines format)
- All flashcard fields exported
- No changes to file naming or folder structure