package com.marvin.app.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.common.costs.DailyCostDTO;
import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.common.costs.SalaryDTO;
import com.marvin.common.costs.SpecialCostDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CostImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CostImporter.class);

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "([a-z_]+)_[0-9]{8}_[0-9]{6}\\.json");

    private final String in;
    private final String done;
    private final ObjectMapper objectMapper;
    private final DailyCostImportService dailyCostImportService;
    private final MonthlyCostImportService monthlyCostImportService;
    private final SalaryImportService salaryImportService;
    private final SpecialCostImportService specialCostImportService;

    public CostImporter(
            @Value("${importer.in}") String in,
            @Value("${importer.done}") String done,
            ObjectMapper objectMapper,
            DailyCostImportService dailyCostImportService,
            MonthlyCostImportService monthlyCostImportService,
            SalaryImportService salaryImportService,
            SpecialCostImportService specialCostImportService
    ) {
        this.in = in;
        this.done = done;
        this.objectMapper = objectMapper;
        this.dailyCostImportService = dailyCostImportService;
        this.monthlyCostImportService = monthlyCostImportService;
        this.salaryImportService = salaryImportService;
        this.specialCostImportService = specialCostImportService;
    }

    public void importFiles() {
        try (Stream<Path> pathStream = Files.walk(Path.of(in), 1)) {
            pathStream
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(this::readDataFromFile);
        } catch (Exception e) {
            LOGGER.error("Could read files!", e);
        }
    }

    private void readDataFromFile(Path path) {

        final Matcher matcher = FILE_NAME_PATTERN.matcher(path.getFileName().toString());
        if (!matcher.matches()) {
            LOGGER.warn("Could not match file name pattern for {}", path);
            return;
        }

        final String type = matcher.group(1);
        final Consumer<String> sender = switch (type) {
            case "monthly_costs" -> c -> send(readValue(c, MonthlyCostDTO.class),
                    monthlyCostImportService::importData);
            case "salaries" -> c -> send(readValue(c, SalaryDTO.class), salaryImportService::importData);
            case "special_costs" -> c -> send(readValue(c, SpecialCostDTO.class),
                    specialCostImportService::importData);
            case "daily_costs" -> c -> send(readValue(c, DailyCostDTO.class), dailyCostImportService::importData);
            default -> null;
        };

        if (sender == null) {
            LOGGER.warn("Could not find matching sender for {}!", type);
            return;
        }

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(sender);
        } catch (Exception e) {
            LOGGER.error("Could read lines from {}!", path.getFileName(), e);
        }

        try {
            Files.move(path, Path.of(done).resolve(path.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Could not move file {} to done!", path);
        }
    }

    private <T> T readValue(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (Exception e) {
            LOGGER.error("Could not parse {}!", value);
            return null;
        }
    }

    private <T> void send(T dto, Consumer<T> sender) {
        if (dto != null) {
            sender.accept(dto);
        }
    }
}
