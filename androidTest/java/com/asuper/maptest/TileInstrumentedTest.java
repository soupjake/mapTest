package com.asuper.maptest;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.UrlTileProvider;


import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TileInstrumentedTest {

    String testURL = "http://tile.openweathermap.org/map/temp_new/2/3/1.png?appid=69be65f65a5fabd4d745d0544b7b771e";
    URL url;
    String tileType = "temp_new";
    int zoomTest = 2;
    int xTest = 3;
    int yTest= 1;

    public final String APP_ID = "69be65f65a5fabd4d745d0544b7b771e";
    String OWM_TILE_URL = "http://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=" + APP_ID ;


    @Test
    public void useAppContext() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        UrlTileProvider test = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String fUrl = String.format(OWM_TILE_URL, tileType, zoom, x, y);
                try {
                    url = new URL(fUrl);
                } catch (MalformedURLException mfe) {
                    mfe.printStackTrace();
                }

                return url;
            }
        };
        test.getTileUrl(xTest, yTest, zoomTest);

        assertEquals(testURL, url.toString());
    }

}

