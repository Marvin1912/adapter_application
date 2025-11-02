package com.marvin.camt.maintenance;

import com.marvin.camt.model.book_entry.BookingEntryDTO;

public interface BookEntryDataMaintenance {

  BookingEntryDTO applyToBookEntry(BookingEntryDTO bookingEntry);
}
