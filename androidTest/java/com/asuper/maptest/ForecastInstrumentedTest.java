package com.asuper.maptest;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ForecastInstrumentedTest {

    String condition = "Clouds";
    String test1 = "";
    String test2 = "";
    double mLat = 51.48686;
    double mLon = -3.2137181;
    public final String APP_ID = "69be65f65a5fabd4d745d0544b7b771e";

    @Test
    public void useAppContext() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        String urlOpen = "http://api.openweathermap.org/data/2.5/forecast?lat=" + mLat + "&lon="
                + mLon + "&units=metric&appid=" + APP_ID;

        try {
            URL url = new URL(urlOpen);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();

            String inputString;
            while ((inputString = bufferedReader.readLine()) != null) {
                builder.append(inputString);
            }

            //Object to hold JSON information
            JSONObject weatherJSON = new JSONObject(builder.toString());
            JSONArray listArray = weatherJSON.getJSONArray("list");

            JSONObject listOne = listArray.getJSONObject(1);
            JSONObject listTwo = listArray.getJSONObject(2);

            JSONArray weatherArrayOne = listOne.getJSONArray("weather");
            JSONObject weatherObjOne = weatherArrayOne.getJSONObject(0);
            test1 = weatherObjOne.getString("main");

            JSONArray weatherArrayTwo = listTwo.getJSONArray("weather");
            JSONObject weatherObjTwo = weatherArrayTwo.getJSONObject(0);
            test2 = weatherObjTwo.getString("main");

            urlConnection.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        assertEquals(condition, test1);
        assertEquals(condition, test1);
    }

}

