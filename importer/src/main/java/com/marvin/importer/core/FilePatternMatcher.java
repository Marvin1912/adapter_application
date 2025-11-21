package com.marvin.importer.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePatternMatcher {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "([a-z_]+)_[0-9]{8}_[0-9]{6}\\.json");

    public static FileTypeMatchResult matchFileName(String fileName) {
        final Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return FileTypeMatchResult.failed(fileName);
        }

        final String type = matcher.group(1);
        return FileTypeMatchResult.success(fileName, type);
    }

    public record FileTypeMatchResult(
            String fileName,
            String fileType,
            boolean matches
    ) {
        public static FileTypeMatchResult success(String fileName, String fileType) {
            return new FileTypeMatchResult(fileName, fileType, true);
        }

        public static FileTypeMatchResult failed(String fileName) {
            return new FileTypeMatchResult(fileName, null, false);
        }
    }
}