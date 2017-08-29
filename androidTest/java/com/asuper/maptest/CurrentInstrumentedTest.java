package com.asuper.maptest;

import android.content.Context;

import android.os.AsyncTask;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

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

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CurrentInstrumentedTest{

    String name = "Cathays";
    String nameTest = "";
    String condition = "Clouds";
    String conditionTest = "";
    double mLat = 51.48686;
    double mLon = -3.2137181;
    public final String APP_ID = "69be65f65a5fabd4d745d0544b7b771e";

    @Test
    public void useAppContext() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        String urlOpen = "http://api.openweathermap.org/data/2.5/weather?lat=" + mLat + "&lon="
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
            nameTest = weatherJSON.getString("name");

            JSONArray weatherArray = weatherJSON.getJSONArray("weather");
            JSONObject weatherObj = weatherArray.getJSONObject(0);
            conditionTest = weatherObj.getString("main");

            urlConnection.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        assertEquals(name, nameTest);
        assertEquals(condition, conditionTest);
    }

}

