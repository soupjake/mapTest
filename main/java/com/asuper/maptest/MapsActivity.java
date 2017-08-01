package com.asuper.maptest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Vector;

public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "jakesMessage";
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    //OpenWeatherMap API key
    private final String APP_ID = "69be65f65a5fabd4d745d0544b7b771e";

    //Weather object used for overlay
    private Weather mWeather;

    //Vector to store forecast information
    private Vector<Weather> mWeatherVec;

    //LinkedList to implement FIFO queue for Station locations
    private LinkedList<Station> mStationList;

    //String variable to set unit type
    private String mUnits = "metric";

    //Unit variables
    private String mDegrees = "\u00b0";
    private double mph = 2.23694;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(51.48, -3.21);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    protected Location mLocation;
    private double mLat;
    private double mLon;

    //AppBar variables
    private Toolbar mToolbar;
    private TextView mStationNameText;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    //Navigation menu variables
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mToggle;
    private boolean mFilterSearch;

    //Weather button and card variables
    private FloatingActionButton mTemperatureButton;
    private FloatingActionButton mHumidityButton;
    private FloatingActionButton mCloudButton;
    private FloatingActionButton mPrecipitationButton;
    private FloatingActionButton mWindButton;
    private TextView mDescriptionText;
    private TextView mWeatherText;
    private int mWeatherSelection;
    private String mStationName;
    private String mCountryCode;


    //Forecast ard variables
    private TextView mDateText;
    private Button mLeftButton;
    private Button mRightButton;
    private int mForecastSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        //Set up AppBar
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);

        //Set App bar TextView to Station name
        mStationNameText = (TextView) findViewById(R.id.mStationNameText);

        //Set weather TextViews
        mDescriptionText = (TextView) findViewById(R.id.mDescriptionText);
        mWeatherText = (TextView) findViewById(R.id.mWeatherText);

        //Set up forecast CardView
        mDateText = (TextView) findViewById(R.id.mDateText);
        mLeftButton = (Button) findViewById(R.id.mLeftButton);
        mLeftButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (mForecastSelection != 0){
                    --mForecastSelection;
                }
                selectForecast(mForecastSelection);
            }
        });

        mRightButton = (Button) findViewById(R.id.mRightButton);
        mRightButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (mForecastSelection != mWeatherVec.size()-1){
                    ++mForecastSelection;
                }
                selectForecast(mForecastSelection);
            }
        });

        //Set up weather Buttons
        mTemperatureButton = (FloatingActionButton) findViewById(R.id.mTemperatureButton);
        mTemperatureButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        drawTemperature();
                        //Draw toast to say weather type being displayed
                        Toast toast = Toast.makeText(getApplicationContext(), "Temperature", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, Format.dpToPx(88));
                        toast.show();
                    }
                }
        );

        mHumidityButton = (FloatingActionButton) findViewById(R.id.mHumidityButton);
        mHumidityButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        drawHumidity();
                        //Draw toast to say weather type being displayed
                        Toast toast = Toast.makeText(getApplicationContext(), "Humidity", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, Format.dpToPx(88));
                        toast.show();
                    }
                }
        );

        mCloudButton = (FloatingActionButton) findViewById(R.id.mCloudButton);
        mCloudButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        drawCloud();
                        //Draw toast to say weather type being displayed
                        Toast toast = Toast.makeText(getApplicationContext(), "Clouds", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, Format.dpToPx(88));
                        toast.show();
                    }
                }
        );

        mPrecipitationButton = (FloatingActionButton) findViewById(R.id.mPrecipitationButton);
        mPrecipitationButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        drawPrecipitation();
                        //Draw toast to say weather type being displayed
                        Toast toast = Toast.makeText(getApplicationContext(), "Precipitation", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, Format.dpToPx(88));
                        toast.show();
                    }
                }
        );

        mWindButton = (FloatingActionButton) findViewById(R.id.mWindButton);
        mWindButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        //Draw toast to say weather type being displayed
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Wind: " + mWeather.getWindSpeed() + "mph" + " " + Format.formatWind(mWeather.getWindDeg())
                                , Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, Format.dpToPx(88));
                        toast.show();
                    }
                }
        );

        //Set up App bar navigation menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.mDrawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.mNavigationView);
        mToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mToggle.setDrawerIndicatorEnabled(false);
        mToggle.setHomeAsUpIndicator(R.drawable.ic_navigation);
        mToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        mToggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);

        // Retrieve data from SharedPreferences
        try {
            SharedPreferences sharedPref = MapsActivity.this.getPreferences(Context.MODE_PRIVATE);

            //Get objects using Gson
            Gson gson = new Gson();
            String mLatJSON = sharedPref.getString("mLat", null);
            mLat = gson.fromJson(mLatJSON, double.class);
            String mLonJSON = sharedPref.getString("mLon", null);
            mLon = gson.fromJson(mLonJSON, double.class);
            String mWeatherVecJSON = sharedPref.getString("mWeatherVec", null);
            mWeatherVec = gson.fromJson(mWeatherVecJSON, new TypeToken<Vector<Weather>>() {}.getType());
            String mStationListJSON = sharedPref.getString("mStationList", null);
            mStationList = gson.fromJson(mStationListJSON, new TypeToken<LinkedList<Station>>() {}.getType());

            //Get UI preferences
            mWeatherSelection = sharedPref.getInt("mWeatherSelection", 0);
            mForecastSelection = sharedPref.getInt("mForecastSelection", 0);
            mFilterSearch = sharedPref.getBoolean("mFilterSearch", true);

        } catch (Exception e){
            e.printStackTrace();
            //Initialise objects default
            mWeather = new Weather();
            mWeatherVec = new Vector<>();
            mStationList = new LinkedList<>();
            mFilterSearch = true;
        }

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    //Create options menu for App bar buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar_items, menu);
        return true;
    }

    //Apply click listeners to App bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mSearchButton:
                //Launch Autocomplete Place search function
                try {
                    if(mFilterSearch){
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                        .setFilter(new AutocompleteFilter.Builder()
                                                .setCountry(mCountryCode)
                                                .build())
                                        .build(MapsActivity.this);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } else{
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                        .build(MapsActivity.this);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.mLocationButton:
                //Get location based on GPS and update map UI
                getDeviceLocation();
                updateLocationUI();

                //Get weather for GPS location
                getWeather();
                return true;

            case R.id.mRefreshButton:
                //Refresh weather and mForecastSelection
                getWeather();
                mForecastSelection = 0;
                selectForecast(mForecastSelection);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //Settings back button to close navigation drawer if opened
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.mDrawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Setting click listeners for items within Navigation menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.mDrawerLayout);

        //Change weather based on Nav item place selected
        if(item.getGroupId() == 0){
            mLat = mStationList.get(item.getItemId()).getStationLat();
            mLon = mStationList.get(item.getItemId()).getStationLon();
            updateLocationUI();
            getWeather();
            drawer.closeDrawer(GravityCompat.START);
        } else if(item.getTitle().equals("Filter Search")){
            if(mFilterSearch){
                mFilterSearch = false;
                item.setIcon(R.drawable.ic_radio_button_unchecked);
            } else {
                mFilterSearch = true;
                item.setIcon(R.drawable.ic_radio_button_checked);
            }
        }

        return true;
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    public void onPause(){
        super.onPause();

        if (mMap != null){
            //Save current data in SharedPreferences
            SharedPreferences mapPref = MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mapPref.edit();

            //Use Gson to save objects
            Gson gson = new Gson();
            String mLatJSON = gson.toJson(mLat);
            editor.putString("mLat", mLatJSON);
            String mLonJSON = gson.toJson(mLon);
            editor.putString("mLon", mLonJSON);
            String mWeatherVecJSON = gson.toJson(mWeatherVec);
            editor.putString("mWeatherVec", mWeatherVecJSON);
            String mStationListJSON = gson.toJson(mStationList);
            editor.putString("mStationList", mStationListJSON);

            //Save UI preferences
            editor.putInt("mWeatherSelection", mWeatherSelection);
            editor.putInt("mForecastSelection", mForecastSelection);
            editor.putBoolean("mFilterSearch", mFilterSearch);
            editor.apply();
        }
    }


    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    // Get the current location of the device and set the position of the map.
                    getDeviceLocation();

                    // Turn on the My Location layer and the related control on the map.
                    updateLocationUI();

                    //Use location to run Weather Task to get weather info
                    if (mLocation != null) {

                        //Get weather of location
                        getWeather();

                    } else {
                        Log.i(TAG, "location is null");
                    }
                }
            }
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        if (mLat == 0){
            //Get device's Location
            getDeviceLocation();
            updateLocationUI();
            getWeather();

        } else {
            updateLocationUI();
            selectForecast(mForecastSelection);
            updateNavigationMenu();
        }

        //Click listener to update location based on clicking on map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mLat = latLng.latitude;
                mLon = latLng.longitude;

                //update map's location to place location
                updateLocationUI();

                //Get weather for place
                getWeather();

            }
        });

    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            //First set location to variable for use
            try{
                mLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);
                mLat = mLocation.getLatitude();
                mLon = mLocation.getLongitude();
            } catch (Exception e){
                mLat = mDefaultLocation.latitude;
                mLon = mDefaultLocation.longitude;
            }

        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.clear();
            mMap.setMyLocationEnabled(false);
            mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));
        } else {
            mMap.setMyLocationEnabled(false);
            mLocation = null;
        }

        // Set the map's camera position to the location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLat != 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLat, mLon), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        }
    }

    /**
     * AsyncTask which gets weather information from OpenWeatherMap API and stores it into weatherVec
     */
    private class WeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... strings) {

            //Weather object for first city weather to load as default
            Weather weather = new Weather();

            //Clear weatherVec so new data can be loaded in
            mWeatherVec.clear();

            try {
                URL url = new URL(strings[0]);
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

                //Set nearest weather station's name and shorten if too long
                weather.setStationName(weatherJSON.getString("name"));

                //Set date as "present"
                weather.setDate("Present");

                //Set country's code based on location
                JSONObject sysObj = weatherJSON.getJSONObject("sys");
                weather.setCountryCode(sysObj.getString("country"));

                //Get weather condition and description
                JSONArray weatherArray = weatherJSON.getJSONArray("weather");
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                weather.setCondition(weatherObj.getString("main"));
                weather.setDescription(Format.stringCapitalise(weatherObj.getString("description")));

                //Get temperature and humidity
                JSONObject mainObj = weatherJSON.getJSONObject("main");
                weather.setTemp((int)Math.round(mainObj.getDouble("temp")));
                weather.setHumidity(mainObj.getInt("humidity"));

                //Get cloud percentage
                JSONObject cloudObj = weatherJSON.getJSONObject("clouds");
                weather.setCloudPercentage(cloudObj.getInt("all"));

                //Get wind speed and direction
                JSONObject windObj = weatherJSON.getJSONObject("wind");
                weather.setWindSpeed((int)Math.round(windObj.getDouble("speed") * mph));
                weather.setWindDeg(windObj.getInt("deg"));

                //Try to get rain/snow volume
                try{
                    JSONObject rainObj = weatherJSON.getJSONObject("rain");
                    weather.setRainVolume(rainObj.getDouble("3h"));
                } catch (JSONException e) {
                    //Do nothing as not needed if null
                }
                try{
                    JSONObject snowObj = weatherJSON.getJSONObject("snow");
                    weather.setSnowVolume(snowObj.getDouble("3h"));
                } catch (JSONException e) {
                    //Do nothing as not needed if null
                }

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            //Check if default "Earth" isn't name
            if(!weather.getStationName().equals("Earth")){
                //Set global weather object based on location
                mWeatherVec.add(weather);

                //Create Station object
                Station station = new Station();
                station.setStationName(weather.getStationName());
                station.setStationLat(mLat);
                station.setStationLon(mLon);

                //Check for duplicates
                int duplicate = 0;

                for(int i = 0; i < mStationList.size(); i++) {
                    if (mStationList.get(i).getStationName().equals(weather.getStationName())) {
                        mStationList.remove(i);
                        mStationList.add(station);
                        duplicate = 1;
                    }
                }
                //Create new Station object for mStationList if no duplicates
                if(duplicate != 1){
                    //Set Station details
                    if (mStationList.size() < 4){
                        mStationList.add(station);
                    } else {
                        mStationList.removeFirst();
                        mStationList.add(station);
                    }
                }

                //Update Navigation Menu
                updateNavigationMenu();

                //Set text variables
                mStationName = weather.getStationName();

                mCountryCode = weather.getCountryCode();

                //Draw weather
                drawWeather();

                getForecast();
            }
        }
    }

    //Method to getting weather
    public void getWeather() {

        //Set weather text to say loading
        mStationNameText.setText(R.string.getting_station);
        mDescriptionText.setText(R.string.getting_weather);
        mWeatherText.setText("");

        //Get present weather
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + mLat + "&lon=" + mLon + "&units=" + mUnits + "&appid=" + APP_ID;
        new WeatherTask().execute(url);

    }

    private class ForecastTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... strings) {

            //Weather object for first city weather to load as default
            Weather weather = new Weather();

            try {
                URL url = new URL(strings[0]);
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
                //Add each object in the array to the weatherVector
                for(int i = 0; i < listArray.length() ; i++) {
                    JSONObject listObj = listArray.getJSONObject(i);

                    //Create temp weather object
                    Weather weatherTemp = new Weather();

                    //JSONArray of weather type
                    JSONArray weatherArray = listObj.getJSONArray("weather");
                    JSONObject weatherObj = weatherArray.getJSONObject(0);
                    weatherTemp.setCondition(weatherObj.getString("main"));
                    weatherTemp.setDescription(Format.stringCapitalise(weatherObj.getString("description")));

                    //Set string variables
                    weatherTemp.setStationName(mStationName);
                    weatherTemp.setCountryCode(mCountryCode);


                    //JSON Object of main weather doubles
                    JSONObject main = listObj.getJSONObject("main");
                    weatherTemp.setTemp((int)Math.round(main.getDouble("temp")));
                    weatherTemp.setHumidity(main.getInt("humidity"));

                    //Get cloud percentage
                    JSONObject cloudObj = listObj.getJSONObject("clouds");
                    weatherTemp.setCloudPercentage(cloudObj.getInt("all"));

                    //Get wind speed and direction
                    JSONObject windObj = listObj.getJSONObject("wind");
                    weatherTemp.setWindSpeed((int)Math.round(windObj.getDouble("speed") * mph));
                    weatherTemp.setWindDeg(windObj.getInt("deg"));

                    //Try to get rain/snow volume
                    try{
                        JSONObject rainObj = listObj.getJSONObject("rain");
                        weatherTemp.setRainVolume(rainObj.getDouble("3h"));
                    } catch (JSONException e) {
                        //Do nothing as not needed if null
                    }
                    try{
                        JSONObject snowObj = listObj.getJSONObject("snow");
                        weatherTemp.setSnowVolume(snowObj.getDouble("3h"));
                    } catch (JSONException e) {
                        //Do nothing as not needed if null
                    }

                    //JSON Object of weather date for forecast
                    String date = (Format.formatDate(String.valueOf(listObj.get("dt_txt"))));
                    weatherTemp.setDate(date);

                    //Add to forecastVec
                    mWeatherVec.add(weatherTemp);
                }

                //Set first item in weatherVector as default (not used atm)
                weather = mWeatherVec.get(1);

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            selectForecast(mForecastSelection);

        }

    }

    //Method to getting forecast
    public void getForecast() {

        //Get forecast weather
        String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + mLat + "&lon=" + mLon + "&units=" + mUnits + "&appid=" + APP_ID;
        new ForecastTask().execute(url);

    }



        //Method for getting forecast weather
    public void selectForecast(int forecastItem){

        //Set mWeather to corresponding item in weather vector
        mWeather = mWeatherVec.get(forecastItem);

        //Set date and weather description text
        mStationNameText.setText(mWeather.getStationName());
        mDateText.setText(mWeather.getDate());
        mDescriptionText.setText(mWeather.getDescription());

        //Draw weather
        drawWeather();

        //Set forecast buttons to be disabled or enabled based on mWeatherSelection
        if(mForecastSelection == 0){
            mLeftButton.setEnabled(false);
            mLeftButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.cardview_light_background, null));
        } else {
            mLeftButton.setEnabled(true);
            mLeftButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_left, null));
        }
        if(mForecastSelection == mWeatherVec.size()-1){
            mRightButton.setEnabled(false);
            mRightButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.cardview_light_background, null));
        } else {
            mRightButton.setEnabled(true);
            mRightButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_right, null));
        }

    }



    //Method for allowing Google Places intent on search button
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                //Set place Lat and Lon of place to location and lat/lon variables
                LatLng placeLatLng = place.getLatLng();
                mLat = placeLatLng.latitude;
                mLon = placeLatLng.longitude;

                //update map's location to place location
                updateLocationUI();

                //Get weather for place
                getWeather();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "Search Cancelled");
            }
        }
    }

    //Method to draw temperature info and overlay
    public void drawTemperature() {

        //Change mWeatherText to display humidity
        mWeatherSelection = 0;
        mWeatherText.setText((Integer.toString(mWeather.getTemp()) + mDegrees + "C"));

        //Clear map from other circles and add position marker
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));

        //Get temperature and corresponding colour
        int temperature = mWeather.getTemp();
        int color = TemperatureColors.getTemperatureColor(temperature);

        //Draw temperature circle onto map
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mLat, mLon))
                .radius(2000) //in metres
                .strokeWidth(0)
                .fillColor(color)
        );

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer_enabled);
        mHumidityButton.setImageResource(R.drawable.ic_humidity);
        mCloudButton.setImageResource(R.drawable.ic_cloud);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation);

    }


    //Method to draw humidity info and overlay
    public void drawHumidity() {

        //Change mWeatherText to display humidity
        mWeatherSelection = 1;
        mWeatherText.setText(Integer.toString(mWeather.getHumidity()) + "%");

        //Clear map from other circles and add position marker
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));

        //Draw humidity circle onto map
        int humidity = (int)(mWeather.getHumidity() * 1.5);
        int color = Color.argb(humidity, 124,252,0);
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mLat, mLon))
                .radius(2000) //in metres
                .strokeWidth(0)
                .fillColor(color)
        );

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer);
        mHumidityButton.setImageResource(R.drawable.ic_humidity_enabled);
        mCloudButton.setImageResource(R.drawable.ic_cloud);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation);

    }

    //Method to draw cloud info and overlay
    public void drawCloud() {

        //Change mWeatherText to display humidity
        mWeatherSelection = 2;
        mWeatherText.setText(Integer.toString(mWeather.getCloudPercentage()) + "%");

        //Clear map from other circles and add position marker
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));

        //Draw humidity circle onto map
        int clouds = (int)(mWeather.getCloudPercentage() * 1.5);
        int color = Color.argb(clouds, 128,128,128);
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mLat, mLon))
                .radius(2000) //in metres
                .strokeWidth(0)
                .fillColor(color)
        );

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer);
        mHumidityButton.setImageResource(R.drawable.ic_humidity);
        mCloudButton.setImageResource(R.drawable.ic_cloud_enabled);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation);

    }

    //Method to draw precipitation info and overlay
    public void drawPrecipitation(){

        //Change mWeatherText to display precipitation
        mWeatherSelection = 3;

        //Clear map from other circles and add position marker
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));

        //Set variables used for method
        double precipitation = 0.0;
        String type = "";
        int precipitationAlpha = 0;

        //Set weather info for forecast weather
        if (mWeather.getRainVolume() != 0.0) {
            type = "Rain";
            precipitation = Format.roundVolume(mWeather.getRainVolume());
            mWeatherText.setText(Double.toString(precipitation) + "mm/3h");
        } else if (mWeather.getSnowVolume() != 0.0) {
            type = "Snow";
            precipitation = Format.roundVolume(mWeather.getSnowVolume());
            mWeatherText.setText(Double.toString(precipitation) + "mm/3h");
        } else {
            mWeatherText.setText("0mm/3h");
        }

        //Selecting opacity of circle depending on volume
        if (precipitation == 0.0){
            precipitationAlpha = 0;
        } else if(precipitation < 1.0) {
            precipitationAlpha = 50;
        } else if(precipitation < 4.0) {
            precipitationAlpha = 75;
        } else if(precipitation < 8.0) {
            precipitationAlpha = 100;
        } else if(precipitation < 16.0) {
            precipitationAlpha = 125;
        } else if(precipitation > 16.0) {
            precipitationAlpha = 150;
        }

        //Make colour blue by default as rain more common
        int color = Color.argb(precipitationAlpha, 0,191,255);

        //Make colour white if type is snow
        if (type.equals("Snow")){
            color = Color.argb(precipitationAlpha, 255,255,255);
        }

        //Set overlay color if currently raining/snowing
        if (mWeather.getDate().equals("Present") && mWeather.getCondition().equals("Rain")){
            color = Color.argb(75, 0, 191, 255);
            mWeatherText.setText("");
        } else if (mWeather.getDate().equals("Present") && mWeather.getCondition().equals("Snow")){
            color = Color.argb(75, 255, 255, 255);
            mWeatherText.setText("");
        }

        //Draw precipitation circle onto map
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mLat, mLon))
                .radius(2000) //in metres
                .strokeWidth(0)
                .fillColor(color)
        );

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer);
        mHumidityButton.setImageResource(R.drawable.ic_humidity);
        mCloudButton.setImageResource(R.drawable.ic_cloud);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation_enabled);

    }

    //Method for drawing same weather as selected before
    public void drawWeather(){
        if(mWeatherSelection == 0 ){
            drawTemperature();
        } else if (mWeatherSelection == 1) {
            drawHumidity();
        } else if (mWeatherSelection == 2) {
            drawCloud();
        } else if (mWeatherSelection == 3) {
            drawPrecipitation();
        }

        //Set wind button
        mWindButton.setRotation(mWeather.getWindDeg());
    }

    //Method for updating Navigation menu
    public void updateNavigationMenu(){
        Menu menu = mNavigationView.getMenu();
        menu.clear();
        if(mStationList.size() != 0){
            int order = 0;
            SubMenu placeSubMenu = menu.addSubMenu("Places");
            for(int listItem = mStationList.size()-1; listItem >= 0; listItem--){
                placeSubMenu.add(0, listItem, order, mStationList.get(listItem).getStationName());
                placeSubMenu.getItem(order).setIcon(R.drawable.ic_place);
                order++;
            }
        }

        SubMenu settingsSubMenu = menu.addSubMenu("Settings");
        settingsSubMenu.add(1, 0, 0, "Filter Search");
        if(mFilterSearch){
            settingsSubMenu.getItem(0).setIcon(R.drawable.ic_radio_button_checked);
        } else{
            settingsSubMenu.getItem(0).setIcon(R.drawable.ic_radio_button_unchecked);
        }
    }

}