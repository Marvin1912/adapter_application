package com.marvin.camt.configuration.jaxb.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

  @Override
  public LocalDateTime unmarshal(String xmlGregorianCalendar) {
    return LocalDateTime.parse(xmlGregorianCalendar);
  }

  @Override
  public String marshal(LocalDateTime localDateTime) {
    return localDateTime.toString();
  }
}
