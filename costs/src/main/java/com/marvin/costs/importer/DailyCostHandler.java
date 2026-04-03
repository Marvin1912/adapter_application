package com.marvin.costs.importer;

import com.marvin.common.costs.DailyCostDTO;
import com.marvin.costs.service.FileTypeHandler;
import com.marvin.influxdb.core.InfluxWriteConfig;
import org.springframework.stereotype.Component;

/** File type handler for daily cost JSON import files. */
@Component
public class DailyCostHandler implements FileTypeHandler<DailyCostDTO> {

    private final DailyCostImportService dailyCostImportService;

    /**
     * Constructs a new {@code DailyCostHandler}.
     *
     * @param dailyCostImportService the service used to persist and forward daily cost data
     */
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
