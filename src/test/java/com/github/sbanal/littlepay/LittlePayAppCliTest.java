package com.github.sbanal.littlepay;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LittlePayAppCliTest {

    @Test
    public void processCommand_withIncompleteInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, LittlePayAppCli::main);
        assertEquals("Invalid argument, usage: " +
                "./littlepay <trip cost csv file> <input taps csv file> <output trips csv file>", ex.getMessage());
    }

    @Test
    public void processCommand_withValidInputOutputFiles() throws IOException {
        String outputTripsFile = "src/test/resources/trips.csv";
        LittlePayAppCli.main(
                "src/test/resources/trip-cost.csv",
                "src/test/resources/taps.csv",
                "src/test/resources/trips.csv");

        String content = Files.readString(Paths.get(outputTripsFile));
        String expectedContent = """
                Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status
                22-01-2023 13:00:00,22-01-2023 13:05:00,300,Stop1,Stop2,$3.25,Company1,Bus37,5500005555555559,COMPLETED
                22-01-2023 09:20:00,,,Stop3,,$7.30,Company1,Bus36,4111111111111111,INCOMPLETE
                23-01-2023 08:00:00,23-01-2023 08:02:00,120,Stop1,Stop1,$0.00,Company1,Bus37,4111111111111111,CANCELLED
                """;
        assertEquals(expectedContent, content);
    }

}