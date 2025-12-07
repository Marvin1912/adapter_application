package com.marvin.app.importer.costs;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.influxdb.core.InfluxWriteConfig;
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
    public String getBucket() {
        return "costs";
    }

    @Override
    public void handle(final InfluxWriteConfig config, MonthlyCostDTO dto) {
        if (dto != null) {
            monthlyCostImportService.importData(config, dto);
        }
    }
}