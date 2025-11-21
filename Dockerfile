FROM eclipse-temurin:21

WORKDIR /app

RUN mkdir -p /app/camt/done
RUN mkdir -p /app/import/done
RUN mkdir -p /app/export
RUN mkdir -p /app/google

COPY importer/build/libs/importer-*.jar /app/importer.jar
COPY app/google/credentials.json /app/google

ENTRYPOINT ["java", "-jar", "importer.jar"]
