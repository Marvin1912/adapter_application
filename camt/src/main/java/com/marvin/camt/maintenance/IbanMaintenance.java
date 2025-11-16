package com.marvin.camt.maintenance;

import com.marvin.camt.model.book_entry.BookingEntryDTO;
import org.springframework.stereotype.Component;

@Component
public class IbanMaintenance implements BookEntryDataMaintenance {

  private static BookingEntryDTO checkCreditIban(BookingEntryDTO bookingEntry) {
    return bookingEntry.creditIban() == null
        ? new BookingEntryDTO(
        bookingEntry.creditDebitCode(),
        bookingEntry.entryInfo(),
        bookingEntry.amount(),
        bookingEntry.bookingDate(),
        bookingEntry.firstOfMonth(),
        bookingEntry.debitName(),
        bookingEntry.debitIban(),
        bookingEntry.creditName(),
        "n/a", bookingEntry.additionalInfo()
    ) : bookingEntry;
  }

  private static BookingEntryDTO checkDebitIban(BookingEntryDTO bookingEntry) {
    return bookingEntry.debitIban() == null
        ? new BookingEntryDTO(
            bookingEntry.creditDebitCode(),
            bookingEntry.entryInfo(),
            bookingEntry.amount(),
            bookingEntry.bookingDate(),
            bookingEntry.firstOfMonth(),
            bookingEntry.debitName(),
            "n/a",
            bookingEntry.creditName(),
            bookingEntry.creditIban(), bookingEntry.additionalInfo()
        ) : bookingEntry;
  }

  @Override
  public BookingEntryDTO applyToBookEntry(BookingEntryDTO bookingEntry) {
    return checkDebitIban(checkCreditIban(bookingEntry));
  }
}
