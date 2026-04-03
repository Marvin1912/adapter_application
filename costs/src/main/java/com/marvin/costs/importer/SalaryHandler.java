package com.marvin.costs.importer;

import com.marvin.common.costs.SalaryDTO;
import com.marvin.costs.service.FileTypeHandler;
import com.marvin.influxdb.core.InfluxWriteConfig;
import org.springframework.stereotype.Component;

/** File type handler for salary JSON import files. */
@Component
public class SalaryHandler implements FileTypeHandler<SalaryDTO> {

    private final SalaryImportService salaryImportService;

    /**
     * Constructs a new {@code SalaryHandler}.
     *
     * @param salaryImportService the service used to persist and forward salary data
     */
    public SalaryHandler(SalaryImportService salaryImportService) {
        this.salaryImportService = salaryImportService;
    }

    @Override
    public boolean canHandle(String fileType) {
        return "salaries".equals(fileType);
    }

    @Override
    public Class<SalaryDTO> getDtoClass() {
        return SalaryDTO.class;
    }

    @Override
    public String getBucket() {
        return "costs";
    }

    @Override
    public void handle(final InfluxWriteConfig config, SalaryDTO dto) {
        if (dto != null) {
            salaryImportService.importData(config, dto);
        }
    }
}
