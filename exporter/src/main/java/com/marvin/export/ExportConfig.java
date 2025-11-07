package com.marvin.export;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExportConfig {

    private final String costExportFolder;

    public ExportConfig(@Value("${exporter.folder}") String costExportFolder) {
        this.costExportFolder = validateCostExportFolder(costExportFolder);
    }

    public String getCostExportFolder() {
        return costExportFolder;
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
