package com.marvin.app.importer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CostImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CostImporter.class);

    private final String in;
    private final GenericFileReader genericFileReader;
    private final List<FileTypeHandler<?>> fileTypeHandlers;

    public CostImporter(
            @Value("${importer.in}") String in,
            GenericFileReader genericFileReader,
            DailyCostHandler dailyCostHandler,
            MonthlyCostHandler monthlyCostHandler,
            SalaryHandler salaryHandler,
            SpecialCostHandler specialCostHandler
    ) {
        this.in = in;
        this.genericFileReader = genericFileReader;
        this.fileTypeHandlers = Arrays.asList(
                dailyCostHandler,
                monthlyCostHandler,
                salaryHandler,
                specialCostHandler
        );
    }

    public void importFiles() {
        try (Stream<Path> pathStream = Files.walk(Path.of(in), 1)) {
            pathStream
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> genericFileReader.processFile(path, fileTypeHandlers));
        } catch (Exception e) {
            LOGGER.error("Could read files!", e);
        }
    }
}
