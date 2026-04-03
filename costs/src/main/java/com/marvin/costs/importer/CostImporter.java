package com.marvin.costs.importer;

import com.marvin.costs.service.FileTypeHandler;
import com.marvin.costs.service.GenericFileReader;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Triggers file-based import of all cost types from the configured input directory. */
@Component
public class CostImporter {

    private final String in;
    private final GenericFileReader genericFileReader;
    private final List<FileTypeHandler<?>> fileTypeHandlers;

    /**
     * Constructs a new {@code CostImporter}.
     *
     * @param in                   the path to the cost import input directory
     * @param genericFileReader    the file reader for processing import files
     * @param dailyCostHandler     the handler for daily cost files
     * @param monthlyCostHandler   the handler for monthly cost files
     * @param salaryHandler        the handler for salary files
     * @param specialCostHandler   the handler for special cost files
     */
    public CostImporter(
            @Value("${importer.in.costs}") String in,
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

    /** Imports all cost files from the configured input directory. */
    public void importFiles() {
        genericFileReader.importFiles(in, fileTypeHandlers);
    }
}
