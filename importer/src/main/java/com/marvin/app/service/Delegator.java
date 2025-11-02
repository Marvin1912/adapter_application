package com.marvin.app.service;

import com.marvin.app.importer.DailyCostImportService;
import com.marvin.app.importer.MonthlyCostImportService;
import com.marvin.app.importer.SalaryImportService;
import com.marvin.app.importer.SpecialCostImportService;
import com.marvin.app.model.event.NewFileEvent;
import com.marvin.camt.maintenance.DataMaintainer;
import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.camt.parser.CamtFileParser;
import com.marvin.camt.parser.DocumentUnmarshaller;
import java.io.InputStream;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class Delegator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Delegator.class);

    private final CamtFileParser camtFileParser;
    private final DocumentUnmarshaller documentUnmarshaller;
    private final DataMaintainer maintainer;
    private final MonthlyCostImportService monthlyCostImportService;
    private final SpecialCostImportService specialCostImportService;
    private final SalaryImportService salaryImportService;
    private final DailyCostImportService dailyCostImportService;

    public Delegator(
            CamtFileParser camtFileParser,
            DocumentUnmarshaller documentUnmarshaller,
            DataMaintainer maintainer,
            MonthlyCostImportService monthlyCostImportService,
            SpecialCostImportService specialCostImportService,
            SalaryImportService salaryImportService,
            DailyCostImportService dailyCostImportService
    ) {
        this.camtFileParser = camtFileParser;
        this.documentUnmarshaller = documentUnmarshaller;
        this.maintainer = maintainer;
        this.monthlyCostImportService = monthlyCostImportService;
        this.specialCostImportService = specialCostImportService;
        this.salaryImportService = salaryImportService;
        this.dailyCostImportService = dailyCostImportService;
    }

    @EventListener(NewFileEvent.class)
    public void startUpWatchService(NewFileEvent newFileEvent) throws Exception {

        Flux<BookingEntryDTO> bookingEntryStream = getBookingEntries(
                Files.newInputStream(newFileEvent.path()))
                .publish().autoConnect(4);

        monthlyCostImportService.importMonthlyCost(bookingEntryStream)
                .subscribe(LOGGER::info);

        specialCostImportService.importSpecialCost(bookingEntryStream)
                .subscribe(LOGGER::info);

        salaryImportService.importSalary(bookingEntryStream)
                .subscribe(LOGGER::info);

        dailyCostImportService.importDailyCost(bookingEntryStream)
                .subscribe(LOGGER::info);
    }

    private Flux<BookingEntryDTO> getBookingEntries(InputStream inputStream) {
        try {
            return documentUnmarshaller.unmarshallFile(camtFileParser.unzipFile(inputStream))
                    .flatMap(maintainer::maintainData);
        } catch (Exception e) {
            LOGGER.error("Error getting book entries!", e);
            return Flux.empty();
        }
    }
}
