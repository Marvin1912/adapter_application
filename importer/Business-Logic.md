# Cost Importer Business Logic

## Overview

The Cost Importer is responsible for processing JSON files containing financial cost data and importing them into the system. It follows a file-based batch processing approach where files are moved from an input directory to a processed directory after successful import.

## File Processing Workflow

### 1. File Discovery
- Files are discovered by walking the input directory (`${importer.in}`) one level deep
- Only regular files (not directories) are processed
- Files must match the naming pattern: `[file_type]_[YYYYMMDD]_[HHMMSS].json`

### 2. File Type Recognition
The importer supports four cost data types based on filename prefixes:

| File Type | Filename Pattern | DTO Class | Import Service |
|-----------|------------------|-----------|----------------|
| Daily Costs | `daily_costs_*.json` | `DailyCostDTO` | `DailyCostImportService` |
| Monthly Costs | `monthly_costs_*.json` | `MonthlyCostDTO` | `MonthlyCostImportService` |
| Salaries | `salaries_*.json` | `SalaryDTO` | `SalaryImportService` |
| Special Costs | `special_costs_*.json` | `SpecialCostDTO` | `SpecialCostImportService` |

### 3. File Processing
- Each file is processed line by line (JSON Lines format)
- Each line contains a single JSON object representing a cost record
- JSON lines are deserialized into appropriate DTOs using Jackson ObjectMapper
- Valid DTOs are passed to their respective import services for persistence

### 4. File Archival
- Successfully processed files are moved to the "done" directory (`${importer.done}`)
- Files are moved even if some individual lines fail to parse (best effort approach)
- Failed file movements are logged but don't prevent processing of other files

## Error Handling Strategy

### Filename Validation
- Files with invalid naming patterns are skipped with a warning log
- No processing attempt is made for files that don't match the expected pattern

### Line Processing Errors
- Individual JSON lines that fail to parse are logged as errors
- Processing continues with subsequent lines (best effort approach)
- Invalid lines don't prevent the entire file from being processed

### File Movement Errors
- Failed file movements are logged as errors
- Files remain in the input directory if archival fails
- This allows for manual intervention and retry

### Service Import Errors
- Import services handle their own error conditions
- The importer only logs errors from the file reading/parsing perspective

## Architecture (Post-Refactoring)

### Generic Components
- **GenericFileReader**: Handles file discovery, reading, and archival operations
- **FilePatternMatcher**: Validates filenames and extracts file types
- **FileArchiveService**: Manages file movement between directories
- **FileTypeHandler**: Strategy interface for data-type-specific processing

### Cost-Specific Handlers
Each cost type has its own handler implementing `FileTypeHandler<T>`:
- **DailyCostHandler**: Processes daily cost records
- **MonthlyCostHandler**: Processes monthly cost records
- **SalaryHandler**: Processes salary records
- **SpecialCostHandler**: Processes special cost records

### Integration Points
The CostImporter orchestrates the process by:
1. Walking the input directory
2. Delegating each file to GenericFileReader with appropriate handlers
3. Handlers perform JSON deserialization and forward to import services
4. Import services handle database/InfluxDB persistence

## Configuration Properties

| Property | Description | Example |
|----------|-------------|---------|
| `importer.in` | Input directory for unprocessed files | `/app/data/in` |
| `importer.done` | Directory for processed files | `/app/data/done` |

## File Format Examples

### Daily Costs Example (`daily_costs_20241121_143000.json`)
```json
{"date":"2024-11-21","amount":125.50,"category":"office_supplies","description":"Printer paper"}
{"date":"2024-11-21","amount":89.99,"category":"software","description":"License renewal"}
```

### Monthly Costs Example (`monthly_costs_20241101_000000.json`)
```json
{"month":"2024-11","amount":2500.00,"category":"rent","description":"Office rent payment"}
{"month":"2024-11","amount":450.00,"category":"utilities","description":"Monthly utilities"}
```

### Salaries Example (`salaries_20241115_120000.json`)
```json
{"employeeId":"EMP001","amount":5000.00,"month":"2024-11","department":"engineering"}
{"employeeId":"EMP002","amount":4500.00","month":"2024-11","department":"marketing"}
```

### Special Costs Example (`special_costs_20241110_150000.json`)
```json
{"date":"2024-11-10","amount":1500.00,"category":"equipment","description":"New laptop purchase","approvalId":"APP-123"}
{"date":"2024-11-15","amount":200.00,"category":"training","description":"Conference registration","approvalId":"APP-124"}
```

## Dependencies and Integration

### External Services
- **Import Services**: Each cost type has its own service for database/InfluxDB operations
- **ObjectMapper**: Jackson JSON library for deserialization
- **File System**: Local filesystem for file operations

### Data Flow
1. Files appear in input directory (external process)
2. CostImporter discovers and processes files
3. DTOs are created from JSON lines
4. Import services persist data to databases
5. Files are moved to processed directory

## Monitoring and Observability

### Logging Levels
- **INFO**: Successful file processing
- **WARN**: Filename pattern mismatches, missing handlers
- **ERROR**: File reading failures, JSON parsing errors, file movement failures

### Key Metrics (for future enhancement)
- Files processed per run
- Records imported per file type
- Parse failure rates
- File movement success/failure rates

## Extension Points

### Adding New Cost Types
To add support for new cost types:
1. Create new DTO class in `common` module
2. Implement new `ImportService` for the data type
3. Create new `FileTypeHandler` implementation
4. Register the handler in `CostImporter` constructor

### Alternative File Formats
The generic reader can be extended to support other formats by:
1. Modifying `FileTypeHandler.readValue()` method
2. Creating alternative line processing strategies
3. Adding format detection logic

## Performance Considerations

### File Size
- Processed line by line to handle large files efficiently
- Memory usage scales with individual record size, not file size

### Batch Processing
- Each file is processed independently
- Parallel processing of multiple files could be implemented in the future

### Error Recovery
- Failed file movements allow for manual retry
- Invalid lines are logged but don't stop processing