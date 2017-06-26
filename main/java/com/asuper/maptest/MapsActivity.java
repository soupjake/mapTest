package com.asuper.maptest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "jakesMessage";
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    //OpenWeatherMap API key
    private static final String APP_ID = "69be65f65a5fabd4d745d0544b7b771e";

    //Weather object used for overlay
    private Weather mWeather = new Weather();

    //Vector to store forecast information
    private Vector<Weather> mWeatherVec = new Vector<>();

    //String variable to set unit type
    private String mUnits = "metric";

    //String unicode for degree sign
    private String mDegrees = "\u00b0";

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    protected Location mLocation;
    private double mLat;
    private double mLon;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private final int mMaxEntries = 5;
    private String[] mLikelyPlaceNames = new String[mMaxEntries];
    private String[] mLikelyPlaceAddresses = new String[mMaxEntries];
    private String[] mLikelyPlaceAttributions = new String[mMaxEntries];
    private LatLng[] mLikelyPlaceLatLngs = new LatLng[mMaxEntries];

    //AppBar variables
    private Toolbar mToolbar;
    private TextView mStationNameText;
    private Button mSearchButton;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private AutocompleteFilter mCountryFilter;
    private Button mLocationButton;

    //Weather button and card variables
    private FloatingActionButton mWeatherButton;
    private FloatingActionButton mTempButton;
    private FloatingActionButton mHumidityButton;
    private TextView mDescriptionText;
    private TextView mWeatherText;
    private int mWeatherInt = 0;


    //Forecast ard variables
    private ConstraintLayout mWeatherConstraint;
    private TextView mDateText;
    private Button mMinusButton;
    private Button mPlusButton;
    private int mWeatherSelection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);


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

        //Set up AppBar
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);

        mStationNameText = (TextView) findViewById(R.id.mStationNameText);

        mSearchButton = (Button) findViewById(R.id.mSearchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .setFilter(mCountryFilter)
                                    .build(MapsActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });

        mLocationButton = (Button) findViewById(R.id.mLocationButton);
        mLocationButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        //Get location based on GPS and update map UI
                        getDeviceLocation();
                        updateLocationUI();

                        //Get weather for GPS location
                        getWeather();
                    }
                }
        );

        //Set up forecast CardView
        mWeatherConstraint = (ConstraintLayout) findViewById(R.id.mWeatherConstraint);
        mWeatherConstraint.setEnabled(false);
        mDateText = (TextView) findViewById(R.id.mDateText);
        mMinusButton = (Button) findViewById(R.id.mMinusButton);
        mMinusButton.setEnabled(false);
        mMinusButton.setTextColor(Color.TRANSPARENT);
        mMinusButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (mWeatherSelection != 0){
                    --mWeatherSelection;
                }
                try{
                    selectForecast(mWeatherSelection);
                } catch (IndexOutOfBoundsException | JSONException e){
                    e.printStackTrace();
                }
            }
        });

        mPlusButton = (Button) findViewById(R.id.mPlusButton);
        mPlusButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (mWeatherSelection != (mWeatherVec.size()-1)){
                    ++mWeatherSelection;
                }
                try{
                    selectForecast(mWeatherSelection);
                } catch (IndexOutOfBoundsException | JSONException e){
                    e.printStackTrace();
                }
            }
        });

        //Set up weather Buttons
        mWeatherButton = (FloatingActionButton) findViewById(R.id.mWeatherButton);
        mWeatherButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        drawOverlay();
                        if(mWeatherConstraint.isEnabled()){
                            mWeatherConstraint.setAnimation(fadeAnimation(1, 0));
                            mWeatherConstraint.setVisibility(View.GONE);
                            mWeatherConstraint.setEnabled(false);
                        } else {
                            mWeatherConstraint.setAnimation(fadeAnimation(0, 1));
                            mWeatherConstraint.setVisibility(View.VISIBLE);
                            mWeatherConstraint.setEnabled(true);
                        }
                    }
                }
        );

        mTempButton = (FloatingActionButton) findViewById(R.id.mTempButton);
        mTempButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        mWeatherInt = 0;
                        //mTempButton.setBackgroundDrawable(Color.RED);
                        mWeatherText.setText((Integer.toString((int)Math.round(mWeather.getTemp())) + mDegrees + "C"));
                    }
                }
        );

        mHumidityButton = (FloatingActionButton) findViewById(R.id.mHumidityButton);
        mHumidityButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        mWeatherInt = 1;
                        mWeatherText.setText(Double.toString(mWeather.getHumidity()) + "%");
                    }
                }
        );

        //Set weather TextViews
        mDescriptionText = (TextView) findViewById(R.id.mDescriptionText);
        mWeatherText = (TextView) findViewById(R.id.mWeatherText);

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLocation);
            super.onSaveInstanceState(outState);
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
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            //showCurrentPlace();
        }
        return true;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        //Click listener to update location based on clicking on map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mLat = latLng.latitude;
                mLon = latLng.longitude;
                mLocation.setLatitude(mLat);
                mLocation.setLongitude(mLon);

                //update map's location to place location
                updateLocationUI();

                //Get weather for place
                getWeather();
            }
        });

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout)findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText("MapTest");

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

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
            mLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            mLat = mLocation.getLatitude();
            mLon = mLocation.getLongitude();
        }
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
                }
            }
        }
        updateLocationUI();
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
            mMap.setMyLocationEnabled(true);
            mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLocation = null;
        }

        // Set the map's camera position to the location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLocation.getLatitude(),
                            mLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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

                //Set nearest weather station's name
                weather.setStationName(weatherJSON.getString("name"));

                //Set date as "present"
                weather.setDate("Present");

                //Get city's weather condition and description
                JSONArray weatherArray = weatherJSON.getJSONArray("weather");
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                weather.setCondition(weatherObj.getString("main"));
                weather.setDescription(stringCapitalise(weatherObj.getString("description")));

                //Get city's temperature and humidity
                JSONObject mainObj = weatherJSON.getJSONObject("main");
                weather.setTemp(mainObj.getDouble("temp"));
                weather.setHumidity(mainObj.getDouble("humidity"));

                mWeatherVec.add(weather);

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            //Set global weather object based on location
            mWeather = weather;

            //Set search filter to country code
            try{
                mCountryFilter = new AutocompleteFilter.Builder()
                        .setCountry(getCountryCode())
                        .build();
            } catch (IOException e){
                e.printStackTrace();
            }

            //Set textViews with their corresponding text values
            mDateText.setText(weather.getDate());
            mStationNameText.setText(weather.getStationName());
            mDescriptionText.setText(weather.getDescription());
            if(mWeatherInt == 0){
                mWeatherText.setText((Integer.toString((int)Math.round(weather.getTemp())) + mDegrees + "C"));
            } else if (mWeatherInt == 1){
                mWeatherText.setText(Double.toString(weather.getHumidity()) + "%");
            }

            getForecast();
        }
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
                    weatherTemp.setDescription(stringCapitalise(weatherObj.getString("description")));

                    //JSON Object of main weather doubles
                    JSONObject main = listObj.getJSONObject("main");
                    weatherTemp.setTemp(main.getDouble("temp"));
                    weatherTemp.setHumidity(main.getDouble("humidity"));

                    //JSON Object of weather date for forecast
                    String date = (String.valueOf(listObj.get("dt_txt")));
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


            Log.i(TAG, "Weather and Forecast loaded successfully!");

            //Reset forecast Selection CardView
            mWeatherSelection = 0;
            mMinusButton.setEnabled(false);
            mMinusButton.setTextColor(Color.TRANSPARENT);
            mPlusButton.setEnabled(true);
            mPlusButton.setTextColor(Color.GRAY);
        }

    }

    //Method to getting weather
    public void getWeather() {
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + mLat + "&lon=" + mLon + "&units=" + mUnits + "&appid=" + APP_ID;
        new WeatherTask().execute(url);
    }

    //Method to getting forecast
    public void getForecast() {
        String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + mLat + "&lon=" + mLon + "&units=" + mUnits + "&appid=" + APP_ID;
        new ForecastTask().execute(url);
    }

    //Method for getting forecast weather
    public void selectForecast(int forecastItem) throws JSONException{

        Weather forecastTemp = mWeatherVec.get(forecastItem);

        //Set textViews with their corresponding text values
        mDateText.setText(formatDate(forecastTemp.getDate()));
        mDescriptionText.setText(forecastTemp.getDescription());
        if(mWeatherInt == 0){
            mWeatherText.setText((Integer.toString((int)Math.round(forecastTemp.getTemp())) + mDegrees + "C"));
        } else if (mWeatherInt == 1){
            mWeatherText.setText(Double.toString(forecastTemp.getHumidity()) + "%");
        }

        //Set forecast buttons to be disabled or enabled based on mWeatherSelection
        if(mWeatherSelection > 0){
            mMinusButton.setEnabled(true);
            mMinusButton.setTextColor(Color.GRAY);
        } else {
            mMinusButton.setEnabled(false);
            mMinusButton.setTextColor(Color.TRANSPARENT);
        }
        if(mWeatherSelection == (mWeatherVec.size()-1)){
            mPlusButton.setEnabled(false);
            mPlusButton.setTextColor(Color.TRANSPARENT);
        } else {
            mPlusButton.setEnabled(true);
            mPlusButton.setTextColor(Color.GRAY);
        }

    }

    //Method for formatting date
    public String formatDate(String date){

        //First check if date is "present" so to skip it
        if(date.equals("Present")){
            return date;
        } else {

            //Get time, month and date substrings
            String time = date.substring(11, 13);
            String month = date.substring(5, 7);
            String day = date.substring(8, 10);

            //Change time to friendly format
            int timeInt = Integer.parseInt(time);

            if (timeInt == 0) {
                timeInt += 12;
                time = (timeInt + "am");
            } else if (timeInt < 12) {
                time = (timeInt + "am");
            } else if (timeInt == 12) {
                time = (timeInt + "pm");
            } else {
                timeInt -= 12;
                time = (timeInt + "pm");
            }

            //Combine for rearranged date UK format
            String formattedDate = new String(time + " " + day + "/" + month);

            return formattedDate;
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
                mLocation.setLatitude(mLat);
                mLocation.setLongitude(mLon);

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

    //Method to draw overlay
    public void drawOverlay() {

        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)));


        int humidPerc = Math.round((int)mWeather.getHumidity()) * 2;
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mLat, mLon))
                .radius(2000) //in metres
                .strokeWidth(0)
                .fillColor(Color.argb(100,0, 153, 255))
        );
        Circle circle2 = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mLat+0.01, mLon+0.01))
                .radius(2000) //in metres
                .strokeWidth(0)
                .fillColor(Color.argb(100,0, 153, 255))
        );
    }

    //Method to get county code
    public String getCountryCode() throws IOException {

        String countyCode = "";

        Geocoder gcd = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(mLat, mLon, 1);
        if (addresses.size() > 0)
        {
            countyCode = addresses.get(0).getCountryCode();
        }
        return countyCode;
    }

    //Method for capitalising each word in string
    public String stringCapitalise(String str) {
        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        str = builder.toString();
        return str;
    }

    //Method used for creating fading in or out animation
    public AlphaAnimation fadeAnimation(int fadeIn, int fadeOut){
        AlphaAnimation animation = new AlphaAnimation(fadeIn, fadeOut);
        animation.setDuration(500);
        return animation;
    }
}
