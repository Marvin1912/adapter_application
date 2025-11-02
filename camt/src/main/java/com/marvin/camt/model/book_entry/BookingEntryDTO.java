package com.marvin.camt.model.book_entry;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingEntryDTO(
        CreditDebitCodeDTO creditDebitCode,
        String entryInfo,
        BigDecimal amount,
        LocalDate bookingDate,
        LocalDate firstOfMonth,
        String debitName,
        String debitIban,
        String creditName,
        String creditIban,
        String additionalInfo
) {

}
