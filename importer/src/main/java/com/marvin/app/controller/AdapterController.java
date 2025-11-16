package com.marvin.app.controller;

import com.marvin.app.importer.CostImporter;
import com.marvin.export.Exporter;
import com.marvin.upload.Uploader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdapterController {

    private final CostImporter costImporter;
    private final Uploader uploader;
    private final Exporter exporter;

    public AdapterController(CostImporter costImporter, Uploader uploader, Exporter exporter) {
        this.costImporter = costImporter;
        this.uploader = uploader;
        this.exporter = exporter;
    }

    @PostMapping("/import/costs")
    public ResponseEntity<Void> triggerCostImport() {
        costImporter.importFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/export/costs")
    public ResponseEntity<Void> triggerCostUpload() {
        uploader.zipAndUploadCostFiles(exporter.exportCosts());
        return ResponseEntity.ok().build();
    }

}
