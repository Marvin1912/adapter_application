package com.marvin.backup.model.event;

import java.nio.file.Path;

/**
 * Event fired when a new backup file is detected in the watched directory.
 *
 * @param path the path to the detected backup file
 */
public record BackupFileEvent(Path path) {

}
