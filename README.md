# LittlePayTripProcessor

This accepts a welformed CSV card tap on file using the command line arguments and calaculate the trip details.

Asumptions
1. The input CSV file is well-formed and contains all the required data fields (ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN).
2. The input file only contains valid data, and there are no missing or incorrect values.

V3:
1. Test cases added
2. Input and output files added

V2: What does it handles
1. Tap records starting with OFF events
2. Tap records with out of order events
3. Incomplete and cancelled trips


V1: Wht does it handles (depreceiated)
1. Only records starting with an ON reord
2. Only records that are in order.

How to run this code:

1. Create or drop the tap file with the correct format to a file location
2. Copy the file location
3. Compile the LittlePayTripProcessor class (javac LittlePayTripProcessor)
4. Run the application with the file location copied from step 2 as an argument.
5. LittlepayTrupProcessor /path/to/your/input/file.csv
6. Output file be created in the root directory called : LittlePayTrips.csv
