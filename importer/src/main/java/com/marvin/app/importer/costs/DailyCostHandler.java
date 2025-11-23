package com.marvin.app.importer.costs;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.common.costs.DailyCostDTO;
import org.springframework.stereotype.Component;

@Component
public class DailyCostHandler implements FileTypeHandler<DailyCostDTO> {

    private final DailyCostImportService dailyCostImportService;

    public DailyCostHandler(DailyCostImportService dailyCostImportService) {
        this.dailyCostImportService = dailyCostImportService;
    }

    @Override
    public boolean canHandle(String fileType) {
        return "daily_costs".equals(fileType);
    }

    @Override
    public Class<DailyCostDTO> getDtoClass() {
        return DailyCostDTO.class;
    }

    @Override
    public void handle(DailyCostDTO dto) {
        if (dto != null) {
            dailyCostImportService.importData(dto);
        }
    }
}