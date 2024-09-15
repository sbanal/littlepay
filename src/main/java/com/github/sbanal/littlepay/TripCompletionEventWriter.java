package com.github.sbanal.littlepay;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

public class TripCompletionEventWriter implements Closeable {

    private static final DateTimeFormatter UTC_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    private static final String[] HEADERS = {
            "Started",
            "Finished",
            "DurationSecs",
            "FromStopId",
            "ToStopId",
            "ChargeAmount",
            "CompanyId",
            "BusID",
            "PAN",
            "Status"
    };
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("$0.00");

    private final Writer writer;
    private final CSVPrinter csvPrinter;

    public TripCompletionEventWriter(Writer writer) throws IOException {
        Objects.requireNonNull(writer, "writer cannot be null");
        this.writer = writer;
        this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader(HEADERS)
                .withDelimiter(',')
                .withRecordSeparator("\n"));
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.csvPrinter.close();
    }

    public void write(TripCompletionEvent completionEvent) throws IOException {
        String[] rowData = {
                completionEvent.started().atZone(UTC_ZONE_ID).format(UTC_DATE_FORMATTER),
                completionEvent.finished().atZone(UTC_ZONE_ID).format(UTC_DATE_FORMATTER),
                completionEvent.durationSecs().toString(),
                completionEvent.fromStopId(),
                completionEvent.toStopId(),
                DECIMAL_FORMAT.format(completionEvent.chargeAmount()),
                completionEvent.companyId(),
                completionEvent.busId(),
                completionEvent.pan(),
                completionEvent.status().name()
        };
        csvPrinter.printRecord(Arrays.asList(rowData));
    }

}
