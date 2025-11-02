package com.marvin.export;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExportConfig {

    private final String costExportFolder;

    public ExportConfig(
            @Value("${exporter.folder}") String costExportFolder
    ) {
        this.costExportFolder = costExportFolder;
    }

    public String getCostExportFolder() {
        return costExportFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExportConfig that = (ExportConfig) o;
        return Objects.equals(costExportFolder, that.costExportFolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(costExportFolder);
    }

    @Override
    public String toString() {
        return "ExportConfig{" +
                "costExportFolder='" + costExportFolder + '\'' +
                '}';
    }
}
