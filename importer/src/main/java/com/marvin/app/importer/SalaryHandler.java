package com.marvin.app.importer;

import com.marvin.common.costs.SalaryDTO;
import org.springframework.stereotype.Component;

@Component
public class SalaryHandler implements FileTypeHandler<SalaryDTO> {

    private final SalaryImportService salaryImportService;

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
    public ImportService<SalaryDTO> getImportService() {
        return salaryImportService;
    }

    @Override
    public void send(SalaryDTO dto) {
        if (dto != null) {
            salaryImportService.importData(dto);
        }
    }
}