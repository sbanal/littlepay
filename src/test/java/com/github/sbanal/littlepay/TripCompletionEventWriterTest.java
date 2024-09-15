package com.github.sbanal.littlepay;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripCompletionEventWriterTest {

    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    static String formatToUtcStr(Instant instant) {
        return instant.atZone(ZoneId.of("UTC")).format(UTC_FORMATTER);
    }

    @Test
    void write_withCompletedTrip_shouldWriteToWriterWithColumns() throws IOException {
        StringWriter stringWriter = new StringWriter();
        Instant tapOnDateTime = Instant.now();
        Instant tapOffDateTime = tapOnDateTime.plusSeconds(600);

        try (TripCompletionEventWriter tripCompletionEventWriter = new TripCompletionEventWriter(stringWriter)) {
            TripCompletionEvent tripCompletionEvent = new TripCompletionEvent(
                    tapOnDateTime,
                    tapOffDateTime,
                    100L,
                    "stop1",
                    "stop2",
                    1.23f,
                    "company1",
                    "bus1",
                    "123123",
                    TripCompletionStatus.COMPLETED
            );
            tripCompletionEventWriter.write(tripCompletionEvent);
        }

        String expectedOutput = String.format(
                """
                        Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status
                        %s,%s,100,stop1,stop2,$1.23,company1,bus1,123123,COMPLETED
                        """,
                formatToUtcStr(tapOnDateTime), formatToUtcStr(tapOffDateTime));
        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void write_withIncompleteTrip_shouldWriteToWriterWithColumns() throws IOException {
        StringWriter stringWriter = new StringWriter();
        Instant tapOnDateTime = Instant.now();

        try (TripCompletionEventWriter tripCompletionEventWriter = new TripCompletionEventWriter(stringWriter)) {
            TripCompletionEvent tripCompletionEvent = new TripCompletionEvent(
                    tapOnDateTime,
                    null,
                    null,
                    "stop1",
                    null,
                    1.23f,
                    "company1",
                    "bus1",
                    "123123",
                    TripCompletionStatus.INCOMPLETE
            );
            tripCompletionEventWriter.write(tripCompletionEvent);
        }

        String expectedOutput = String.format(
                """
                        Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status
                        %s,,,stop1,,$1.23,company1,bus1,123123,INCOMPLETE
                        """,
                formatToUtcStr(tapOnDateTime));
        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void write_withCancelledTrip_shouldWriteToWriterWithColumns() throws IOException {
        StringWriter stringWriter = new StringWriter();
        Instant tapOnDateTime = Instant.now();
        Instant tapOffDateTime = tapOnDateTime.plusSeconds(600);

        try (TripCompletionEventWriter tripCompletionEventWriter = new TripCompletionEventWriter(stringWriter)) {
            TripCompletionEvent tripCompletionEvent = new TripCompletionEvent(
                    tapOnDateTime,
                    tapOffDateTime,
                    100L,
                    "stop1",
                    "stop1",
                    0.0f,
                    "company1",
                    "bus1",
                    "123123",
                    TripCompletionStatus.CANCELLED
            );
            tripCompletionEventWriter.write(tripCompletionEvent);
        }

        String expectedOutput = String.format(
                """
                        Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status
                        %s,%s,100,stop1,stop1,$0.00,company1,bus1,123123,CANCELLED
                        """,
                formatToUtcStr(tapOnDateTime), formatToUtcStr(tapOffDateTime));
        assertEquals(expectedOutput, stringWriter.toString());
    }
}