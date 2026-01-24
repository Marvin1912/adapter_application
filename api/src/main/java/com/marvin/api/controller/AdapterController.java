package com.marvin.api.controller;

import com.marvin.api.service.ExportTrackingService;
import com.marvin.app.importer.costs.CostImporter;
import com.marvin.app.importer.sensors.SensorDataImporter;
import com.marvin.app.importer.vocabulary.VocabularyImporter;
import com.marvin.export.costs.CostExporter;
import com.marvin.export.vocabulary.VocabularyExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Data Import/Export", description = "API for triggering data import and export operations")
public class AdapterController {

    private final CostImporter costImporter;
    private final SensorDataImporter sensorDataImporter;
    private final VocabularyImporter vocabularyImporter;
    private final CostExporter exporter;
    private final VocabularyExporter vocabularyExporter;
    private final ExportTrackingService exportTrackingService;

    public AdapterController(
        CostImporter costImporter,
        SensorDataImporter sensorDataImporter,
        VocabularyImporter vocabularyImporter,
        CostExporter exporter,
        VocabularyExporter vocabularyExporter,
        ExportTrackingService exportTrackingService
    ) {
        this.costImporter = costImporter;
        this.sensorDataImporter = sensorDataImporter;
        this.vocabularyImporter = vocabularyImporter;
        this.exporter = exporter;
        this.vocabularyExporter = vocabularyExporter;
        this.exportTrackingService = exportTrackingService;
    }

    @Operation(
        summary = "Trigger cost data import",
        description = "Initiates the import process for cost data from configured sources"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import triggered successfully")
    })
    @PostMapping("/import/costs")
    public ResponseEntity<Void> triggerCostImport() {
        costImporter.importFiles();
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Trigger sensor data import",
        description = "Initiates the import process for sensor data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import triggered successfully")
    })
    @PostMapping("/import/sensordata")
    public ResponseEntity<Void> triggerSensordataImport() {
        sensorDataImporter.importFiles();
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Trigger vocabulary data import",
        description = "Initiates the import process for vocabulary data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import triggered successfully")
    })
    @PostMapping("/import/vocabulary")
    public ResponseEntity<Void> triggerVocabularyImport() {
        vocabularyImporter.importFiles();
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Trigger cost data export",
        description = "Initiates the export process for cost data, tracked via ExportTrackingService"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export triggered successfully")
    })
    @PostMapping("/export/costs")
    public ResponseEntity<Void> exportCosts() {
        exportTrackingService.trackExport(ExportTrackingService.ExporterType.COSTS, null, null, exporter::exportCosts);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Trigger vocabulary data export",
        description = "Initiates the export process for vocabulary data, tracked via ExportTrackingService"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export triggered successfully")
    })
    @PostMapping("/export/vocabulary")
    public ResponseEntity<Void> exportVocabulary() {
        exportTrackingService.trackExport(ExportTrackingService.ExporterType.VOCABULARY, null, null, vocabularyExporter::exportVocabulary);
        return ResponseEntity.ok().build();
    }

}
