package com.marvin.camt.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

@Component
public class CamtFileParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(CamtFileParser.class);

  private static Flux<ByteArrayOutputStream> readNextZipEntry(ZipInputStream zipInputStream) {
    return Flux.generate(sink -> readNextZipEntry(zipInputStream, sink));
  }

  private static void readNextZipEntry(ZipInputStream zipInputStream,
      SynchronousSink<ByteArrayOutputStream> sink) {
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      if (entry != null) {

        LOGGER.info("Reading file: " + entry);

        if (!entry.isDirectory()) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          byte[] buffer = new byte[1024];
          int len;
          while ((len = zipInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
          }
          zipInputStream.closeEntry();
          sink.next(outputStream);
        }
      } else {
        sink.complete();
      }
    } catch (IOException e) {
      LOGGER.error("Error processing ZipEntry!", e);
      sink.error(e);
    }
  }

  public Flux<ByteArrayOutputStream> unzipFile(InputStream file) {
    return Flux.usingWhen(
        Mono.fromCallable(() -> new ZipInputStream(file, StandardCharsets.UTF_8)),
        CamtFileParser::readNextZipEntry,
        zipInputStream -> Mono.fromRunnable(() -> {
          try {
            zipInputStream.close();
          } catch (IOException e) {
            LOGGER.error("Error closing ZipInputStream!", e);
          }
        }).subscribeOn(Schedulers.boundedElastic())
    );
  }
}
