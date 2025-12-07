package com.marvin.app.importer.costs;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.common.costs.DailyCostDTO;
import com.marvin.influxdb.core.InfluxWriteConfig;
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
    public String getBucket() {
        return "costs";
    }

    @Override
    public void handle(final InfluxWriteConfig config, DailyCostDTO dto) {
        if (dto != null) {
            dailyCostImportService.importData(config, dto);
        }
    }
}