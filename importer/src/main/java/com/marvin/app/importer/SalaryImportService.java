package com.marvin.app.importer;

import com.marvin.app.infrastructure.Ibans;
import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.camt.model.book_entry.CreditDebitCodeDTO;
import com.marvin.common.costs.SalaryDTO;
import com.marvin.database.repository.SalaryRepository;
import com.marvin.entities.costs.SalaryEntity;
import com.marvin.influxdb.costs.salary.service.SalaryImport;
import java.util.Optional;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class SalaryImportService implements ImportService<SalaryDTO> {

    private final Ibans salaryImportIbans;
    private final SalaryRepository salaryRepository;
    private final SalaryImport salaryImport;

    public SalaryImportService(
            Ibans salaryImportIbans,
            SalaryRepository salaryRepository,
            SalaryImport salaryImport
    ) {
        this.salaryImportIbans = salaryImportIbans;
        this.salaryRepository = salaryRepository;
        this.salaryImport = salaryImport;
    }

    public Flux<String> importSalary(Flux<BookingEntryDTO> bookEntryStream) {
        return bookEntryStream

                .filter(dto ->
                        dto.creditDebitCode() == CreditDebitCodeDTO.CRDT
                                && salaryImportIbans.getIbans().contains(dto.debitIban())
                )

                .map(dto -> new SalaryDTO(dto.firstOfMonth(), dto.amount()))

                .doOnNext(this::importData)
                .map(salary -> "Processed " + salary + "!");
    }

    @Override
    public void importData(SalaryDTO salary) {
        final Optional<SalaryEntity> persistedStateList = salaryRepository.findBySalaryDate(
                salary.salaryDate());
        if (persistedStateList.isEmpty()) {
            SalaryEntity salaryEntity = new SalaryEntity(salary.salaryDate(), salary.value());
            salaryRepository.save(salaryEntity);
        }
        salaryImport.importCost(salary);
    }

}
