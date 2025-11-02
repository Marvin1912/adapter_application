package com.marvin.camt.maintenance;

import com.marvin.camt.model.book_entry.BookingEntryDTO;
import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DataMaintainer {

  private final List<BookEntryDataMaintenance> maintainers;

  public DataMaintainer(List<BookEntryDataMaintenance> maintainers) {
    this.maintainers = maintainers;
  }

  public Flux<BookingEntryDTO> maintainData(BookingEntryDTO bookingEntries) {
    return Flux.fromIterable(maintainers)
        .reduce(bookingEntries, (l, r) -> r.applyToBookEntry(l))
        .flux();
  }
}
