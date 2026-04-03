package com.marvin.api.controller;

import com.marvin.costs.importer.CostImporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Data Import", description = "API for triggering data import operations")
public class AdapterController {

    private final CostImporter costImporter;

    public AdapterController(CostImporter costImporter) {
        this.costImporter = costImporter;
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

}
