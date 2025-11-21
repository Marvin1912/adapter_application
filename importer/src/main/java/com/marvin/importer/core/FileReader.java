package com.marvin.importer.core;

import java.nio.file.Path;
import java.util.List;

public interface FileReader {

    void importFiles();

    void processFile(Path path, List<FileTypeHandler<?>> handlers);
}