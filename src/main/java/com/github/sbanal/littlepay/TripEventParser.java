package com.github.sbanal.littlepay;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TripEventParser {

    private static final DateTimeFormatter UTC_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static List<TripEvent> parse(Reader inputReader) throws IOException {
        List<TripEvent> tripEvents = new ArrayList<>();
        try (Reader reader = inputReader) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim());

            for (CSVRecord csvRecord : csvParser) {
                try {
                    Long id = Long.parseLong(csvRecord.get("ID"));
                    Instant dateTimeUTC;
                    try {
                        dateTimeUTC = LocalDateTime.parse(csvRecord.get("DateTimeUTC"), UTC_DATE_FORMATTER)
                            .toInstant(ZoneOffset.UTC);
                    } catch (DateTimeParseException e) {
                        throw new InvalidTripEventException("Invalid trip event DateTimeUTC for record " + id, e);
                    }
                    TapType tapType;
                    try {
                        tapType = TapType.valueOf(csvRecord.get("TapType"));
                    } catch (IllegalArgumentException e) {
                        throw new InvalidTripEventException("Invalid trip event TapType for record " + id, e);
                    }
                    String stopId = csvRecord.get("StopId");
                    if (StringUtils.isEmpty(stopId)) {
                        throw new InvalidTripEventException("Invalid trip event StopId for record " + id);
                    }
                    String companyId = csvRecord.get("CompanyId");
                    if (StringUtils.isEmpty(companyId)) {
                        throw new InvalidTripEventException("Invalid trip event CompanyId for record " + id);
                    }
                    String busId = csvRecord.get("BusID");
                    if (StringUtils.isEmpty(busId)) {
                        throw new InvalidTripEventException("Invalid trip event BusID for record " + id);
                    }
                    String pan = csvRecord.get("PAN");
                    if (StringUtils.isEmpty(pan)) {
                        throw new InvalidTripEventException("Invalid trip event PAN for record " + id);
                    }
                    tripEvents.add(new TripEvent(id, dateTimeUTC, tapType, stopId, companyId, busId, pan));
                } catch (InvalidTripEventException e) {
                    throw e;
                } catch (NumberFormatException e) {
                    throw new InvalidTripEventException("Invalid trip event ID for record " + csvRecord.get("ID"), e);
                } catch (Exception e) {
                    throw new InvalidTripEventException("Invalid trip event", e);
                }
            }
            return tripEvents;
        }
    }

}
