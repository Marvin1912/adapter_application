# InfluxDB Buckets Documentation

## Overview

This document provides comprehensive information about all buckets configured in the InfluxDB instance running at `192.168.178.29:8086`.

**InfluxDB Version**: v2.7.1 (OSS)
**Organization**: wildfly_domain (ID: d69dd12df095e39a)
**Created**: 2024-03-28T11:31:18.300865683Z

## Bucket Summary

| Bucket Name | Type | Retention Period | Created | Data Status | Labels |
|-------------|------|------------------|---------|-------------|--------|
| `_tasks` | System | 3 days | 2024-03-28 | Empty | 0 |
| `_monitoring` | System | 7 days | 2024-03-28 | Empty | 0 |
| `system_metrics` | User | Infinite | 2024-07-21 | Active | 0 |
| `costs` | User | Infinite | 2024-03-28 | Empty | 0 |
| `sensor_data` | User | 180 days | 2024-03-28 | Active | 0 |
| `sensor_data_30m` | User | Infinite | 2025-08-19 | Active | 0 |

## Detailed Bucket Information

### System Buckets

#### 1. `_tasks`
- **ID**: `588f3f54b63ec812`
- **Type**: System
- **Description**: System bucket for task logs
- **Created**: 2024-03-28T11:31:18.304819839Z
- **Retention Policy**:
  - Type: Expire
  - Duration: 259200 seconds (3 days)
  - Shard Group Duration: 86400 seconds (1 day)
- **Labels**: None
- **Write Endpoint**: `/api/v2/write?org=wildfly_domain&bucket=588f3f54b63ec812`
- **Data Status**: Currently empty
- **Purpose**: Stores InfluxDB task execution logs and system task-related data

#### 2. `_monitoring`
- **ID**: `a2b515e0896b2660`
- **Type**: System
- **Description**: System bucket for monitoring logs
- **Created**: 2024-03-28T11:31:18.311903296Z
- **Retention Policy**:
  - Type: Expire
  - Duration: 604800 seconds (7 days)
  - Shard Group Duration: 86400 seconds (1 day)
- **Labels**: None
- **Write Endpoint**: `/api/v2/write?org=wildfly_domain&bucket=a2b515e0896b2660`
- **Data Status**: Currently empty
- **Purpose**: Stores InfluxDB internal monitoring metrics and logs

### User Buckets

#### 3. `system_metrics`
- **ID**: `6a14b3923c1e1a85`
- **Type**: User
- **Description**: None specified
- **Created**: 2024-07-21T12:43:59.514176839Z
- **Retention Policy**:
  - Type: Expire
  - Duration: 0 seconds (Infinite retention)
  - Shard Group Duration: 604800 seconds (7 days)
- **Labels**: None
- **Write Endpoint**: `/api/v2/write?org=wildfly_domain&bucket=6a14b3923c1e1a85`
- **Data Status**: Active with system monitoring data
- **Data Content**: CPU usage metrics, memory, and system performance data from host `home-server`
- **Data Range**: Recent data from system monitoring
- **Purpose**: Stores system-level metrics from Telegraf or similar monitoring agents

#### 4. `costs`
- **ID**: `d63df5a4f2bda1e1`
- **Type**: User
- **Description**: None specified
- **Created**: 2024-03-28T11:31:18.333742515Z
- **Retention Policy**:
  - Type: Expire
  - Duration: 0 seconds (Infinite retention)
  - Shard Group Duration: 604800 seconds (7 days)
- **Labels**: None
- **Write Endpoint**: `/api/v2/write?org=wildfly_domain&bucket=d63df5a4f2bda1e1`
- **Data Status**: Currently empty
- **Purpose**: Designed for cost-related metrics and financial data (currently unused)

#### 5. `sensor_data`
- **ID**: `e0e298907f1abbce`
- **Type**: User
- **Description**: None specified
- **Created**: 2024-03-28T12:39:33.821718222Z
- **Last Updated**: 2025-08-19T17:52:40.688510131Z
- **Retention Policy**:
  - Type: Expire
  - Duration: 15552000 seconds (180 days)
  - Shard Group Duration: 604800 seconds (7 days)
- **Labels**: None
- **Write Endpoint**: `/api/v2/write?org=wildfly_domain&bucket=e0e298907f1abbce`
- **Data Status**: Active with extensive IoT sensor data
- **Data Content**:
  - **Humidity Sensors**: Multiple Xiaomi Aqara weather sensors (bathroom, hallway, kitchen, bedroom, living room)
  - **Energy Monitoring**: Tasmota energy monitors with current, voltage, power, energy consumption data
  - **Data Sources**: Home Assistant integration (source: "HA")
  - **Measurement Types**: % (humidity), A (current), V (voltage), W (power), kWh (energy), VA (apparent power)
- **Data Range**: From 2025-08-17 to present (active real-time data)
- **Update Frequency**: Real-time sensor readings
- **Purpose**: Main IoT sensor data storage from Home Assistant with 6-month retention

#### 6. `sensor_data_30m`
- **ID**: `f22d9d3440ec874b`
- **Type**: User
- **Description**: None specified
- **Created**: 2025-08-19T16:14:44.806107146Z
- **Retention Policy**:
  - Type: Expire
  - Duration: 0 seconds (Infinite retention)
  - Shard Group Duration: 604800 seconds (7 days)
- **Labels**: None
- **Write Endpoint**: `/api/v2/write?org=wildfly_domain&bucket=f22d9d3440ec874b`
- **Data Status**: Active with aggregated sensor data
- **Data Content**: Downsampled humidity sensor data averaged to 30-minute intervals
- **Data Sources**: Same IoT sensors as `sensor_data` but with time-based aggregation
- **Measurement Types**: % (aggregated humidity values)
- **Data Range**: From 2025-10-09 to present
- **Update Frequency**: 30-minute intervals (downsampled from real-time data)
- **Purpose**: Long-term storage of aggregated sensor data for trend analysis (infinite retention)

## Retention Policy Summary

- **Infinite Retention**: 3 buckets (`system_metrics`, `costs`, `sensor_data_30m`)
- **Finite Retention**: 3 buckets (`_tasks`: 3 days, `_monitoring`: 7 days, `sensor_data`: 180 days)
- **Shortest Retention**: 3 days (system task logs)
- **Longest Finite Retention**: 180 days (main sensor data)

## Data Volume Summary

| Bucket | Data Status | Data Range | Update Frequency | Primary Data Types |
|--------|-------------|------------|------------------|-------------------|
| `_tasks` | Empty | N/A | N/A | System task logs |
| `_monitoring` | Empty | N/A | N/A | InfluxDB monitoring |
| `system_metrics` | Active | Recent | ~10 seconds | CPU, memory, system metrics |
| `costs` | Empty | N/A | N/A | Cost/financial data |
| `sensor_data` | Active | 2025-08-17 to present | Real-time | IoT sensors, energy monitoring |
| `sensor_data_30m` | Active | 2025-10-09 to present | 30 minutes | Aggregated sensor data |

### Data Sources Integration
- **Home Assistant**: Primary data source for IoT sensors
- **Tasmota Devices**: Energy monitoring integration
- **Xiaomi Aqara**: Weather/humidity sensors
- **Telegraf**: System metrics collection
- **Location**: Home environment (multiple rooms)

## Storage Configuration

- **Shard Group Duration**:
  - System buckets: 1 day (86400 seconds)
  - User buckets: 7 days (604800 seconds)
- **Organization**: wildfly_domain
- **Total Buckets**: 6 (4 user, 2 system)
- **Active Data Sources**: Home Assistant, Telegraf, Tasmota devices

## API Access

### Query Buckets
```bash
curl -H "Authorization: Token YOUR_TOKEN" \
  "http://192.168.178.29:8086/api/v2/buckets"
```

### Write to Bucket
```bash
curl -X POST -H "Authorization: Token YOUR_TOKEN" \
  -H "Content-Type: text/plain; charset=utf-8" \
  --data-binary "measurement,tag=value field=1.0 $(date +%s)000000000" \
  "http://192.168.178.29:8086/api/v2/write?org=wildfly_domain&bucket=BUCKET_ID"
```

### Query Data (Flux)
```flux
from(bucket: "sensor_data")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "your_measurement")
```

## Notes

- All buckets have 0 labels assigned
- No bucket descriptions are provided for user buckets
- **Data Collection Strategy**: Two-tier approach with real-time data (`sensor_data`) + aggregated long-term storage (`sensor_data_30m`)
- **System buckets use shorter shard group durations** (1 day) compared to user buckets (7 days) for better performance
- **3 buckets are currently empty**: `_tasks`, `_monitoring`, and `costs` - may be configured but not actively used
- **Infinite retention buckets will require manual management** or cleanup policies for long-term storage planning
- **Home Assistant Integration**: Primary data source appears to be Home Assistant with IoT and energy monitoring devices
- **Data Freshness**: Most recent data from sensor buckets indicates active real-time collection
- **Multi-language Setup**: German-friendly entity names suggest deployment in German-speaking environment

---
**Last Updated**: 2025-11-08
**Generated via InfluxDB API v2**