package com.github.sbanal.littlepay;


import java.io.*;

public class LittlePayAppCli {

    public static void main(String... args) throws IOException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Invalid argument, usage: " +
                    "java -jar littlepay-cli.jar <trip cost csv> <input csv file> <output csv file>");
        }
        LittlePayAppCli littlePayAppCli = new LittlePayAppCli();
        littlePayAppCli.processCommand(args[0], args[1], args[2]);
    }

    public void processCommand(String tripCostCsv, String inputCsv, String outputCsv) throws IOException {
        TripCostService tripCostService = new TripCostService();
        try (Reader tripCostReader = new FileReader(tripCostCsv);
             Reader inputCsvReader = new FileReader(inputCsv);
             Writer outputCsvWriter = new FileWriter(outputCsv)) {
            tripCostService.load(tripCostReader);

            TripEventReader tripEventReader = new TripEventReader(inputCsvReader);
            TripCompletionEventWriter tripCompletionEventWriter = new TripCompletionEventWriter(outputCsvWriter);

            TripEventService tripEventService = new TripEventService(tripCostService);
            tripEventService.processEvents(tripEventReader, tripCompletionEventWriter);
        }
    }

}
