package com.marvin.app.controller;

import com.marvin.app.importer.DailyCostImportService;
import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.camt.model.book_entry.BookingsDTO;
import com.marvin.camt.model.book_entry.CreditDebitCodeDTO;
import com.marvin.camt.model.book_entry.MonthlyBookingEntriesDTO;
import com.marvin.camt.parser.CamtFileParser;
import com.marvin.camt.parser.DocumentUnmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CamtController {

  private final CamtFileParser camtFileParser;
  private final DocumentUnmarshaller documentUnmarshaller;

  public CamtController(CamtFileParser camtFileParser,
      DocumentUnmarshaller documentUnmarshaller) {
    this.camtFileParser = camtFileParser;
    this.documentUnmarshaller = documentUnmarshaller;
  }

  private static String replaceSpaces(String value) {
    return value.replaceAll("\\s+", " ");
  }

  @PostMapping(
      path = "/camt-entries",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<BookingsDTO> bookings(@RequestPart("file") Mono<FilePart> fileMono) {

    return fileMono.flatMap(file -> {

      if (!Objects.requireNonNull(file.filename()).toLowerCase().endsWith(".zip")) {
        return Mono.error(new IllegalArgumentException("Only zip files are allowed"));
      }

      return DataBufferUtils.join(file.content())
          .flatMapMany(dataBuffer -> {

            Flux<ByteArrayOutputStream> using = Flux.using(
                dataBuffer::asInputStream,
                camtFileParser::unzipFile,
                inputStream -> {
                  try {
                    inputStream.close();
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                }
            );

            DataBufferUtils.release(dataBuffer);

            return using;
          })
          .flatMap(fileContent -> unmarshallBookings(fileContent)
              .map(bookingEntryDTO -> new BookingEntryDTO(
                  bookingEntryDTO.creditDebitCode(),
                  bookingEntryDTO.entryInfo(),
                  bookingEntryDTO.amount(),
                  bookingEntryDTO.bookingDate(),
                  bookingEntryDTO.firstOfMonth(),
                  replaceSpaces(bookingEntryDTO.debitName()),
                  bookingEntryDTO.debitIban(),
                  replaceSpaces(bookingEntryDTO.creditName()),
                  bookingEntryDTO.creditIban(),
                  bookingEntryDTO.additionalInfo()
              ))
          )
          .collectList()
          .map(this::getBookingsDTO);
    });
  }

  private Flux<BookingEntryDTO> unmarshallBookings(ByteArrayOutputStream fileContent) {
    try {
      return documentUnmarshaller.unmarshallFile(Flux.just(fileContent));
    } catch (Exception e) {
      return Flux.error(e);
    }
  }

  private BookingsDTO getBookingsDTO(List<BookingEntryDTO> bookings) {

    final List<MonthlyBookingEntriesDTO> dtos = bookings.stream()
        .collect(Collectors.groupingBy(BookingEntryDTO::firstOfMonth))
        .entrySet().stream()
        .map(entry -> {

          final List<BookingEntryDTO> usualBookings = new ArrayList<>();
          final List<BookingEntryDTO> dailyCosts = new ArrayList<>();
          final List<BookingEntryDTO> incomes = new ArrayList<>();

          entry.getValue().forEach(dto -> {
            if (DailyCostImportService.PATTERN.matcher(dto.creditName()).matches()) {
              dailyCosts.add(dto);
            } else if (CreditDebitCodeDTO.CRDT == dto.creditDebitCode()) {
              incomes.add(dto);
            } else {
              usualBookings.add(dto);
            }
          });

          final LocalDate date = entry.getKey();

          return new MonthlyBookingEntriesDTO(
              date.getYear(), date.getMonth().getValue(),
              usualBookings, dailyCosts, incomes
          );
        }).toList();

    return new BookingsDTO(dtos);
  }
}
