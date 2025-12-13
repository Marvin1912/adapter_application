package com.marvin.export.vocabulary;

import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VocabularyExporter {

    public static final String FILENAME_PREFIX = "vocabulary";

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final String FILE_EXTENSION = ".json";

}
