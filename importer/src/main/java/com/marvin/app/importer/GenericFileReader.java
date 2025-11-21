package com.marvin.app.importer;

import java.nio.file.Path;
import java.util.List;

public interface GenericFileReader {

    void importFiles();

    void processFile(Path path, List<FileTypeHandler<?>> handlers);
}