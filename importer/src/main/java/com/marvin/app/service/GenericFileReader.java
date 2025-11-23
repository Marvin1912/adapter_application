package com.marvin.app.service;

import java.util.List;

public interface GenericFileReader {

    void importFiles(String in, List<FileTypeHandler<?>> handlers);
}
