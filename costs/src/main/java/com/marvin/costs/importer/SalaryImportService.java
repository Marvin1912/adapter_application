package com.marvin.costs.importer;

import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.camt.model.book_entry.CreditDebitCodeDTO;
import com.marvin.common.costs.SalaryDTO;
import com.marvin.costs.infrastructure.Ibans;
import com.marvin.costs.repository.SalaryRepository;
import com.marvin.costs.service.ImportService;
import com.marvin.costs.entity.SalaryEntity;
import com.marvin.influxdb.core.InfluxWriteConfig;
import com.marvin.influxdb.costs.salary.service.SalaryImport;
import java.util.Optional;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/** Service that imports salary data from CAMT booking entries and JSON files. */
@Component
public class SalaryImportService implements ImportService<SalaryDTO> {

    private final Ibans salaryImportIbans;
    private final SalaryRepository salaryRepository;
    private final SalaryImport salaryImport;

    /**
     * Constructs a new {@code SalaryImportService}.
     *
     * @param salaryImportIbans the IBANs provider for salary imports
     * @param salaryRepository  the repository for salary entities
     * @param salaryImport      the InfluxDB salary import service
     */
    public SalaryImportService(
            Ibans salaryImportIbans,
            SalaryRepository salaryRepository,
            SalaryImport salaryImport
    ) {
        this.salaryImportIbans = salaryImportIbans;
        this.salaryRepository = salaryRepository;
        this.salaryImport = salaryImport;
    }

    /**
     * Imports salaries from a stream of booking entries by filtering credit transactions.
     *
     * @param bookEntryStream the stream of booking entries to process
     * @return a flux of status messages for each processed salary
     */
    public Flux<String> importSalary(Flux<BookingEntryDTO> bookEntryStream) {
        return bookEntryStream
                .filter(dto ->
                        dto.creditDebitCode() == CreditDebitCodeDTO.CRDT
                                && salaryImportIbans.getIbans().contains(dto.debitIban())
                )
                .map(dto -> new SalaryDTO(dto.firstOfMonth(), dto.amount()))
                .doOnNext(config -> importData(null, config))
                .map(salary -> "Processed " + salary + "!");
    }

    @Override
    public void importData(InfluxWriteConfig config, SalaryDTO salary) {
        final Optional<SalaryEntity> persistedStateList = salaryRepository.findBySalaryDate(
                salary.salaryDate());
        if (persistedStateList.isEmpty()) {
            final SalaryEntity salaryEntity = new SalaryEntity(salary.salaryDate(),
                    salary.value());
            salaryRepository.save(salaryEntity);
        }
        salaryImport.importCost(salary);
    }

}
