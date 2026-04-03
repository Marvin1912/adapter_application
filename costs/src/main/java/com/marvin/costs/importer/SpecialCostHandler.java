package com.marvin.costs.importer;

import com.marvin.common.costs.SpecialCostDTO;
import com.marvin.costs.service.FileTypeHandler;
import com.marvin.influxdb.core.InfluxWriteConfig;
import org.springframework.stereotype.Component;

/** File type handler for special cost JSON import files. */
@Component
public class SpecialCostHandler implements FileTypeHandler<SpecialCostDTO> {

    private final SpecialCostImportService specialCostImportService;

    /**
     * Constructs a new {@code SpecialCostHandler}.
     *
     * @param specialCostImportService the service used to persist and forward special cost data
     */
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
