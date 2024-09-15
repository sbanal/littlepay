# LittlePay Project

## Requirements
This code is built and tested using the following toolset:
* Gradle 8.10.1
* Java SE SDK 22.0.2

## Assumptions
* Input taps.csv is valid and the order of the records are based on tap DateTimeUTC
* The taps.csv Tap in and out events for columns Company Id and Bus ID are the same, no evaluation of any kind is done on this
* The taps.csv PAN is valid and unique and should be the same one used for Tap On and Tap Off events, no validation of any kind is done for this column since it seems validation is irrelevant to the problem
* In trips.csv empty string is written on Finished, DurationSecs and ToStopId for INCOMPLETE Status trips

## Design Decisions
### Core Classes
* TripEvent - represents the tap on and tap off events record in taps.csv
* TripCompletionEvent - represents the trip completion event record in trips.csv

### Services
* TripCostService - contains the in-memory cache of the trip cost of each route and calculates the route cost
* TripEventService - processes the tap events by reading the events using TripEventReader and calculate the cost of trip using the TripCostService, then outputs the trips record using the TripCompletionEventWriter

### CLI integration Classes
* LittlePayAppCli - contains the main methid which accepts as parameter the input trip cost csv file, the input taps.csv file and the output trips.csv file, providing less than three parameters or invalid file will throw an exception

## Limitations
* This application reads all the records in the input taps.csv in memory. The input file size should be small enough to be loaded by JVM based on memory heap size configured. For Example, `java -Xmx2G -Xms1G -jar yourApp.jar`.
* This application was not tested for large file input taps.csv

## Test and Test Coverage
* Unit test has bean written for all reader and writer classes
* Unit test has been written for all service classes
* Unit test has been written for the main class
* Test coverage 100% based on IntelliJ Test Coverage profiling

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
