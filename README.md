# LittlePayTripProcessor

This accepts a welformed CSV card tap on file using the command line arguments and calaculate the trip details.

Asumptions
1. The input CSV file is well-formed and contains all the required data fields (ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN).
2. The input file only contains valid data, and there are no missing or incorrect values.

What does it handles
1. Tap records starting with OFF events
2. Tap records with out of order events
3. Incomplete and cancelled trips
