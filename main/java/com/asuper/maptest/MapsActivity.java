package com.asuper.maptest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
    private Weather locationWeather = new Weather();

    //Vector to store forecast information
    private Vector<Weather> forecastVec = new Vector<Weather>();

    //String variable to set unit type
    private String units = "metric";

    //String unicode for degree sign
    private String degree = "\u00b0";

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
    private double lat;
    private double lon;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private final int mMaxEntries = 5;
    private String[] mLikelyPlaceNames = new String[mMaxEntries];
    private String[] mLikelyPlaceAddresses = new String[mMaxEntries];
    private String[] mLikelyPlaceAttributions = new String[mMaxEntries];
    private LatLng[] mLikelyPlaceLatLngs = new LatLng[mMaxEntries];

    //UI variables
    private PlaceAutocompleteFragment mAutocompleteFragment;
    private SeekBar mSeekForecast;
    private FloatingActionButton mLocationButton;
    private FloatingActionButton mOverlayButton;
    private TextView mConditionText;
    private TextView mTempText;
    private TextView mHumidityText;
    private Toast mDateToast;

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

        //Assign IDs to UI elements
        mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.mAutocompleteFragment);

        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //Set place Lat and Lon of place to location and lat/lon variables
                LatLng placeLatLng = place.getLatLng();
                lat = placeLatLng.latitude;
                lon = placeLatLng.longitude;
                mLocation.setLatitude(lat);
                mLocation.setLongitude(lon);

                //update map's location to place location
                updateLocationUI();

                //get weather for place
                String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=" + units + "&appid=" + APP_ID;
                new GetWeatherTask().execute(url);

            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mSeekForecast = (SeekBar) findViewById(R.id.mSeekForecast);

        //Allows seekbar to get forecasted weather
        mSeekForecast.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){
                    int progressValue;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress;


                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        try{
                            selectForecast(progressValue);
                        } catch (ArrayIndexOutOfBoundsException | JSONException e){
                            e.printStackTrace();
                        }

                        //Create toast to display date of forecast
                        Context context = getApplicationContext();

                        CharSequence text = formatDate(progressValue);
                        int duration = Toast.LENGTH_SHORT;

                        if(mDateToast != null) {
                            mDateToast.cancel();
                        }
                        mDateToast = Toast.makeText(context, text, duration);
                        mDateToast.setGravity(Gravity.BOTTOM, 0, 120);
                        mDateToast.show();
                    }

                });

        mLocationButton = (FloatingActionButton) findViewById(R.id.mLocationButton);

        //Listener to reset map location back to device's GPS location
        mLocationButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        //Get location based on GPS and update map UI
                        getDeviceLocation();
                        updateLocationUI();

                        //Get weather for GPS location
                        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=" + units + "&appid=" + APP_ID;
                        new GetWeatherTask().execute(url);
                    }
                }
        );

        mOverlayButton = (FloatingActionButton) findViewById(R.id.mOverlayButton);

        //Listener to apply overlay method
        mOverlayButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        drawOverlay();
                    }
                }
        );

        //Set weather TextViews
        mConditionText = (TextView) findViewById(R.id.mConditionText);
        mTempText = (TextView) findViewById(R.id.mTempText);
        mHumidityText = (TextView) findViewById(R.id.mHumidityText);
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
                lat = latLng.latitude;
                lon = latLng.longitude;
                mLocation.setLatitude(lat);
                mLocation.setLongitude(lon);

                //update map's location to place location
                updateLocationUI();

                //get weather for place
                String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=" + units + "&appid=" + APP_ID;
                new GetWeatherTask().execute(url);
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
            String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=" + units + "&appid=" + APP_ID;
            new GetWeatherTask().execute(url);
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
            lat = mLocation.getLatitude();
            lon = mLocation.getLongitude();
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
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
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
    private class GetWeatherTask extends AsyncTask<String, Void, Weather> {

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

                //Set nearest weather station's name
                weather.setStationName(weatherJSON.getString("name"));

                //Set country's code based on location
                JSONObject sysObj = weatherJSON.getJSONObject("sys");
                weather.setCountryCode(sysObj.getString("country"));

                //Get city's weather condition and description
                JSONArray weatherArray = weatherJSON.getJSONArray("weather");
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                weather.setCondition(weatherObj.getString("main"));
                weather.setDescription(weatherObj.getString("description"));

                //Get city's temperature and humidity
                JSONObject mainObj = weatherJSON.getJSONObject("main");
                weather.setTemp(mainObj.getDouble("temp"));
                weather.setHumidity(mainObj.getDouble("humidity"));

                weather = weather;

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
            locationWeather = weather;

            AutocompleteFilter mCountryFilter = new AutocompleteFilter.Builder()
                    .setCountry(weather.getCountryCode())
                    .build();
            mAutocompleteFragment.setFilter(mCountryFilter);

            //Set textViews with their corresponding text values
            mConditionText.setText(weather.getCondition());
            mTempText.setText((Integer.toString((int)Math.round(weather.getTemp())) + degree + "C"));
            mHumidityText.setText(Double.toString(weather.getHumidity()) + "%");

            String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=" + units + "&appid=" + APP_ID;
            new GetForecastTask().execute(url);
        }
    }
    private class GetForecastTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... strings) {

            //Clear weatherVec so new data can be loaded in
            forecastVec.clear();

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
                    weatherTemp.setDescription(weatherObj.getString("description"));

                    //JSON Object of main weather doubles
                    JSONObject main = listObj.getJSONObject("main");
                    weatherTemp.setTemp(main.getDouble("temp"));
                    weatherTemp.setHumidity(main.getDouble("humidity"));

                    //JSON Object of weather date for forecast
                    String date = (String.valueOf(listObj.get("dt_txt")));
                    weatherTemp.setDate(date);

                    //Add to forecastVec
                    forecastVec.add(weatherTemp);
                }

                //Set first item in weatherVector as default
                weather = forecastVec.get(0);

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
            //Reset forecast Seekbar to 0 and size to forecastVec if changed
            if (mSeekForecast.getProgress() != 0){
                mSeekForecast.setProgress(0);
            }

            //Set forecast seekbar max to size of forecast vector
            mSeekForecast.setMax(forecastVec.size());
        }

    }

    //Method for getting forecast weather
    public void selectForecast(int forecastItem) throws JSONException{

        Weather forecastTemp = forecastVec.get(forecastItem);

        //Set textViews with their corresponding text values
        mConditionText.setText(forecastTemp.getCondition());
        mTempText.setText((Integer.toString((int)Math.round(forecastTemp.getTemp())) + degree + "C"));
        mHumidityText.setText(Double.toString(forecastTemp.getHumidity()) + "%");

    }

    //Method for formatting date
    public String formatDate(int forecastItem){

        //Get forecast date string from object
        String forecastDate =  forecastVec.get(forecastItem).getDate();

        //Get time, month and date substrings
        String time = forecastDate.substring(11, 13);
        String month = forecastDate.substring(5, 7);
        String day = forecastDate.substring(8, 10);

        //Change time to friendly format
        int timeInt = Integer.parseInt(time);

        if(timeInt < 12) {
            time = (timeInt + "am");
        } else if (timeInt == 12) {
            time = (timeInt + "pm");
        } else if (timeInt < 24){
            timeInt -= 12;
            time = (timeInt + "pm");
        } else {
            timeInt -= 24;
            time = (timeInt + "am");
        }

        //Combine for rearranged date UK format
        String formattedDate = new String(time + " " + day + "/" + month);

        return formattedDate;
    }

    //Method to draw overlay
    public void drawOverlay() {

        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));

        int humidPerc = Math.round((int)locationWeather.getHumidity()) * 2;
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(lat, lon))
                .radius(2000) //in metres
                .strokeWidth(0)
                .fillColor(Color.argb(humidPerc, 0, 153, 255))
        );
    }
}
