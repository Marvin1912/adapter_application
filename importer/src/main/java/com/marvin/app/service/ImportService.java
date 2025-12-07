package com.marvin.app.service;

import com.marvin.influxdb.core.InfluxWriteConfig;

public interface ImportService<T> {

    void importData(InfluxWriteConfig config, T data);
}
