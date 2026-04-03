package com.marvin.costs.importer;

import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.camt.model.book_entry.CreditDebitCodeDTO;
import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.costs.infrastructure.Ibans;
import com.marvin.costs.repository.MonthlyCostRepository;
import com.marvin.costs.service.ImportService;
import com.marvin.entities.costs.MonthlyCostEntity;
import com.marvin.influxdb.core.InfluxWriteConfig;
import com.marvin.influxdb.costs.monthly.service.MonthlyCostImport;
import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/** Service that imports monthly cost data from CAMT booking entries and JSON files. */
@Component
public class MonthlyCostImportService implements ImportService<MonthlyCostDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyCostImportService.class);

    private final Ibans monthlyCostBlockedIbans;
    private final MonthlyCostRepository monthlyCostRepository;
    private final MonthlyCostImport monthlyCostImport;

    /**
     * Constructs a new {@code MonthlyCostImportService}.
     *
     * @param monthlyCostBlockedIbans the blocked IBANs provider for monthly costs
     * @param monthlyCostRepository   the repository for monthly cost entities
     * @param monthlyCostImport       the InfluxDB monthly cost import service
     */
    public MonthlyCostImportService(
            Ibans monthlyCostBlockedIbans,
            MonthlyCostRepository monthlyCostRepository,
            MonthlyCostImport monthlyCostImport
    ) {
        this.monthlyCostBlockedIbans = monthlyCostBlockedIbans;
        this.monthlyCostRepository = monthlyCostRepository;
        this.monthlyCostImport = monthlyCostImport;
    }

    /**
     * Imports monthly costs from a stream of booking entries by filtering and aggregating by month.
     *
     * @param bookEntryStream the stream of booking entries to process
     * @return a flux of status messages for each processed monthly cost
     */
    public Flux<String> importMonthlyCost(Flux<BookingEntryDTO> bookEntryStream) {
        return bookEntryStream
                .filter(dto ->
                        dto.creditDebitCode() == CreditDebitCodeDTO.DBIT
                                && !monthlyCostBlockedIbans.getIbans().contains(dto.creditIban())
                )
                .groupBy(BookingEntryDTO::firstOfMonth)
                .flatMap(group -> group
                        .reduce(
                                new MonthlyCostDTO(group.key(), BigDecimal.ZERO),
                                (monthlyCostDTO, bookingEntryDTO) -> new MonthlyCostDTO(
                                        monthlyCostDTO.costDate(),
                                        monthlyCostDTO.value().add(bookingEntryDTO.amount())
                                )
                        )
                )
                .doOnNext(config -> importData(null, config))
                .map(monthlyCostDTO -> "Processed " + monthlyCostDTO + "!");
    }

    @Override
    public void importData(InfluxWriteConfig config, MonthlyCostDTO monthlyCost) {
        final Optional<MonthlyCostEntity> persistedStateList = monthlyCostRepository.findByCostDate(
                monthlyCost.costDate());
        if (persistedStateList.isEmpty()) {
            final MonthlyCostEntity monthlyCostEntity = new MonthlyCostEntity(
                    monthlyCost.costDate(), monthlyCost.value());
            monthlyCostRepository.save(monthlyCostEntity);
        } else {
            final BigDecimal newValue = monthlyCost.value();
            final MonthlyCostEntity persistedState = persistedStateList.get();
            final BigDecimal persistedValue = persistedState.getValue();
            if (newValue.compareTo(persistedValue) > 0) {
                LOGGER.info("Updated value of {} from {} to {}!", persistedState.getCostDate(),
                        newValue, persistedValue);
                persistedState.setValue(newValue);
                monthlyCostRepository.save(persistedState);
            }
        }
        monthlyCostImport.importCost(monthlyCost);
    }

}
