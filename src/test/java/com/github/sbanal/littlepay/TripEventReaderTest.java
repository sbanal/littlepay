package com.github.sbanal.littlepay;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripEventReaderTest {

    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    static String formatToUtcStr(Instant instant) {
        return instant.atZone(ZoneId.of("UTC")).format(UTC_FORMATTER);
    }

    @Test
    void parse_withInvalidCsvColumns_shouldThrowException() {
        StringReader stringReader = new StringReader("some, invalid, csv\n" +
                "test, test, test");

        assertThrows(InvalidTripEventException.class, () -> new TripEventReader(stringReader).readEvents());
    }

    @Test
    void parse_withValidFormat_shouldReturnTripEvents() throws IOException {
        StringReader stringReader = new StringReader("""
                ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
                1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559\s
                2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559""");

        List<TripEvent> tripEventList = new TripEventReader(stringReader).readEvents();
        assertNotNull(tripEventList);
        assertEquals(2, tripEventList.size());

        assertEquals(1, tripEventList.get(0).id());
        assertEquals("22-01-2023 13:00:00", formatToUtcStr(tripEventList.get(0).dateTimeUtc()));
        assertEquals(TapType.ON, tripEventList.get(0).tapType());
        assertEquals("Stop1", tripEventList.get(0).stopId());
        assertEquals("Company1", tripEventList.get(0).companyId());
        assertEquals("Bus37", tripEventList.get(0).busId());
        assertEquals("5500005555555559", tripEventList.get(0).pan());

        assertEquals(2, tripEventList.get(1).id());
        assertEquals("22-01-2023 13:05:00", formatToUtcStr(tripEventList.get(1).dateTimeUtc()));
        assertEquals(TapType.OFF, tripEventList.get(1).tapType());
        assertEquals("Stop2", tripEventList.get(1).stopId());
        assertEquals("Company1", tripEventList.get(1).companyId());
        assertEquals("Bus37", tripEventList.get(1).busId());
        assertEquals("5500005555555559", tripEventList.get(1).pan());
    }

    @Test
    void parse_withInvalidIdValue_shouldReturnTripEvents() {
        StringReader stringReader = new StringReader("ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n" +
                "1a, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559");

        InvalidTripEventException ex = assertThrows(InvalidTripEventException.class,
                () -> new TripEventReader(stringReader).readEvents());
        assertEquals("Invalid trip event ID for record 1a", ex.getMessage());
    }

    @Test
    void parse_withInvalidDateTimeValue_shouldReturnTripEvents() {
        StringReader stringReader = new StringReader("ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n" +
                "1, 22-01-2023a 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559");

        InvalidTripEventException ex = assertThrows(InvalidTripEventException.class,
                () -> new TripEventReader(stringReader).readEvents());
        assertEquals("Invalid trip event DateTimeUTC for record 1", ex.getMessage());
    }

    @Test
    void parse_withInvalidTapTypeValue_shouldReturnTripEvents() {
        StringReader stringReader = new StringReader("ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n" +
                "1, 22-01-2023 13:00:00, invalid, Stop1, Company1, Bus37, 5500005555555559");

        InvalidTripEventException ex = assertThrows(InvalidTripEventException.class,
                () -> new TripEventReader(stringReader).readEvents());
        assertEquals("Invalid trip event TapType for record 1", ex.getMessage());
    }

    @Test
    void parse_withInvalidStopIdValue_shouldReturnTripEvents() {
        StringReader stringReader = new StringReader("ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n" +
                "1, 22-01-2023 13:00:00, ON, , Company1, Bus37, 5500005555555559");

        InvalidTripEventException ex = assertThrows(InvalidTripEventException.class,
                () -> new TripEventReader(stringReader).readEvents());
        assertEquals("Invalid trip event StopId for record 1", ex.getMessage());
    }

    @Test
    void parse_withInvalidCompanyIdIdValue_shouldReturnTripEvents() {
        StringReader stringReader = new StringReader("ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n" +
                "1, 22-01-2023 13:00:00, ON, Stop1, , Bus37, 5500005555555559");

        InvalidTripEventException ex = assertThrows(InvalidTripEventException.class,
                () -> new TripEventReader(stringReader).readEvents());
        assertEquals("Invalid trip event CompanyId for record 1", ex.getMessage());
    }

    @Test
    void parse_withInvalidBusIdIdValue_shouldReturnTripEvents() {
        StringReader stringReader = new StringReader("ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n" +
                "1, 22-01-2023 13:00:00, ON, Stop1, Company1, , 5500005555555559");

        InvalidTripEventException ex = assertThrows(InvalidTripEventException.class,
                () -> new TripEventReader(stringReader).readEvents());
        assertEquals("Invalid trip event BusID for record 1", ex.getMessage());
    }

    @Test
    void parse_withInvalidPanValue_shouldReturnTripEvents() {
        StringReader stringReader = new StringReader("ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n" +
                "1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, ");

        InvalidTripEventException ex = assertThrows(InvalidTripEventException.class,
                () -> new TripEventReader(stringReader).readEvents());
        assertEquals("Invalid trip event PAN for record 1", ex.getMessage());
    }

}