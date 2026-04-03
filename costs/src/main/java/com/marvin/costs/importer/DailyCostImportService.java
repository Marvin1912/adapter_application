package com.marvin.costs.importer;

import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.common.costs.DailyCostDTO;
import com.marvin.costs.repository.DailyCostRepository;
import com.marvin.costs.service.ImportService;
import com.marvin.costs.entity.DailyCostEntity;
import com.marvin.influxdb.core.InfluxWriteConfig;
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

/** Service that imports daily cost data from CAMT booking entries and JSON files. */
@Component
public class DailyCostImportService implements ImportService<DailyCostDTO> {

    /** Pattern used to identify daily cost bookings by the credit name. */
    public static final Pattern PATTERN = Pattern.compile(
            "(?i).*\\b(edeka|rewe|budni|lidl|lamehr)\\b.*");

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyCostImportService.class);

    private final DailyCostRepository dailyCostRepository;
    private final DailyCostImport dailyCostImport;
    private final DailyCostImportService dailyCostImportService;

    /**
     * Constructs a new {@code DailyCostImportService}.
     *
     * @param dailyCostRepository    the repository for daily cost entities
     * @param dailyCostImport        the InfluxDB daily cost import service
     * @param dailyCostImportService self-reference for transactional proxy (lazy)
     */
    public DailyCostImportService(
            DailyCostRepository dailyCostRepository,
            DailyCostImport dailyCostImport,
            @Lazy DailyCostImportService dailyCostImportService
    ) {
        this.dailyCostRepository = dailyCostRepository;
        this.dailyCostImport = dailyCostImport;
        this.dailyCostImportService = dailyCostImportService;
    }

    /**
     * Imports daily costs from a stream of booking entries by filtering and aggregating by date.
     *
     * @param bookEntryStream the stream of booking entries to process
     * @return a flux of status messages for each processed daily cost
     */
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
                .doOnNext(config -> dailyCostImportService.importData(null, config))
                .map(monthlyCostDTO -> "Processed " + monthlyCostDTO + "!");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importData(InfluxWriteConfig config, DailyCostDTO dailyCost) {
        dailyCostRepository
                .findByCostDateAndDescriptionOrderByCostDate(dailyCost.costDate(),
                        dailyCost.description())
                .ifPresentOrElse(
                        persistedState -> updateIfNecessary(persistedState, dailyCost),
                        () -> saveNewDailyCost(dailyCost));
        dailyCostImport.importCost(dailyCost);
    }

    private void updateIfNecessary(DailyCostEntity persistedState, DailyCostDTO dailyCost) {
        final BigDecimal newValue = dailyCost.value();
        final BigDecimal persistedValue = persistedState.getValue();
        if (newValue.compareTo(persistedValue) > 0) {
            LOGGER.info("Updated value of {} from {} to {}!", persistedState.getCostDate(),
                    persistedValue, newValue);
            persistedState.setValue(newValue);
            dailyCostRepository.save(persistedState);
        }
    }

    private void saveNewDailyCost(DailyCostDTO dailyCost) {
        final DailyCostEntity newDailyCostEntity = new DailyCostEntity(dailyCost.costDate(),
                dailyCost.value(), dailyCost.description());
        dailyCostRepository.save(newDailyCostEntity);
    }
}
