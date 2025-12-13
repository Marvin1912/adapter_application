package com.marvin.export.core;

import java.util.Objects;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ExportConfig {

    private final String costExportFolder;
    private final String influxdbUrl;
    private final String influxdbToken;
    private final String influxdbOrg;
    private final boolean influxdbExportEnabled;

    public ExportConfig(
        @Value("${exporter.folder}") String costExportFolder,
        @Value("${influxdb.url:}") String influxdbUrl,
        @Value("${influxdb.token:}") String influxdbToken,
        @Value("${influxdb.org:}") String influxdbOrg,
        @Value("${influxdb.export.enabled:false}") boolean influxdbExportEnabled
    ) {
        this.costExportFolder = validateCostExportFolder(costExportFolder);
        this.influxdbUrl = influxdbUrl;
        this.influxdbToken = influxdbToken;
        this.influxdbOrg = influxdbOrg;
        this.influxdbExportEnabled = influxdbExportEnabled;
    }

    private String validateCostExportFolder(String folder) {
        if (folder == null || folder.trim().isEmpty()) {
            throw new IllegalArgumentException("Export folder cannot be null or empty");
        }
        return folder.trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ExportConfig that = (ExportConfig) obj;
        return Objects.equals(costExportFolder, that.costExportFolder)
            && Objects.equals(influxdbUrl, that.influxdbUrl)
            && Objects.equals(influxdbToken, that.influxdbToken)
            && Objects.equals(influxdbOrg, that.influxdbOrg)
            && Objects.equals(influxdbExportEnabled, that.influxdbExportEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(costExportFolder, influxdbUrl, influxdbToken, influxdbOrg, influxdbExportEnabled);
    }

    @Override
    public String toString() {
        return "ExportConfig{"
            + "costExportFolder='" + costExportFolder + '\''
            + ", influxdbUrl='" + influxdbUrl + '\''
            + ", influxdbToken='" + (influxdbToken != null ? "***" : "null") + '\''
            + ", influxdbOrg='" + influxdbOrg + '\''
            + ", influxdbExportEnabled=" + influxdbExportEnabled
            + '}';
    }
}
