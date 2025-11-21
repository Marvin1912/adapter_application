# Sensor Data Integration with GenericPojoImporter

This module provides integration between the `@importer` module and the `GenericPojoImporter` for processing sensor data in JSON format.

## Architecture

The integration follows the existing pattern in the codebase:

```
JSON File → GenericFileReader → SensorDataFileTypeHandler → SensorDataImportService → SensorDataImport → GenericPojoImporter → InfluxDB
```

## Components

### 1. SensorData POJO (`SensorData.java`)
- **Location**: `dto/SensorData.java`
- **Purpose**: Java representation of sensor data matching the provided JSON structure
- **Features**:
  - `@Measurement(name = "%")` for dynamic measurements
  - Proper `@Column` annotations for InfluxDB
  - Support for fields, tags, and metadata
  - Helper method `getPrimaryValue()` for extracting the main sensor value

### 2. SensorDataImport (`SensorDataImport.java`)
- **Location**: `service/SensorDataImport.java`
- **Purpose**: Wrapper around `GenericPojoImporter` for sensor data
- **Features**:
  - Configurable bucket and organization settings
  - Single and batch import methods
  - Uses `GenericPojoImporter<SensorData>` internally

### 3. SensorDataImportService (`SensorDataImportService.java`)
- **Location**: `importer/SensorDataImportService.java`
- **Purpose**: Implements `ImportService<SensorData>` interface
- **Features**:
  - Bridges the importer module with InfluxDB integration
  - Handles error logging and validation
  - Forwards data to `SensorDataImport`

### 4. SensorDataFileTypeHandler (`SensorDataFileTypeHandler.java`)
- **Location**: `importer/SensorDataFileTypeHandler.java`
- **Purpose**: Implements `FileTypeHandler<SensorData>` interface
- **Features**:
  - Detects sensor files (contains "sensor" in filename)
  - JSON deserialization to `SensorData`
  - Integration with existing `GenericFileReader`

## Usage

### Basic Integration

```java
// Create components
SensorDataImport sensorDataImport = new SensorDataImport(influxDBClient);
SensorDataImportService importService = new SensorDataImportService(sensorDataImport);
SensorDataFileTypeHandler fileTypeHandler = new SensorDataFileTypeHandler(importService);

// Use with GenericFileReader
List<FileTypeHandler<?>> handlers = List.of(fileTypeHandler);
genericFileReader.processFile(path, handlers);
```

### JSON Format Example

```json
{
    "measurement":"%",
    "entityId":"lumi_lumi_weather_luftfeuchtigkeit",
    "friendlyName":"Badezimmer",
    "timestamp":"2025-10-11T14:00:00Z",
    "fields":{"value":74.5},
    "tags":{
        "result":"_result",
        "friendly_name":"Badezimmer",
        "domain":"sensor",
        "source":"HA",
        "entity_id":"lumi_lumi_weather_luftfeuchtigkeit"
    },
    "temperatureSensor":false,
    "humiditySensor":true
}
```

### Direct Usage

```java
// Parse JSON to SensorData
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
SensorData sensorData = mapper.readValue(jsonString, SensorData.class);

// Import using the service
sensorDataImport.importSensorData(sensorData);

// Or batch import
List<SensorData> batch = List.of(sensorData1, sensorData2);
sensorDataImport.importSensorDataBatch(batch);
```

## Configuration

The integration uses the following InfluxDB configuration:

- **Bucket**: `sensors` (configurable)
- **Organization**: `wildfly_domain` (configurable)
- **Write Precision**: `S` (seconds)

## Spring Components

All main components are Spring `@Component` or `@Service` annotated and can be autowired:

```java
@Autowired
private SensorDataImportService sensorDataImportService;

@Autowired
private SensorDataFileTypeHandler sensorDataFileTypeHandler;
```

## Testing

See `SensorDataIntegrationTest.java` for comprehensive tests including:

- JSON deserialization validation
- FileTypeHandler functionality
- Integration flow testing
- Example usage with the provided JSON data

## Benefits

1. **Modular Design**: Follows existing architecture patterns
2. **Type Safety**: Uses generic types and proper POJO mapping
3. **Error Handling**: Comprehensive logging and validation
4. **Batch Support**: Efficient batch processing capabilities
5. **Spring Integration**: Ready for dependency injection
6. **Extensible**: Easy to extend for additional sensor types

## Dependencies

- `influxdb-client-java` for InfluxDB operations
- `jackson` for JSON processing
- `jackson-datatype-jsr310` for Java 8 time support
- Spring Boot annotations
- Existing `@influxdb` module interfaces