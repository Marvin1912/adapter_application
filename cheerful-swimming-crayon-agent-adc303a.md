# Component-Based Refactoring Plan for Export Module

## Current Structure Analysis

The current export module has the following organization:
```
/com/marvin/export/
├── Exporter.java (costs exporter)
├── InfluxExporter.java (time-series data exporter)
├── AbstractExporterBase.java (base class for exporters)
├── ExportConfig.java (configuration)
├── ExportFileWriter.java (file writing utility)
├── VocabularyExporter.java (vocabulary exporter)
└── /influxdb/
    ├── AbstractInfluxExport.java (base for InfluxDB exporters)
    ├── InfluxQueryBuilder.java (query building utility)
    ├── /dto/
    │   ├── AbstractInfluxData.java
    │   ├── SensorDataDTO.java
    │   └── SystemMetricsDTO.java
    ├── /handlers/
    │   └── DataTypeHandler.java
    ├── /mappings/
    │   └── MeasurementMappings.java
    └── /services/
        ├── TemperatureExportService.java
        ├── TemperatureAggregatedExportService.java
        ├── HumidityExportService.java
        ├── HumidityAggregatedExportService.java
        ├── PowerExportService.java
        └── PowerAggregatedExportService.java
```

## Identified Components

1. **Core Export Infrastructure**
   - AbstractExporterBase
   - ExportConfig
   - ExportFileWriter

2. **Cost Export Component**
   - Exporter
   - (handles daily costs, monthly costs, special costs, salaries)

3. **InfluxDB Export Component**
   - InfluxExporter
   - AbstractInfluxExport
   - InfluxQueryBuilder
   - All supporting classes (dto, handlers, mappings, services)

4. **Vocabulary Export Component**
   - VocabularyExporter

## Proposed Component-Based Structure

```
/com/marvin/export/
├── core/                          # Core export infrastructure
│   ├── AbstractExporterBase.java
│   ├── ExportConfig.java
│   └── ExportFileWriter.java
├── costs/                         # Cost export component
│   └── CostExporter.java (renamed from Exporter.java)
├── influxdb/                      # InfluxDB export component (keep as is)
│   ├── InfluxExporter.java
│   ├── AbstractInfluxExport.java
│   ├── InfluxQueryBuilder.java
│   ├── dto/
│   ├── handlers/
│   ├── mappings/
│   └── services/
└── vocabulary/                    # Vocabulary export component
    └── VocabularyExporter.java
```

## Refactoring Steps

1. Create new component directories:
   - `/app/workspace/adapter_application/exporter/src/main/java/com/marvin/export/core/`
   - `/app/workspace/adapter_application/exporter/src/main/java/com/marvin/export/costs/`
   - `/app/workspace/adapter_application/exporter/src/main/java/com/marvin/export/vocabulary/`

2. Move core infrastructure files to `core/` package:
   - Move `AbstractExporterBase.java` → `core/AbstractExporterBase.java`
   - Move `ExportConfig.java` → `core/ExportConfig.java`
   - Move `ExportFileWriter.java` → `core/ExportFileWriter.java`
   - Update package declarations

3. Move cost export component:
   - Move `Exporter.java` → `costs/CostExporter.java`
   - Update package declaration
   - Update class name from `Exporter` to `CostExporter`

4. Move vocabulary export component:
   - Move `vocabulary/VocabularyExporter.java` → `vocabulary/VocabularyExporter.java` (already in correct location)
   - Update import statements in affected files

5. Update all import statements in affected files:
   - InfluxExporter imports
   - All service imports in influxdb/services/
   - Any other classes that import moved files

6. Verify Spring component scanning still works correctly (package-level scanning should find all components)

## Benefits

1. Clear separation of concerns
2. Easier to find and maintain related code
3. Scalable structure for adding new export types
4. Minimal changes required - just moving files and updating packages
5. No functional changes to the code