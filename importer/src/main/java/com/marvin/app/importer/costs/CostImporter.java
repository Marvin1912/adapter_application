package com.marvin.app.importer.costs;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.app.service.GenericFileReader;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CostImporter {

    private final String in;
    private final GenericFileReader genericFileReader;
    private final List<FileTypeHandler<?>> fileTypeHandlers;

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

    public void importFiles() {
        genericFileReader.importFiles(in, fileTypeHandlers);
    }
}
