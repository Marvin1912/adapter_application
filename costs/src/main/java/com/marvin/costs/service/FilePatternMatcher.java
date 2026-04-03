package com.marvin.costs.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for matching cost import file names against the expected pattern. */
public class FilePatternMatcher {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "^([a-z_0-9]+)_[0-9]{8}_[0-9]{6}\\.jsonl?$");

    private FilePatternMatcher() {
    }

    /**
     * Matches the given file name against the expected cost import file name pattern.
     *
     * @param fileName the file name to match
     * @return the match result
     */
    public static FileTypeMatchResult matchFileName(String fileName) {
        final Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return FileTypeMatchResult.failed(fileName);
        }

        final String type = matcher.group(1);
        return FileTypeMatchResult.success(fileName, type);
    }

    /**
     * Holds the result of matching a file name against the expected pattern.
     *
     * @param fileName the file name that was matched
     * @param fileType the extracted file type, or {@code null} if the match failed
     * @param matches  whether the file name matched the expected pattern
     */
    public record FileTypeMatchResult(
            String fileName,
            String fileType,
            boolean matches
    ) {
        /**
         * Creates a successful match result.
         *
         * @param fileName the matched file name
         * @param fileType the extracted file type
         * @return a successful match result
         */
        public static FileTypeMatchResult success(String fileName, String fileType) {
            return new FileTypeMatchResult(fileName, fileType, true);
        }

        /**
         * Creates a failed match result.
         *
         * @param fileName the file name that did not match
         * @return a failed match result
         */
        public static FileTypeMatchResult failed(String fileName) {
            return new FileTypeMatchResult(fileName, null, false);
        }
    }
}
