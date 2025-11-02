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

    public static final Function<SpecialCostEntryEntity, SpecialCostEntryDTO> SPECIAL_COST_ENTRY_MAPPER = e ->
            new SpecialCostEntryDTO(e.getDescription(), e.getValue(), e.getAdditionalInfo());
    public static final Function<Map.Entry<LocalDate, List<SpecialCostEntryDTO>>, SpecialCostDTO> SPECIAL_COST_MAPPER = e ->
            new SpecialCostDTO(e.getKey(), e.getValue());
    public static final Function<SalaryEntity, SalaryDTO> SALARY_MAPPER = salaryEntity ->
            new SalaryDTO(salaryEntity.getSalaryDate(), salaryEntity.getValue());
    private static final DateTimeFormatter FILE_DTF = DateTimeFormatter.ofPattern(
            "yyyyMMdd_hhmmss");
    private static final Function<DailyCostEntity, DailyCostDTO> DAILY_COST_MAPPER = dailyCostEntity ->
            new DailyCostDTO(dailyCostEntity.getCostDate(), dailyCostEntity.getValue(),
                    dailyCostEntity.getDescription());
    private static final Function<MonthlyCostEntity, MonthlyCostDTO> MONTHLY_COST_MAPPER = monthlyCostEntity ->
            new MonthlyCostDTO(monthlyCostEntity.getCostDate(), monthlyCostEntity.getValue());
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
            SalaryRepository salaryRepository
    ) {
        this.exportConfig = exportConfig;
        this.exportFileWriter = exportFileWriter;
        this.dailyCostRepository = dailyCostRepository;
        this.monthlyCostRepository = monthlyCostRepository;
        this.specialCostEntryRepository = specialCostEntryRepository;
        this.salaryRepository = salaryRepository;
    }

    public List<Path> exportCosts() {

        String now = LocalDateTime.now().format(FILE_DTF);
        String costExportFolder = exportConfig.getCostExportFolder();

        final Path dailyCostsPath = Path.of(costExportFolder + "/daily_costs_" + now + ".json");
        exportCost(
                dailyCostsPath,
                () -> dailyCostRepository.findAll().stream().map(DAILY_COST_MAPPER)
        );

        final Path monthlyCostsPath = Path.of(costExportFolder + "/monthly_costs_" + now + ".json");
        exportCost(
                monthlyCostsPath,
                () -> monthlyCostRepository.findAll().stream().map(MONTHLY_COST_MAPPER)
        );

        final Path specialCostsPath = Path.of(costExportFolder + "/special_costs_" + now + ".json");
        exportCost(
                specialCostsPath,
                () -> specialCostEntryRepository.findAll().stream()
                        .collect(
                                Collectors.groupingBy(
                                        e -> e.getSpecialCost().getCostDate(),
                                        Collectors.mapping(SPECIAL_COST_ENTRY_MAPPER,
                                                Collectors.toList())
                                )
                        ).entrySet().stream()
                        .map(SPECIAL_COST_MAPPER)
        );

        final Path salariesPath = Path.of(costExportFolder + "/salaries_" + now + ".json");
        exportCost(
                salariesPath,
                () -> salaryRepository.findAll().stream().map(SALARY_MAPPER)
        );

        return List.of(dailyCostsPath, monthlyCostsPath, specialCostsPath, salariesPath).stream()
                .map(Path::getFileName)
                .toList();
    }

    private <T> void exportCost(Path path, Supplier<Stream<T>> costs) {
        exportFileWriter.writeFile(path, costs.get());
    }

}
