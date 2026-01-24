package com.marvin.api.controller;

import com.marvin.api.service.ExportTrackingService;
import com.marvin.app.importer.costs.CostImporter;
import com.marvin.app.importer.sensors.SensorDataImporter;
import com.marvin.app.importer.vocabulary.VocabularyImporter;
import com.marvin.export.costs.CostExporter;
import com.marvin.export.vocabulary.VocabularyExporter;
import com.marvin.upload.Uploader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdapterController {

    private final CostImporter costImporter;
    private final SensorDataImporter sensorDataImporter;
    private final VocabularyImporter vocabularyImporter;
    private final Uploader uploader;
    private final CostExporter exporter;
    private final VocabularyExporter vocabularyExporter;
    private final ExportTrackingService exportTrackingService;

    public AdapterController(CostImporter costImporter, SensorDataImporter sensorDataImporter, VocabularyImporter vocabularyImporter, Uploader uploader, CostExporter exporter,
        VocabularyExporter vocabularyExporter, ExportTrackingService exportTrackingService) {
        this.costImporter = costImporter;
        this.sensorDataImporter = sensorDataImporter;
        this.vocabularyImporter = vocabularyImporter;
        this.uploader = uploader;
        this.exporter = exporter;
        this.vocabularyExporter = vocabularyExporter;
        this.exportTrackingService = exportTrackingService;
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

    @PostMapping("/import/vocabulary")
    public ResponseEntity<Void> triggerVocabularyImport() {
        vocabularyImporter.importFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/export/costs")
    public ResponseEntity<Void> exportCosts() {
        exportTrackingService.trackExport(ExportTrackingService.ExporterType.COSTS, null, null, () -> exporter.exportCosts());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/export/vocabulary")
    public ResponseEntity<Void> exportVocabulary() {
        exportTrackingService.trackExport(ExportTrackingService.ExporterType.VOCABULARY, null, null, () -> vocabularyExporter.exportVocabulary());
        return ResponseEntity.ok().build();
    }

}
