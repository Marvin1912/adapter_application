# Plan: InfluxDB User Buckets Export Infrastructure

## Overview
Create export infrastructure in the `exporter` module to export data from InfluxDB **user buckets only**. The InfluxDB Java client dependency will be added to `influxdb/build.gradle` to make it transitive to `exporter/`.

## User Buckets to Export
Based on `buckets-documentation.md`, focus on these 4 user buckets:
1. `system_metrics`: System monitoring data (CPU, memory, performance)
2. `costs`: Cost-related metrics (currently empty, but structured for future use)
3. `sensor_data`: Real-time IoT sensor data (humidity, energy monitoring)
4. `sensor_data_30m`: Aggregated sensor data (30-minute intervals)

## Implementation Phases

### Phase 1: Dependencies and Configuration ✅ COMPLETED
- [x] **1.1**: Add InfluxDB Java client dependency to `influxdb/build.gradle`
- [x] **1.2**: Add `:influxdb` module dependency to `exporter/build.gradle`
- [x] **1.3**: Extend `ExportConfig.java` with InfluxDB connection settings
- [x] **1.4**: Add properties for URL, token, organization, and export preferences

### Phase 2: Base Infrastructure
- [ ] **2.1**: Create `AbstractInfluxExport<T>` generic base class
- [ ] **2.2**: Create `InfluxQueryBuilder` utility class for Flux queries
- [ ] **2.3**: Create bucket-specific export DTOs and measurement mappings
- [ ] **2.4**: Handle different data types (system metrics, IoT sensors, aggregated data)

### Phase 3: User Bucket Export Services
- [ ] **3.1**: Create `SystemMetricsExportService` for `system_metrics` bucket
- [ ] **3.2**: Create `SensorDataExportService` for `sensor_data` bucket
- [ ] **3.3**: Create `SensorDataAggregatedExportService` for `sensor_data_30m` bucket
- [ ] **3.4**: Create `CostsExportService` for `costs` bucket (future preparation)

### Phase 4: Main Export Orchestrator
- [ ] **4.1**: Create `InfluxExporter.java` main service
- [ ] **4.2**: Implement bucket orchestration following existing `Exporter.java` patterns
- [ ] **4.3**: Add timestamped output file generation
- [ ] **4.4**: Support selective bucket exports

### Phase 5: Integration and File Output
- [ ] **5.1**: Integrate with existing `Exporter.java` workflow
- [ ] **5.2**: Extend `ExportFileWriter` for InfluxDB data formats if needed
- [ ] **5.3**: Maintain existing JSON Lines format as default
- [ ] **5.4**: Test integration with existing export system

## Expected Output Files
Exports will generate timestamped files:
- `system_metrics_YYYYMMDD_HHMMSS.json`
- `sensor_data_YYYYMMDD_HHMMSS.json`
- `sensor_data_30m_YYYYMMDD_HHMMSS.json`
- `costs_YYYYMMDD_HHMMSS.json`

## Data Focus Areas
Based on active buckets in documentation:
1. **System Metrics**: Active data from host `home-server` (CPU, memory)
2. **IoT Sensor Data**: Real-time data from Home Assistant (humidity, energy monitoring)
3. **Aggregated Sensor Data**: 30-minute averaged data for trend analysis
4. **Costs**: Prepared for future cost-related exports

## Architecture Notes
- Follow existing patterns from `influxdb` module (AbstractCostImport template pattern)
- Maintain consistency with `exporter` module patterns (Exporter.java factory pattern)
- Use InfluxDB Java SDK annotations (@Measurement, @Column) for type-safe data handling
- Implement proper error handling and logging following existing codebase standards
- Support JSON Lines format as default output format

## Dependencies
- InfluxDB Java client (com.influxdb:influxdb-client-java) - to be added to influxdb module
- Existing Jackson dependency for JSON serialization
- Existing Spring Boot configuration infrastructure

This plan focuses specifically on the 4 user buckets, excluding system buckets (`_tasks`, `_monitoring`), while maintaining architectural consistency with existing code patterns.