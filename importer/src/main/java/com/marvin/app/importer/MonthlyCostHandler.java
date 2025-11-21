package com.marvin.app.importer;

import com.marvin.common.costs.MonthlyCostDTO;
import org.springframework.stereotype.Component;

@Component
public class MonthlyCostHandler implements FileTypeHandler<MonthlyCostDTO> {

    private final MonthlyCostImportService monthlyCostImportService;

    public MonthlyCostHandler(MonthlyCostImportService monthlyCostImportService) {
        this.monthlyCostImportService = monthlyCostImportService;
    }

    @Override
    public boolean canHandle(String fileType) {
        return "monthly_costs".equals(fileType);
    }

    @Override
    public Class<MonthlyCostDTO> getDtoClass() {
        return MonthlyCostDTO.class;
    }

    @Override
    public ImportService<MonthlyCostDTO> getImportService() {
        return monthlyCostImportService;
    }

    @Override
    public void send(MonthlyCostDTO dto) {
        if (dto != null) {
            monthlyCostImportService.importData(dto);
        }
    }
}