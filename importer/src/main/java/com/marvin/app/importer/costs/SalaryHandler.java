package com.marvin.app.importer.costs;

import com.marvin.app.service.FileTypeHandler;
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
    public void handle(SalaryDTO dto) {
        if (dto != null) {
            salaryImportService.importData(dto);
        }
    }
}