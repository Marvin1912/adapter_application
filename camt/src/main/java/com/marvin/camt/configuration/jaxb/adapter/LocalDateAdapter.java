package com.marvin.camt.configuration.jaxb.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

  @Override
  public LocalDate unmarshal(String xmlGregorianCalendar) {
    return LocalDate.parse(xmlGregorianCalendar);
  }

  @Override
  public String marshal(LocalDate localDate) {
    return localDate.toString();
  }
}
