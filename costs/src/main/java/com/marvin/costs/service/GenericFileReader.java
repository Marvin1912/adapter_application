package com.marvin.costs.service;

import java.util.List;

/** Reads and processes files from a directory using a list of typed handlers. */
public interface GenericFileReader {

    /**
     * Imports all files from the given directory using the provided handlers.
     *
     * @param in       the path to the input directory
     * @param handlers the list of handlers to process files with
     */
    void importFiles(String in, List<FileTypeHandler<?>> handlers);
}
