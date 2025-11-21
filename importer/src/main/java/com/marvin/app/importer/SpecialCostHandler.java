package com.marvin.app.importer;

import com.marvin.common.costs.SpecialCostDTO;
import org.springframework.stereotype.Component;

@Component
public class SpecialCostHandler implements FileTypeHandler<SpecialCostDTO> {

    private final SpecialCostImportService specialCostImportService;

    public SpecialCostHandler(SpecialCostImportService specialCostImportService) {
        this.specialCostImportService = specialCostImportService;
    }

    @Override
    public boolean canHandle(String fileType) {
        return "special_costs".equals(fileType);
    }

    @Override
    public Class<SpecialCostDTO> getDtoClass() {
        return SpecialCostDTO.class;
    }

    @Override
    public ImportService<SpecialCostDTO> getImportService() {
        return specialCostImportService;
    }

    @Override
    public void send(SpecialCostDTO dto) {
        if (dto != null) {
            specialCostImportService.importData(dto);
        }
    }
}