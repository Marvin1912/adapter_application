package com.marvin.app.importer.costs;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.common.costs.SpecialCostDTO;
import com.marvin.influxdb.core.InfluxWriteConfig;
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
    public String getBucket() {
        return "costs";
    }

    @Override
    public void handle(final InfluxWriteConfig config, SpecialCostDTO dto) {
        if (dto != null) {
            specialCostImportService.importData(config, dto);
        }
    }
}