# LittlePay Project

## Requirements
This code is built and tested using the following toolset:
* Gradle 8.10.1
* Java SE SDK 22.0.2

## Assumptions
* Input taps.csv is valid and the order of the records are based on tap DateTimeUTC
* In taps.csv, both tap on and out event records have the same values for columns Company Id and Bus ID. No evaluation of any kind is done on this and is expected to be valid. The code uses the tap on event company id and bus id columns values when writing the trips event record
* In taps.csv, PAN is valid and unique and should be the same one used for Tap On and Tap Off events. No validation of any kind is done for this column since it seems validation is irrelevant to the problem
* In trips.csv, empty string is written on Finished, DurationSecs and ToStopId for INCOMPLETE Status trips

## Design Decisions
### Core Classes
* TripEvent - represents the tap on and tap off events record in taps.csv
* TripCompletionEvent - represents the trip completion event record in trips.csv
* TripEventReader - is the reader class which parses the taps.csv file into a Java Object TripEvent
* TripCompletionEventWriter - is the writer class which writes TripCompletionEvent objects into the trips.csv file

### Services
* TripCostService - contains the in-memory cache of the trip cost of each route and calculates the route cost
* TripEventService - processes the tap events by reading the events using TripEventReader and calculate the cost of trip using the TripCostService, then outputs the trips record using the TripCompletionEventWriter

### CLI integration Classes
* LittlePayAppCli - contains the main method which accepts as parameter the input trip cost csv file, the input taps.csv file and the output trips.csv file, providing less than three parameters or invalid file will throw an exception

## Limitations
* This application reads all the records in the input taps.csv in memory. The input file size should be small enough to be loaded by JVM based on memory heap size configured. For Example, `java -Xmx2G -Xms1G -jar yourApp.jar`.
* This application was not tested for large file input taps.csv

## Test and Test Coverage
* Unit test has bean written for all reader and writer classes
* Unit test has been written for all service classes
* Unit test has been written for the main class
* Test coverage 100% based on IntelliJ Test Coverage profiling

## Algorithm Analysis
* The time cost for calculating a completed, incomplete, cancelled trip cost is O(1) given that it uses a map that retrieve the cost of the trip. In this map, the key is the tap on stop id and the tap off stop id of the trip which maps to a cost. For incomplete trip cost, the tap off stop id is null which maps to a pre-calculated cost based on the max cost of all possible routes.
* The space cost for required for calculating the trip cost is O(E) given that the size of the map used to store the cost is equivalent to the number of edges between each possible route
* The time cost for processing the taps events is O(N) where N is the number of records in the taps.csv file
* The space cost for processing the taps events is O(N) where N is the number of records read from the taps.csv file 

## Build & Run Tests
```
gradle clean build
```
Distribution is found in folder build/distributions/littlepay-1.0-SNAPSHOT.zip

## Run
```
unzip build/distributions/littlepay-1.0-SNAPSHOT.zip
```

### Unix
```
./littlepay-1.0-SNAPSHOT/bin/littlepay src/test/resources/trip-cost.csv src/test/resources/taps.csv src/test/resources/trips.csv
```

### Windows
```
./littlepay-1.0-SNAPSHOT/bin/littlepay.bat src/test/resources/trip-cost.csv src/test/resources/taps.csv src/test/resources/trips.csv
```
