package com.asuper.maptest;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.UrlTileProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LocationInstrumentedTest {

    double latTest = 51.23;
    double lonTest = -3.21;
    double locationLat;
    double locationLon;

    @Test
    public void useAppContext() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeTestProvider("Test");
        locationManager.addTestProvider("Test", false, false, false, false, false, false, false, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
        locationManager.setTestProviderEnabled("Test", true);

        Location location = new Location("Test");
        location.setLatitude(latTest);
        location.setLongitude(lonTest);
        Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");
        if (locationJellyBeanFixMethod != null) {
            locationJellyBeanFixMethod.invoke(location);
        }
        locationManager.setTestProviderLocation("Test", location);
        locationLat = location.getLatitude();
        locationLon = location.getLongitude();
        assertEquals(latTest, locationLat, 0);
        assertEquals(lonTest, locationLon, 0);

    }

}

