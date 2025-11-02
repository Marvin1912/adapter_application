package com.marvin.camt.parser;

import com.marvin.camt.model.book_entry.BookingEntryDTO;
import com.marvin.camt.model.book_entry.CreditDebitCodeDTO;
import com.marvin.common.util.NullSafeUtil;
import iso.std.iso._20022.tech.xsd.camt_052_001.AccountIdentification4Choice;
import iso.std.iso._20022.tech.xsd.camt_052_001.AccountReport25;
import iso.std.iso._20022.tech.xsd.camt_052_001.ActiveOrHistoricCurrencyAndAmount;
import iso.std.iso._20022.tech.xsd.camt_052_001.CashAccount38;
import iso.std.iso._20022.tech.xsd.camt_052_001.Document;
import iso.std.iso._20022.tech.xsd.camt_052_001.EntryDetails9;
import iso.std.iso._20022.tech.xsd.camt_052_001.EntryTransaction10;
import iso.std.iso._20022.tech.xsd.camt_052_001.Party40Choice;
import iso.std.iso._20022.tech.xsd.camt_052_001.PartyIdentification135;
import iso.std.iso._20022.tech.xsd.camt_052_001.RemittanceInformation16;
import iso.std.iso._20022.tech.xsd.camt_052_001.ReportEntry10;
import iso.std.iso._20022.tech.xsd.camt_052_001.TransactionParties6;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class DocumentUnmarshaller {

  private static Flux<BookingEntryDTO> getBookingEntries(AccountReport25 accountReport) {
    return Flux.fromIterable(accountReport.getNtries())
        .flatMap(DocumentUnmarshaller::getBookingEntries);
  }

  private static Flux<BookingEntryDTO> getBookingEntries(ReportEntry10 entry) {

    var creditDebitCode = entry.getCdtDbtInd().value();
    var entryInfo = entry.getAddtlNtryInf();
    var bookingDate = entry.getBookgDt().getDt();

    return Flux.fromIterable(entry.getNtryDtls())
        .flatMap(entryDetail -> getBookingEntries(creditDebitCode, entryInfo, bookingDate,
            entryDetail));
  }

  private static Flux<BookingEntryDTO> getBookingEntries(String creditDebitCode, String entryInfo,
      LocalDate bookingDate, EntryDetails9 entryDetail) {
    return Flux.fromIterable(entryDetail.getTxDtls())
        .map(entryTransaction -> getBookingEntry(entryTransaction, creditDebitCode,
            bookingDate, entryInfo));
  }

  private static BookingEntryDTO getBookingEntry(EntryTransaction10 entryTransaction,
      String creditDebitCode, LocalDate bookingDate, String entryInfo) {

    var amount = NullSafeUtil.eval(null, entryTransaction.getAmt(),
        ActiveOrHistoricCurrencyAndAmount::getValue);

    String debitName = NullSafeUtil.eval(
        null,
        entryTransaction.getRltdPties(),
        TransactionParties6::getDbtr,
        Party40Choice::getPty,
        PartyIdentification135::getNm
    );

    String debitIban = NullSafeUtil.eval(
        null,
        entryTransaction.getRltdPties(),
        TransactionParties6::getDbtrAcct,
        CashAccount38::getId,
        AccountIdentification4Choice::getIBAN
    );

    String creditName = NullSafeUtil.eval(
        null,
        entryTransaction.getRltdPties(),
        TransactionParties6::getCdtr,
        Party40Choice::getPty,
        PartyIdentification135::getNm
    );

    String creditIban = NullSafeUtil.eval(
        null,
        entryTransaction.getRltdPties(),
        TransactionParties6::getCdtrAcct,
        CashAccount38::getId,
        AccountIdentification4Choice::getIBAN
    );

    String additionalInfo = NullSafeUtil.eval(
        null,
        entryTransaction.getRmtInf(),
        RemittanceInformation16::getUstrds,
        strings -> String.join(", ", strings)
    );

    return new BookingEntryDTO(
        CreditDebitCodeDTO.fromValue(creditDebitCode),
        entryInfo,
        amount,
        bookingDate,
        LocalDate.of(bookingDate.getYear(), bookingDate.getMonth(), 1),
        debitName,
        debitIban,
        creditName,
        creditIban,
        additionalInfo
    );
  }

  public Flux<BookingEntryDTO> unmarshallFile(Flux<ByteArrayOutputStream> zipFileStream)
      throws Exception {

    JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);

    return zipFileStream
        .flatMap(file ->
            readDocument(jaxbContext, file)
                .flatMap(this::getBookingEntries)
                .filter(bookingEntryDTO -> bookingEntryDTO.debitName() != null)
        );
  }

  private Flux<Document> readDocument(JAXBContext jaxbContext, ByteArrayOutputStream file) {
    return Mono.fromCallable(() -> {
          Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
          return (Document) jaxbUnmarshaller.unmarshal(
              new ByteArrayInputStream(file.toByteArray()));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flux();
  }

  private Flux<BookingEntryDTO> getBookingEntries(Document document) {
    return Flux.fromIterable(document.getBkToCstmrAcctRpt().getRpts())
        .flatMap(DocumentUnmarshaller::getBookingEntries);
  }

}
