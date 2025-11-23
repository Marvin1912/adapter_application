package com.marvin.app.importer.costs;

import com.marvin.app.service.ImportService;
import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.common.costs.DailyCostDTO;
import com.marvin.database.repository.DailyCostRepository;
import com.marvin.entities.costs.DailyCostEntity;
import com.marvin.influxdb.costs.daily.service.DailyCostImport;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Component
public class DailyCostImportService implements ImportService<DailyCostDTO> {

    public static final Pattern PATTERN = Pattern.compile(
            "(?i).*\\b(edeka|rewe|budni|lidl|lamehr)\\b.*");

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyCostImportService.class);

    private final DailyCostRepository dailyCostRepository;
    private final DailyCostImport dailyCostImport;
    private final DailyCostImportService dailyCostImportService;

    public DailyCostImportService(
            DailyCostRepository dailyCostRepository,
            DailyCostImport dailyCostImport,
            @Lazy DailyCostImportService dailyCostImportService
    ) {
        this.dailyCostRepository = dailyCostRepository;
        this.dailyCostImport = dailyCostImport;
        this.dailyCostImportService = dailyCostImportService;
    }

    public Flux<String> importDailyCost(Flux<BookingEntryDTO> bookEntryStream) {
        return bookEntryStream
                .filter(dto -> PATTERN.matcher(dto.creditName()).matches())
                .groupBy(BookingEntryDTO::bookingDate)
                .concatMap(group -> group
                        .reduce(
                                new DailyCostDTO(group.key(), BigDecimal.ZERO, ""),
                                (dailyCost, bookingEntry) ->
                                        new DailyCostDTO(
                                                dailyCost.costDate(),
                                                dailyCost.value().add(bookingEntry.amount()),
                                                dailyCost.description() + "|"
                                                        + bookingEntry.creditName()
                                        )
                        )
                )
                .doOnNext(dailyCostImportService::importData)
                .map(monthlyCostDTO -> "Processed " + monthlyCostDTO + "!");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importData(DailyCostDTO dailyCost) {
        dailyCostRepository.findByCostDateAndDescriptionOrderByCostDate(dailyCost.costDate(),
                        dailyCost.description())
                .ifPresentOrElse(
                        persistedState -> updateIfNecessary(persistedState, dailyCost),
                        () -> saveNewDailyCost(dailyCost)
                );
    }

    private void updateIfNecessary(DailyCostEntity persistedState, DailyCostDTO dailyCost) {
        BigDecimal newValue = dailyCost.value();
        BigDecimal persistedValue = persistedState.getValue();
        if (newValue.compareTo(persistedValue) > 0) {
            LOGGER.info("Updated value of {} from {} to {}!", persistedState.getCostDate(),
                    persistedValue, newValue);
            persistedState.setValue(newValue);
            dailyCostRepository.save(persistedState);
            dailyCostImport.importCost(dailyCost);
        }
    }

    private void saveNewDailyCost(DailyCostDTO dailyCost) {
        DailyCostEntity newDailyCostEntity = new DailyCostEntity(dailyCost.costDate(),
                dailyCost.value(), dailyCost.description());
        dailyCostRepository.save(newDailyCostEntity);
        dailyCostImport.importCost(dailyCost);
    }
}
