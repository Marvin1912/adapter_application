package com.marvin.camt.model.book_entry;

import java.util.List;

public record MonthlyBookingEntriesDTO(
        int year, int month,
        List<BookingEntryDTO> usualBookings,
        List<BookingEntryDTO> dailyCosts,
        List<BookingEntryDTO> incomes
) {

}
