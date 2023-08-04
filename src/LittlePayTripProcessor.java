import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LittlePayTripProcessor {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the input file location as a command-line argument.");
            return;
        }

        String inputFile = args[0];
        String outputFile = "LittlePayTrips.csv";

        try {
            LittlePayTripProcessor littlePayTripProcessor = new LittlePayTripProcessor();
            // Read the input taps from the CSV file
            Tap[] taps = littlePayTripProcessor.readTapsFromFile(inputFile);

            // Process the taps to create trips
            Trip[] trips = littlePayTripProcessor.processTaps(taps);

            // Write the trips to the output CSV file
            littlePayTripProcessor.writeTripsToFile(outputFile, trips);

            System.out.println("Trips have been processed and written to " + outputFile);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static double getMaxCharge(Tap tapOn, Tap tapOff) {
        double maxCharge = 0.0;

        if ((tapOn.stopId.equals("Stop1") && tapOff.stopId.equals("Stop2")) ||
                (tapOn.stopId.equals("Stop2") && tapOff.stopId.equals("Stop1"))) {
            maxCharge = 3.25;
        } else if ((tapOn.stopId.equals("Stop2") && tapOff.stopId.equals("Stop3")) ||
                (tapOn.stopId.equals("Stop3") && tapOff.stopId.equals("Stop2"))) {
            maxCharge = 5.50;
        } else if ((tapOn.stopId.equals("Stop1") && tapOff.stopId.equals("Stop3")) ||
                (tapOn.stopId.equals("Stop3") && tapOff.stopId.equals("Stop1"))) {
            maxCharge = 7.30;
        }
        return maxCharge;
    }

    private Tap[] readTapsFromFile(String inputFile) throws IOException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String line;
        int lineCount = 0;
        int tapCount = 0;

        // Count the number of taps in the file
        while ((line = reader.readLine()) != null) {
            lineCount++;
        }
        reader.close();

        // Create an array to hold the taps
        Tap[] taps = new Tap[lineCount - 1]; // Exclude header line

        // Read the taps from the file
        reader = new BufferedReader(new FileReader(inputFile));
        // Skip the header line
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            String[] data = line.split(", ");

            Tap tap = new Tap();
            tap.id = Integer.parseInt(data[0]);
            tap.dateTime = dateFormat.parse(data[1]);
            tap.tapType = data[2];
            tap.stopId = data[3];
            tap.companyId = data[4];
            tap.busId = data[5];
            tap.pan = data[6];

            taps[tapCount++] = tap;
        }

        reader.close();
        return taps;
    }

    private Trip[] processTaps(Tap[] taps) {
        List<Trip> trips = new ArrayList<>();
        int i = 0;

        while (i < taps.length) {

            if (!taps[i].processed && taps[i].tapType.equals("OFF")) {
                // Skip the current tap as it is an OFF tap and should be matched with a previous ON tap
                trips.add(processIncompleteTrips(taps[i]));
                taps[i].processed = true;
                i++;
                continue;
            }

            Tap tapOn;
            if (!taps[i].processed && taps[i].tapType.equals("ON")) {
                tapOn = taps[i];
                int j = i + 1;
                boolean tapOffFound = false;

                // Find the corresponding tap off for the tap on
                while (j < taps.length) {
                    if (!taps[j].processed && taps[j].tapType.equals("OFF") && taps[j].pan.equals(tapOn.pan) && taps[j].busId.equals(tapOn.busId)) {
                        tapOffFound = true;
                        break;
                    }
                    j++;
                }

                Trip trip = new Trip();
                trip.startTime = tapOn.dateTime;
                trip.fromStop = tapOn.stopId;
                trip.companyId = tapOn.companyId;
                trip.busId = tapOn.busId;
                trip.pan = tapOn.pan;

                if (tapOffFound) {
                    // Check for cancelled trip (tap on and tap off at the same stop)
                    if (tapOn.stopId.equals(taps[j].stopId)) {
                        trip.endTime = null;
                        trip.toStop = null;
                        trip.chargeAmount = 0.0;
                        trip.status = "CANCELLED";

                    } else {
                        // Completed trip, set tap off details
                        Tap tapOff = taps[j];
                        trip.endTime = tapOff.dateTime;
                        trip.toStop = tapOff.stopId;

                        // Calculate charge based on stops
                        trip.chargeAmount = getMaxCharge(tapOn, tapOff);
                        trip.status = "COMPLETED";

                    }
                    taps[i].processed = true;
                    taps[j].processed = true;

                    // Move to the next pair of taps
                } else {
                    // Incomplete trip, set the charge to the maximum possible charge for the stop
                    double maxCharge = 0.0;

                    switch (tapOn.stopId) {
                        case "Stop1":
                        case "Stop3":
                            maxCharge = 7.30;
                            break;
                        case "Stop2":
                            maxCharge = 5.50;
                            break;

                    }

                    trip.endTime = null;
                    trip.toStop = null;
                    trip.chargeAmount = maxCharge;
                    trip.status = "INCOMPLETE";
                    taps[i].processed = true;


                    // Move to the next tap
                }
                i++;
                trips.add(trip);

            } else {
                i++;
            }


        }

        return trips.toArray(new Trip[0]);
    }

    private Trip processIncompleteTrips(Tap tap) {
        Trip trip = new Trip();
        trip.startTime = tap.dateTime;
        trip.fromStop = tap.stopId;
        trip.companyId = tap.companyId;
        trip.busId = tap.busId;
        trip.pan = tap.pan;
        // Incomplete trip, set the charge to the maximum possible charge for the stop

        double maxCharge = 0.0;

        switch (tap.stopId) {
            case "Stop1":
            case "Stop3":
                maxCharge = 7.30;
                break;
            case "Stop2":
                maxCharge = 5.50;
                break;

        }

        trip.endTime = null;
        trip.toStop = null;
        trip.chargeAmount = maxCharge;
        trip.status = "INCOMPLETE";

        return trip;
    }


    private void writeTripsToFile(String outputFile, Trip[] trips) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        writer.write("Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status");
        writer.newLine();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        for (Trip trip : trips) {
            String startTimeStr = dateFormat.format(trip.startTime);
            String endTimeStr = (trip.endTime != null) ? dateFormat.format(trip.endTime) : "";
            String durationSecsStr = (trip.endTime != null) ? String.valueOf((trip.endTime.getTime() - trip.startTime.getTime()) / 1000) : "";

            writer.write(startTimeStr + ", " + endTimeStr + ", " + durationSecsStr + ", " +
                    trip.fromStop + ", " + trip.toStop + ", $" + String.format("%.2f", trip.chargeAmount) + ", " +
                    trip.companyId + ", " + trip.busId + ", " + trip.pan + ", " + trip.status);
            writer.newLine();
        }

        writer.close();
    }
}