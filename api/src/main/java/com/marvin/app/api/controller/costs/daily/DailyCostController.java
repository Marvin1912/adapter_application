package com.marvin.app.api.controller.costs.daily;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.app.importer.costs.DailyCostImportService;
import com.marvin.common.costs.DailyCostDTO;
import com.marvin.database.repository.DailyCostRepository;
import java.time.LocalDate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DailyCostController {

    private final ObjectMapper objectMapper;
    private final DailyCostRepository dailyCostRepository;
    private final DailyCostImportService dailyCostImportService;

    public DailyCostController(
            ObjectMapper objectMapper,
            DailyCostRepository dailyCostRepository,
            DailyCostImportService dailyCostImportService
    ) {
        this.objectMapper = objectMapper;
        this.dailyCostRepository = dailyCostRepository;
        this.dailyCostImportService = dailyCostImportService;
    }

    @GetMapping(path = "/daily_costs/{date}/ge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDailyCostGe(@PathVariable("date") String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            return ResponseEntity.ok(
                    dailyCostRepository.findByCostDateGreaterThanEqualOrderByCostDate(localDate)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e);
        }
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> importDailyCost(String dailyCostValue) {
        try {
            DailyCostDTO dailyCost = objectMapper.readValue(dailyCostValue, DailyCostDTO.class);
            dailyCostImportService.importData(null, dailyCost);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
