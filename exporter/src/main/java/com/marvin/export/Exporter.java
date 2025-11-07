package com.marvin.export;

import com.marvin.common.costs.DailyCostDTO;
import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.common.costs.SalaryDTO;
import com.marvin.common.costs.SpecialCostDTO;
import com.marvin.common.costs.SpecialCostEntryDTO;
import com.marvin.database.repository.DailyCostRepository;
import com.marvin.database.repository.MonthlyCostRepository;
import com.marvin.database.repository.SalaryRepository;
import com.marvin.database.repository.SpecialCostEntryRepository;
import com.marvin.entities.costs.DailyCostEntity;
import com.marvin.entities.costs.MonthlyCostEntity;
import com.marvin.entities.costs.SalaryEntity;
import com.marvin.entities.costs.SpecialCostEntryEntity;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class Exporter {

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final String DAILY_COSTS_FILENAME_PREFIX = "daily_costs_";
    private static final String MONTHLY_COSTS_FILENAME_PREFIX = "monthly_costs_";
    private static final String SPECIAL_COSTS_FILENAME_PREFIX = "special_costs_";
    private static final String SALARIES_FILENAME_PREFIX = "salaries_";
    private static final String FILE_EXTENSION = ".json";

    private static final Function<SpecialCostEntryEntity, SpecialCostEntryDTO>
            SPECIAL_COST_ENTRY_MAPPER = e -> new SpecialCostEntryDTO(
                    e.getDescription(), e.getValue(), e.getAdditionalInfo());

    private static final Function<Map.Entry<LocalDate, List<SpecialCostEntryDTO>>, SpecialCostDTO>
            SPECIAL_COST_MAPPER = e -> new SpecialCostDTO(e.getKey(), e.getValue());

    private static final Function<SalaryEntity, SalaryDTO> SALARY_MAPPER =
            salaryEntity -> new SalaryDTO(salaryEntity.getSalaryDate(), salaryEntity.getValue());

    private static final Function<DailyCostEntity, DailyCostDTO> DAILY_COST_MAPPER =
            dailyCostEntity -> new DailyCostDTO(
                    dailyCostEntity.getCostDate(),
                    dailyCostEntity.getValue(),
                    dailyCostEntity.getDescription());

    private static final Function<MonthlyCostEntity, MonthlyCostDTO> MONTHLY_COST_MAPPER =
            monthlyCostEntity -> new MonthlyCostDTO(
                    monthlyCostEntity.getCostDate(),
                    monthlyCostEntity.getValue());

    private final ExportConfig exportConfig;
    private final ExportFileWriter exportFileWriter;
    private final DailyCostRepository dailyCostRepository;
    private final MonthlyCostRepository monthlyCostRepository;
    private final SpecialCostEntryRepository specialCostEntryRepository;
    private final SalaryRepository salaryRepository;

    public Exporter(
            ExportConfig exportConfig,
            ExportFileWriter exportFileWriter,
            DailyCostRepository dailyCostRepository,
            MonthlyCostRepository monthlyCostRepository,
            SpecialCostEntryRepository specialCostEntryRepository,
            SalaryRepository salaryRepository) {
        this.exportConfig = exportConfig;
        this.exportFileWriter = exportFileWriter;
        this.dailyCostRepository = dailyCostRepository;
        this.monthlyCostRepository = monthlyCostRepository;
        this.specialCostEntryRepository = specialCostEntryRepository;
        this.salaryRepository = salaryRepository;
    }

    public List<Path> exportCosts() {
        final String timestamp = LocalDateTime.now().format(FILE_DATE_TIME_FORMATTER);
        final String costExportFolder = exportConfig.getCostExportFolder();

        final Path dailyCostsPath = createFilePath(costExportFolder, DAILY_COSTS_FILENAME_PREFIX, timestamp);
        exportCost(dailyCostsPath, () -> dailyCostRepository.findAll()
                .stream()
                .map(DAILY_COST_MAPPER));

        final Path monthlyCostsPath = createFilePath(costExportFolder, MONTHLY_COSTS_FILENAME_PREFIX, timestamp);
        exportCost(monthlyCostsPath, () -> monthlyCostRepository.findAll()
                .stream()
                .map(MONTHLY_COST_MAPPER));

        final Path specialCostsPath = createFilePath(costExportFolder, SPECIAL_COSTS_FILENAME_PREFIX, timestamp);
        exportCost(specialCostsPath, this::createSpecialCostsStream);

        final Path salariesPath = createFilePath(costExportFolder, SALARIES_FILENAME_PREFIX, timestamp);
        exportCost(salariesPath, () -> salaryRepository.findAll()
                .stream()
                .map(SALARY_MAPPER));

        return List.of(dailyCostsPath, monthlyCostsPath, specialCostsPath, salariesPath)
                .stream()
                .map(Path::getFileName)
                .toList();
    }

    private Path createFilePath(String folder, String prefix, String timestamp) {
        return Path.of(folder, prefix + timestamp + FILE_EXTENSION);
    }

    private Stream<SpecialCostDTO> createSpecialCostsStream() {
        return specialCostEntryRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getSpecialCost().getCostDate(),
                        Collectors.mapping(SPECIAL_COST_ENTRY_MAPPER, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(SPECIAL_COST_MAPPER);
    }

    private <T> void exportCost(Path path, Supplier<Stream<T>> costs) {
        exportFileWriter.writeFile(path, costs.get());
    }
}
