package com.marvin.costs.importer;

import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.costs.service.FileTypeHandler;
import com.marvin.influxdb.core.InfluxWriteConfig;
import org.springframework.stereotype.Component;

/** File type handler for monthly cost JSON import files. */
@Component
public class MonthlyCostHandler implements FileTypeHandler<MonthlyCostDTO> {

    private final MonthlyCostImportService monthlyCostImportService;

    /**
     * Constructs a new {@code MonthlyCostHandler}.
     *
     * @param monthlyCostImportService the service used to persist and forward monthly cost data
     */
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
