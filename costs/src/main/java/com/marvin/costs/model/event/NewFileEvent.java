package com.marvin.costs.model.event;

import java.nio.file.Path;

/**
 * Event fired when a new cost import file is detected in the watched directory.
 *
 * @param path the path to the detected file
 */
public record NewFileEvent(Path path) {

}
