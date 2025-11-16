package com.marvin.camt.maintenance;

import com.marvin.camt.model.book_entry.BookingEntryDTO;
import org.springframework.stereotype.Component;

@Component
public class NameMaintenance implements BookEntryDataMaintenance {

    private static String replaceSpaces(String value) {
        return value == null ? "n/a" : value.replaceAll("\\s+", " ");
    }

    @Override
    public BookingEntryDTO applyToBookEntry(BookingEntryDTO bookingEntry) {
        return new BookingEntryDTO(
                bookingEntry.creditDebitCode(),
                bookingEntry.entryInfo(),
                bookingEntry.amount(),
                bookingEntry.bookingDate(),
                bookingEntry.firstOfMonth(),
                replaceSpaces(bookingEntry.debitName()),
                bookingEntry.debitIban(),
                replaceSpaces(bookingEntry.creditName()),
                bookingEntry.creditIban(),
                bookingEntry.additionalInfo()
        );
    }
}
