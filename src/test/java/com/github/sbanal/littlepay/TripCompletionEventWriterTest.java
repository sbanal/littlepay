package com.github.sbanal.littlepay;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class TripCompletionEventWriterTest {

    @Test
    void write_shouldWriteToWriterWithColumns() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        StringWriter stringWriter = new StringWriter();
        Instant timeNow = Instant.now();

        try (TripCompletionEventWriter tripCompletionEventWriter = new TripCompletionEventWriter(stringWriter)) {
            TripCompletionEvent tripCompletionEvent = new TripCompletionEvent(
                    timeNow,
                    timeNow,
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

        String dateTimeUtc = timeNow.atZone(ZoneId.of("UTC")).format(formatter);
        String expectedOutput = String.format(
                "Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status\n" +
                "%s,%s,100,stop1,stop2,$1.23,company1,bus1,123123,COMPLETED\n",
                dateTimeUtc, dateTimeUtc);
        assertEquals(expectedOutput, stringWriter.toString());
    }

}