package com.marvin.app.importer;

import com.marvin.app.infrastructure.Ibans;
import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.camt.model.book_entry.CreditDebitCodeDTO;
import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.database.repository.MonthlyCostRepository;
import com.marvin.entities.costs.MonthlyCostEntity;
import com.marvin.influxdb.costs.monthly.service.MonthlyCostImport;
import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class MonthlyCostImportService implements ImportService<MonthlyCostDTO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyCostImportService.class);

  private final Ibans monthlyCostBlockedIbans;
  private final MonthlyCostRepository monthlyCostRepository;
  private final MonthlyCostImport monthlyCostImport;

  public MonthlyCostImportService(
      Ibans monthlyCostBlockedIbans,
      MonthlyCostRepository monthlyCostRepository,
      MonthlyCostImport monthlyCostImport
  ) {
    this.monthlyCostBlockedIbans = monthlyCostBlockedIbans;
    this.monthlyCostRepository = monthlyCostRepository;
    this.monthlyCostImport = monthlyCostImport;
  }

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
        .doOnNext(this::importData)
        .map(monthlyCostDTO -> "Processed " + monthlyCostDTO + "!");
  }

  @Override
  public void importData(MonthlyCostDTO monthlyCost) {
    final Optional<MonthlyCostEntity> persistedStateList = monthlyCostRepository.findByCostDate(
        monthlyCost.costDate());
    if (persistedStateList.isEmpty()) {
      MonthlyCostEntity monthlyCostEntity = new MonthlyCostEntity(monthlyCost.costDate(),
          monthlyCost.value());
      monthlyCostRepository.save(monthlyCostEntity);
    } else {
      BigDecimal newValue = monthlyCost.value();
      MonthlyCostEntity persistedState = persistedStateList.get();
      BigDecimal persistedValue = persistedState.getValue();
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
