# InfluxDB Export REST API

## Overview
This document describes the REST endpoint for triggering InfluxDB data exports from the importer module.

## Endpoint
**POST** `/export/influxdb`

**Content-Type**: `application/json`
**Response**: `application/json`

## Request Body (InfluxExportRequest)

```json
{
  "buckets": ["SYSTEM_METRICS", "SENSOR_DATA"], // Optional - exports all if null/empty
  "startTime": "2024-01-01T00:00:00Z",          // Optional - ISO-8601 format
  "endTime": "2024-01-02T00:00:00Z"             // Optional - ISO-8601 format
}
```

### Parameters
- **buckets** (optional): List of bucket names to export. If null or empty, all enabled buckets will be exported.
  - Valid values: `SYSTEM_METRICS`, `SENSOR_DATA`, `SENSOR_DATA_AGGREGATED`, `COSTS`
- **startTime** (optional): Start time for filtering data in ISO-8601 format. If not provided, defaults to 24 hours ago.
- **endTime** (optional): End time for filtering data in ISO-8601 format. If not provided, defaults to current time.

## Response (InfluxExportResponse)

### Success Response
```json
{
  "success": true,
  "message": "InfluxDB buckets exported successfully",
  "exportedFiles": [
    "system_metrics_20240109_143022.json",
    "sensor_data_20240109_143022.json"
  ],
  "timestamp": "2024-01-09T14:30:22.123Z"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Invalid bucket name: INVALID_BUCKET",
  "timestamp": "2024-01-09T14:30:22.123Z"
}
```

## Usage Examples

### 1. Export All Buckets
```bash
curl -X POST http://localhost:8080/export/influxdb \
  -H "Content-Type: application/json" \
  -d '{}'
```

### 2. Export Specific Buckets
```bash
curl -X POST http://localhost:8080/export/influxdb \
  -H "Content-Type: application/json" \
  -d '{"buckets": ["SYSTEM_METRICS", "SENSOR_DATA"]}'
```

### 3. Export with Time Range
```bash
curl -X POST http://localhost:8080/export/influxdb \
  -H "Content-Type: application/json" \
  -d '{
    "buckets": ["SYSTEM_METRICS"],
    "startTime": "2024-01-01T00:00:00Z",
    "endTime": "2024-01-02T00:00:00Z"
  }'
```

### 4. Export All Buckets with Custom Time Range
```bash
curl -X POST http://localhost:8080/export/influxdb \
  -H "Content-Type: application/json" \
  -d '{
    "startTime": "2024-01-08T00:00:00Z",
    "endTime": "2024-01-09T00:00:00Z"
  }'
```

## Available Buckets

| Bucket Name | Description |
|-------------|-------------|
| `SYSTEM_METRICS` | System performance metrics (CPU, memory, disk, network) |
| `SENSOR_DATA` | Real-time IoT sensor data |
| `SENSOR_DATA_AGGREGATED` | 30-minute aggregated sensor data |
| `COSTS` | Cost-related metrics |

## Output Files
The export generates timestamped JSON files in the configured export directory:
- `system_metrics_YYYYMMDD_HHMMSS.json`
- `sensor_data_YYYYMMDD_HHMMSS.json`
- `sensor_data_30m_YYYYMMDD_HHMMSS.json`
- `costs_YYYYMMDD_HHMMSS.json`

## Error Handling
- **400 Bad Request**: Invalid bucket names or malformed time format
- **500 Internal Server Error**: Export process failures

The endpoint integrates with the existing InfluxExporter service and maintains the JSON Lines format for consistency with the existing export system.