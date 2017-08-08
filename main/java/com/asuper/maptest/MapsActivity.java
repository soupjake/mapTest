package com.asuper.maptest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "jakesMessage";
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    //OpenWeatherMap API key
    public final String APP_ID = "69be65f65a5fabd4d745d0544b7b771e";

    //Weather object used for overlay
    private Weather mWeather;

    //LinkedList to implement FIFO queue for Station locations
    private LinkedList<Station> mStationList;



    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(51.48, -3.21);
    private static final int DEFAULT_ZOOM = 10;
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
    private FloatingActionButton mCloudButton;
    private FloatingActionButton mPrecipitationButton;
    private FloatingActionButton mWindButton;
    private TextView mDescriptionText;
    private TextView mWeatherText;
    private int mWeatherSelection;
    private String mStationName;
    private String mCountryCode;

    //Variables to set units
    private String mDegrees = "\u00b0";
    private boolean mTemperatureUnits;
    private boolean mWindUnits;

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

        //Set up weather Buttons
        mTemperatureButton = (FloatingActionButton) findViewById(R.id.mTemperatureButton);
        mTemperatureButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        drawTemperature();
                        //Draw toast to say weather type being displayed
                        Toast toast = Toast.makeText(getApplicationContext(), "Temperature", Toast.LENGTH_SHORT);
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
                        toast.show();
                    }
                }
        );

        mWindButton = (FloatingActionButton) findViewById(R.id.mWindButton);
        mWindButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        drawWind();
                        //Draw toast to say weather type being displayed
                        Toast toast = Toast.makeText(getApplicationContext(), "Wind", Toast.LENGTH_SHORT);
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
            String mCameraPositionJSON = sharedPref.getString("mCameraPosition", null);
            mCameraPosition = gson.fromJson(mCameraPositionJSON, CameraPosition.class);
            String mStationListJSON = sharedPref.getString("mStationList", null);
            mStationList = gson.fromJson(mStationListJSON, new TypeToken<LinkedList<Station>>() {}.getType());

            //Get UI preferences
            mWeatherSelection = sharedPref.getInt("mWeatherSelection", 0);
            mFilterSearch = sharedPref.getBoolean("mFilterSearch", true);
            mTemperatureUnits = sharedPref.getBoolean("mTemperatureUnits", true);
            mWindUnits = sharedPref.getBoolean("mWindUnits", true);

        } catch (Exception e){
            e.printStackTrace();
            //Initialise objects default
            mWeather = new Weather();
            mStationList = new LinkedList<>();
            mFilterSearch = true;
            mTemperatureUnits = true;
            mWindUnits = true;
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
                } catch (GooglePlayServicesRepairableException e ) {
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
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(new LatLng(mLat, mLon), DEFAULT_ZOOM, 0, 0)));
                getWeather();
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

        } else if(item.getTitle().equals("Temperature Units")){
            if(mTemperatureUnits){
                mTemperatureUnits = false;
                item.setIcon(R.drawable.ic_radio_button_unchecked);
                if(mWeatherSelection == 0){
                    mWeatherText.setText(Format.celsiusToFahrenheit(mWeather.getTemp()) + mDegrees + "F");
                }
            } else {
                mTemperatureUnits = true;
                item.setIcon(R.drawable.ic_radio_button_checked);
                if(mWeatherSelection == 0){
                    mWeatherText.setText(mWeather.getTemp() + mDegrees + "C");
                }
            }
        } else if (item.getTitle().equals("Wind Units")){
            if(mWindUnits){
                mWindUnits = false;
                item.setIcon(R.drawable.ic_radio_button_unchecked);
                if(mWeatherSelection == 3){
                    mWeatherText.setText(mWeather.getWindSpeed() + "m/s"
                            + " " + Format.formatWind(mWeather.getWindDeg()));
                }
            } else {
                mWindUnits = true;
                item.setIcon(R.drawable.ic_radio_button_checked);
                if(mWeatherSelection == 3){
                    mWeatherText.setText(Format.windMph(mWeather.getWindSpeed()) + "mph"
                            + " " + Format.formatWind(mWeather.getWindDeg()));
                }
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
            String mCameraPositionJSON = gson.toJson(mMap.getCameraPosition());
            editor.putString("mCameraPosition", mCameraPositionJSON);
            String mStationListJSON = gson.toJson(mStationList);
            editor.putString("mStationList", mStationListJSON);


            //Save UI preferences
            editor.putInt("mWeatherSelection", mWeatherSelection);
            editor.putBoolean("mFilterSearch", mFilterSearch);
            editor.putBoolean("mTemperatureUnits", mTemperatureUnits);
            editor.putBoolean("mWindUnits", mWindUnits);
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
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            getWeather();
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
            mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));
        } else {
            mLocation = null;
        }

        // Set the map's camera position to the location of the device.
        if (mLat != 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(mLat, mLon)));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    mDefaultLocation, DEFAULT_ZOOM));
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
                weather.setWindSpeed((int)Math.round(windObj.getDouble("speed")));
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
                mWeather = weather;

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
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + mLat + "&lon="
                + mLon + "&units=metric&appid=" + APP_ID;
        new WeatherTask().execute(url);

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

    //Method for drawing weather tile layer

    public void drawTileLayer(String tileType){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(createTileProvider(tileType)));
    }

    //Method to draw temperature info and overlay
    public void drawTemperature() {

        //Change mWeatherText to display humidity
        mWeatherSelection = 0;

        if(mTemperatureUnits){
            mWeatherText.setText(mWeather.getTemp() + mDegrees + "C");
        } else {
            mWeatherText.setText(Format.celsiusToFahrenheit(mWeather.getTemp()) + mDegrees + "F");
        }
        //Draw the weather layer
        drawTileLayer("temp_new");

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer_enabled);
        mCloudButton.setImageResource(R.drawable.ic_cloud);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation);
        mWindButton.setImageResource(R.drawable.ic_wind);

    }

        //Method to draw cloud info and overlay
    public void drawCloud() {

        //Change mWeatherText to display humidity
        mWeatherSelection = 1;
        mWeatherText.setText(mWeather.getCloudPercentage() + "%");

        drawTileLayer("clouds_new");

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer);
        mCloudButton.setImageResource(R.drawable.ic_cloud_enabled);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation);
        mWindButton.setImageResource(R.drawable.ic_wind);

    }

    //Method to draw precipitation info and overlay
    public void drawPrecipitation(){

        //Change mWeatherText to display precipitation
        mWeatherSelection = 2;
        //Set weather info for forecast weather
        double precipitation = 0.0;

        if (mWeather.getRainVolume() != 0.0) {
            precipitation = Format.roundVolume(mWeather.getRainVolume());
            mWeatherText.setText(precipitation + "mm/3h");
        } else if (mWeather.getSnowVolume() != 0.0) {
            precipitation = Format.roundVolume(mWeather.getSnowVolume());
            mWeatherText.setText(precipitation + "mm/3h");
        } else {
            mWeatherText.setText("");
        }

        drawTileLayer("precipitation_new");

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer);
        mCloudButton.setImageResource(R.drawable.ic_cloud);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation_enabled);
        mWindButton.setImageResource(R.drawable.ic_wind);

    }

    //Method to draw wind info and overlay
    public void drawWind() {

        //Change mWeatherText to display humidity
        mWeatherSelection = 3;

        if(mWindUnits){
            mWeatherText.setText(Format.windMph(mWeather.getWindSpeed()) + "mph"
                    + " " + Format.formatWind(mWeather.getWindDeg()));
        } else {
            mWeatherText.setText(mWeather.getWindSpeed() + "m/s"
                    + " " + Format.formatWind(mWeather.getWindDeg()));
        }

        drawTileLayer("wind_new");

        //Set weather FAB images
        mTemperatureButton.setImageResource(R.drawable.ic_thermometer);
        mCloudButton.setImageResource(R.drawable.ic_cloud);
        mPrecipitationButton.setImageResource(R.drawable.ic_precipitation);
        mWindButton.setImageResource(R.drawable.ic_wind_enabled);

    }

    //Method for drawing same weather as selected before
    public void drawWeather(){

        mStationNameText.setText(mWeather.getStationName());
        mDescriptionText.setText(mWeather.getDescription());

        if(mWeatherSelection == 0 ){
            drawTemperature();
        } else if (mWeatherSelection == 1) {
            drawCloud();
        } else if (mWeatherSelection == 2) {
            drawPrecipitation();
        } else if (mWeatherSelection == 3) {
            drawWind();
        }

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
        settingsSubMenu.add(1, 1, 0, "Temperature Units");
        if(mTemperatureUnits){
            settingsSubMenu.getItem(1).setIcon(R.drawable.ic_radio_button_checked);
        } else{
            settingsSubMenu.getItem(1).setIcon(R.drawable.ic_radio_button_unchecked);
        }
        settingsSubMenu.add(1, 2, 0, "Wind Units");
        if(mWindUnits){
            settingsSubMenu.getItem(2).setIcon(R.drawable.ic_radio_button_checked);
        } else{
            settingsSubMenu.getItem(2).setIcon(R.drawable.ic_radio_button_unchecked);
        }
    }

    public TileProvider createTileProvider(final String tileType) {
        return new UrlTileProvider(256, 256) {

            String OWM_TILE_URL = "http://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=" + APP_ID ;

            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String fUrl = String.format(OWM_TILE_URL, tileType, zoom, x, y);
                URL url = null;
                try {
                    url = new URL(fUrl);
                } catch (MalformedURLException mfe) {
                    mfe.printStackTrace();
                }

                return url;
            }
        };
    }

}