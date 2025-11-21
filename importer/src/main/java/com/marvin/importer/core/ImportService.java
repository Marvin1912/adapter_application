package com.marvin.importer.core;

public interface ImportService<T> {

    void importData(T data);
}