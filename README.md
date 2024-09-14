# LittlePay Project

## Requirements
This code is built and tested using the following toolset:
* Gradle 7.1.1
* Java SE SDK v15+

## Assumptions
* 

## Design Decisions
### Core Classes
* 

### CLI integration Classes
* 

## Extensibility
* 

## Test
* 

## Build & Run Tests
```
gradle clean build
```
Distribution is found in folder build/distributions/littlepay-1.0-SNAPSHOT.zip

A package under the same folder already exist and you can opt not to execute the command above.

## Run
```
unzip build/distributions/littlepay-1.0-SNAPSHOT.zip
```

### Unix
```
./littlepay-1.0-SNAPSHOT/bin/littlepay
./littlepay-1.0-SNAPSHOT/bin/littlepay src/test/resources/taps.csv
```

### Windows
```
./littlepay-1.0-SNAPSHOT/bin/littlepay.bat
./littlepay-1.0-SNAPSHOT/bin/littlepay.bat src/test/resources/trips.csv
```
