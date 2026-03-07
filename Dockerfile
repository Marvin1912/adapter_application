FROM eclipse-temurin:21

WORKDIR /app

RUN apt-get update && apt-get install -y \
    postgresql-client \
    openssl \
    gzip \
    zip \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/camt/done
RUN mkdir -p /app/import/done
RUN mkdir -p /app/export
RUN mkdir -p /app/google
RUN mkdir -p /app/backup/keys

COPY boot/build/libs/backend.jar /app/backend.jar
COPY backup/backup.sh /app/backup/backup.sh
COPY backup/keys/backup_public.pem /app/backup/keys/backup_public.pem

RUN chmod +x /app/backup/backup.sh

ENTRYPOINT ["java", "-jar", "backend.jar"]
