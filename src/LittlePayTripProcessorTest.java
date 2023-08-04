import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.*;

class LittlePayTripProcessorTest {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Test
    void testGetMaxCharge() throws Exception {
        // Create an instance of the LittlePayTripProcessor class
        LittlePayTripProcessor littlePayTripProcessor = new LittlePayTripProcessor();

        // Access the private getMaxCharge method using reflection
        Method getMaxChargeMethod = LittlePayTripProcessor.class.getDeclaredMethod("getMaxCharge", Tap.class, Tap.class);
        getMaxChargeMethod.setAccessible(true);

        // Create sample Tap objects for testing
        Tap tap1 = new Tap();
        tap1.stopId = "Stop1";

        Tap tap2 = new Tap();
        tap2.stopId = "Stop2";

        Tap tap3 = new Tap();
        tap3.stopId = "Stop3";

        // Test different combinations of stop IDs and verify the max charge
        double charge1 = (double) getMaxChargeMethod.invoke(littlePayTripProcessor, tap1, tap2);
        assertEquals(3.25, charge1);

        double charge2 = (double) getMaxChargeMethod.invoke(littlePayTripProcessor, tap2, tap1);
        assertEquals(3.25, charge2);

        double charge3 = (double) getMaxChargeMethod.invoke(littlePayTripProcessor, tap2, tap3);
        assertEquals(5.50, charge3);

        double charge4 = (double) getMaxChargeMethod.invoke(littlePayTripProcessor, tap3, tap2);
        assertEquals(5.50, charge4);

        double charge5 = (double) getMaxChargeMethod.invoke(littlePayTripProcessor, tap1, tap3);
        assertEquals(7.30, charge5);

        double charge6 = (double) getMaxChargeMethod.invoke(littlePayTripProcessor, tap3, tap1);
        assertEquals(7.30, charge6);
    }

    @Test
    void testCompletedTrip() throws ParseException {
        Tap tapOn = createTap("ON", "Stop1", "5500005555555559", "Bus37", "22-01-2023 13:00:00");
        Tap tapOff = createTap("OFF", "Stop2", "5500005555555559", "Bus37", "22-01-2023 13:05:00");

        Trip[] trips = processTaps(tapOn, tapOff);

        assertEquals(1, trips.length);
        assertEquals("Stop1", trips[0].fromStop);
        assertEquals("Stop2", trips[0].toStop);
        assertEquals(3.25, trips[0].chargeAmount);
        assertEquals("COMPLETED", trips[0].status);
    }

    @Test
    void testIncompleteTrip() throws ParseException {
        Tap tapOn = createTap("ON", "Stop1", "5500005555555559", "Bus37", "22-01-2023 13:00:00");

        Trip[] trips = processTaps(tapOn);

        assertEquals(1, trips.length);
        assertEquals("Stop1", trips[0].fromStop);
        assertNull(trips[0].toStop);
        assertEquals(7.30, trips[0].chargeAmount);
        assertEquals("INCOMPLETE", trips[0].status);
    }

    @Test
    void testCancelledTrip() throws ParseException {
        Tap tapOn = createTap("ON", "Stop1", "5500005555555559", "Bus37", "22-01-2023 13:00:00");
        Tap tapOff = createTap("OFF", "Stop1", "5500005555555559", "Bus37", "22-01-2023 13:05:00");

        Trip[] trips = processTaps(tapOn, tapOff);

        assertEquals(1, trips.length);
        assertEquals("Stop1", trips[0].fromStop);
        assertNull(trips[0].toStop);
        assertEquals(0.0, trips[0].chargeAmount);
        assertEquals("CANCELLED", trips[0].status);
    }

    @Test
    void testMultipleTrips() throws ParseException {
        Tap tapOn1 = createTap("ON", "Stop1", "5500005555555559", "Bus37", "22-01-2023 13:00:00");
        Tap tapOff1 = createTap("OFF", "Stop2", "5500005555555559", "Bus37", "22-01-2023 13:05:00");
        Tap tapOn2 = createTap("ON", "Stop3", "4111111111111111", "Bus36", "22-01-2023 09:20:00");

        Trip[] trips = processTaps(tapOn1, tapOff1, tapOn2);

        assertEquals(2, trips.length);
        assertEquals("Stop1", trips[0].fromStop);
        assertEquals("Stop2", trips[0].toStop);
        assertEquals(3.25, trips[0].chargeAmount);
        assertEquals("COMPLETED", trips[0].status);

        assertEquals("Stop3", trips[1].fromStop);
        assertNull(trips[1].toStop);
        assertEquals(7.30, trips[1].chargeAmount);
        assertEquals("INCOMPLETE", trips[1].status);
    }


    private Tap createTap(String tapType, String stopId, String pan, String busId, String dateTime) throws ParseException {
        Tap tap = new Tap();
        tap.tapType = tapType;
        tap.stopId = stopId;
        tap.pan = pan;
        tap.busId = busId;
        tap.dateTime = dateFormat.parse(dateTime);
        return tap;
    }

    private Trip[] processTaps(Tap... taps) {
        LittlePayTripProcessor littlePayTripProcessor = new LittlePayTripProcessor();
        return littlePayTripProcessor.processTaps(taps);
    }
}