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

## Architecture

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
