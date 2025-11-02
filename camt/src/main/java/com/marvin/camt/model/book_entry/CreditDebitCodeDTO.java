package com.marvin.camt.model.book_entry;

public enum CreditDebitCodeDTO {

  CRDT,
  DBIT;

  public static CreditDebitCodeDTO fromValue(String v) {
    return valueOf(v);
  }

  public String value() {
    return name();
  }
}
