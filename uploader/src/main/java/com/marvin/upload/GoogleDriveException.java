package com.marvin.upload;

public class GoogleDriveException extends Exception {

    public GoogleDriveException(String message) {
        super(message);
    }

    public GoogleDriveException(Exception e) {
        super(e);
    }
}