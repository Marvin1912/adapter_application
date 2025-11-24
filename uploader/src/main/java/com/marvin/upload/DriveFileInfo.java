package com.marvin.upload;

import com.google.api.client.util.DateTime;

public class DriveFileInfo {

    private final String id;
    private final String name;
    private final String mimeType;
    private final Long size;
    private final DateTime modifiedTime;
    private final String webViewLink;

    public DriveFileInfo(String id, String name, String mimeType, Long size,
                        DateTime modifiedTime, String webViewLink) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
        this.modifiedTime = modifiedTime;
        this.webViewLink = webViewLink;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getSize() {
        return size;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public boolean isDirectory() {
        return "application/vnd.google-apps.folder".equals(mimeType);
    }

    public boolean isFile() {
        return !isDirectory();
    }

    @Override
    public String toString() {
        return String.format("DriveFileInfo{name='%s', mimeType='%s', size=%d, isDirectory=%s}",
                           name, mimeType, size, isDirectory());
    }
}