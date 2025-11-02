package com.marvin.camt.model.book_entry;

import java.util.List;

public record BookingsDTO(
        List<MonthlyBookingEntriesDTO> bookingsPerMonth
) {

}
