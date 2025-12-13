package com.marvin.export.costs;

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
import com.marvin.export.core.AbstractExporterBase;
import com.marvin.export.core.ExportConfig;
import com.marvin.export.core.ExportFileWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class CostExporter extends AbstractExporterBase {

    private static final String DAILY_COSTS_FILENAME_PREFIX = "daily_costs";
    private static final String MONTHLY_COSTS_FILENAME_PREFIX = "monthly_costs";
    private static final String SPECIAL_COSTS_FILENAME_PREFIX = "special_costs";
    private static final String SALARIES_FILENAME_PREFIX = "salaries";
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

    private final DailyCostRepository dailyCostRepository;
    private final MonthlyCostRepository monthlyCostRepository;
    private final SpecialCostEntryRepository specialCostEntryRepository;
    private final SalaryRepository salaryRepository;

    public CostExporter(
        ExportConfig exportConfig,
        ExportFileWriter exportFileWriter,
        DailyCostRepository dailyCostRepository,
        MonthlyCostRepository monthlyCostRepository,
        SpecialCostEntryRepository specialCostEntryRepository,
        SalaryRepository salaryRepository) {
        super(exportConfig, exportFileWriter);
        this.dailyCostRepository = dailyCostRepository;
        this.monthlyCostRepository = monthlyCostRepository;
        this.specialCostEntryRepository = specialCostEntryRepository;
        this.salaryRepository = salaryRepository;
    }

    public List<Path> exportCosts() {
        final String timestamp = getCurrentTimestamp();
        final String costExportFolder = exportConfig.getCostExportFolder();

        final Path dailyCostsPath = createFilePath(costExportFolder, DAILY_COSTS_FILENAME_PREFIX,
            timestamp, FILE_EXTENSION);
        exportData(dailyCostsPath, () -> dailyCostRepository.findAll()
            .stream()
            .map(DAILY_COST_MAPPER));

        final Path monthlyCostsPath = createFilePath(costExportFolder,
            MONTHLY_COSTS_FILENAME_PREFIX, timestamp, FILE_EXTENSION);
        exportData(monthlyCostsPath, () -> monthlyCostRepository.findAll()
            .stream()
            .map(MONTHLY_COST_MAPPER));

        final Path specialCostsPath = createFilePath(costExportFolder,
            SPECIAL_COSTS_FILENAME_PREFIX, timestamp, FILE_EXTENSION);
        exportData(specialCostsPath, this::createSpecialCostsStream);

        final Path salariesPath = createFilePath(costExportFolder, SALARIES_FILENAME_PREFIX,
            timestamp, FILE_EXTENSION);
        exportData(salariesPath, () -> salaryRepository.findAll()
            .stream()
            .map(SALARY_MAPPER));

        return Stream.of(dailyCostsPath, monthlyCostsPath, specialCostsPath, salariesPath)
            .map(Path::getFileName)
            .toList();
    }

    @Override
    protected List<Path> export() {
        return exportCosts();
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
}
