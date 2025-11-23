package com.marvin.app.controller;

import com.marvin.app.importer.costs.CostImporter;
import com.marvin.app.importer.sensors.SensorDataImporter;
import com.marvin.export.Exporter;
import com.marvin.upload.Uploader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdapterController {

    private final CostImporter costImporter;
    private final SensorDataImporter sensorDataImporter;
    private final Uploader uploader;
    private final Exporter exporter;

    public AdapterController(CostImporter costImporter, SensorDataImporter sensorDataImporter, Uploader uploader, Exporter exporter) {
        this.costImporter = costImporter;
        this.sensorDataImporter = sensorDataImporter;
        this.uploader = uploader;
        this.exporter = exporter;
    }

    @PostMapping("/import/costs")
    public ResponseEntity<Void> triggerCostImport() {
        costImporter.importFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import/sensordata")
    public ResponseEntity<Void> triggerSensordataImport() {
        sensorDataImporter.importFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/export/costs")
    public ResponseEntity<Void> triggerCostUpload() {
        uploader.zipAndUploadCostFiles("costs", exporter.exportCosts());
        return ResponseEntity.ok().build();
    }

}
