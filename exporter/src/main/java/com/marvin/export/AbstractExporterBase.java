package com.marvin.export;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractExporterBase {

    protected static final DateTimeFormatter FILE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    protected final ExportConfig exportConfig;
    protected final ExportFileWriter exportFileWriter;

    protected AbstractExporterBase(ExportConfig exportConfig, ExportFileWriter exportFileWriter) {
        this.exportConfig = exportConfig;
        this.exportFileWriter = exportFileWriter;
    }

    protected abstract List<Path> export();

    protected Path createFilePath(String folder, String prefix, String timestamp, String fileExtension) {
        return Path.of(folder, prefix + '_' + timestamp + fileExtension);
    }

    protected <T> void exportData(Path path, Supplier<Stream<T>> dataSupplier) {
        exportFileWriter.writeFile(path, dataSupplier.get());
    }

    protected String getCurrentTimestamp() {
        return LocalDateTime.now().format(FILE_DATE_TIME_FORMATTER);
    }
}