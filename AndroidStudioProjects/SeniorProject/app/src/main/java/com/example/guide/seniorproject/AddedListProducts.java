package com.example.guide.seniorproject;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.PendingIntent.getActivity;

/**
 * Created by guide on 4/7/2017.
 */

public class AddedListProducts extends SearchActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    //Define request code to send to Google play service
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;


    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public static RecyclerView mRVProduct;
    public static AdapterProduct mAdapter;
    private static Context context = null;
    public static List<NearbyPlace> Place_data;
    public Double TescoPriceSum = 0.00;
    public Double BigCPriceSum = 0.00;
    ArrayList<DataProduct> selectedTescoItems = new ArrayList<>();
    ArrayList<DataProduct> selectedBigCItems = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addedlist);

        Bundle bundle = getIntent().getExtras();
        selectedTescoItems = (ArrayList<DataProduct>) bundle
                .getSerializable("selectedTescoItems");

        selectedBigCItems = (ArrayList<DataProduct>) bundle
                .getSerializable("selectedBigCItems");

        //mRVProduct = (RecyclerView) findViewById(R.id.AddedList);
        //mAdapter = new AdapterProduct(this, selectedTescoItems);
        //mRVProduct.setAdapter(mAdapter);
        //mRVProduct.setLayoutManager(new LinearLayoutManager(this));

        //System.out.print(selectedTescoItems);

        for (DataProduct Tesco : selectedTescoItems) {
            TescoPriceSum += Tesco.price;
        }
        for (DataProduct BigC : selectedBigCItems) {
            BigCPriceSum += BigC.price;
        }

        TextView TescoPrice = (TextView) findViewById(R.id.textView2);
        TextView BigCPrice = (TextView) findViewById(R.id.textView3);

        TescoPrice.setText(String.valueOf(TescoPriceSum));
        BigCPrice.setText(String.valueOf(BigCPriceSum));

        //Current Location

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);

        //Initial Arguments for NearbyPlace
        String latitude = String.valueOf(currentLatitude);
        String longitude = String.valueOf(currentLongitude);
        String radius = "10000";
        String types = "department_store";
        String keyword = "BigC";
        String key = "AIzaSyBxDmpkx-nb_K5j65Pe612yddpa4nbNJqI";


        NearbyTask myNearbytask = new NearbyTask();
        myNearbytask.execute(latitude,longitude,radius,types,keyword,key);

        TextView cLocation = (TextView) findViewById(R.id.textView4);
        cLocation.setText(String.valueOf(currentLatitude));

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");
        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
    //if connected get lat long
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }


    public class NearbyTask extends AsyncTask<String, String, String> {

        private ProgressDialog pdLoading ;
        HttpURLConnection conn;
        URL url = null;


        @Override
        public void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        public String doInBackground(String... args) {
            try {
                Looper.prepare();
                String latitude = args[0];
                String longitude = args[1];
                String radius = args[2];
                String types = args[3];
                String keyword = args[4];
                String key = "YOUR_API_KEY_FOR_BROWSER";

                String uri = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                        + "location=" + latitude + "," + longitude
                        + "&radius=" + radius
                        + "&types" + types
                        + "&keyword=" + keyword
                        + "&key="+ key; // you can add more options here
                // Enter URL address where your php file resides
                url = new URL(uri);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }

            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.connect();

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    input.close();
                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return("Connection error");
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        public void onPostExecute(String result) {

            //List<Place> data=new ArrayList<>();
            Place_data = new ArrayList<>();
                try {

                    JSONArray jArray = new JSONArray(result);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        NearbyPlace nearbyPlace = new NearbyPlace();
                        nearbyPlace.place_name = json_data.getString("name");
                        nearbyPlace.vicinity = json_data.getString("vicinity");
                        nearbyPlace.lat = json_data.getJSONObject("geometry").getJSONObject("location").getString("lat");
                        nearbyPlace.lng = json_data.getJSONObject("geometry").getJSONObject("location").getString("lng");
                        nearbyPlace.reference = json_data.getString("reference");

                        Place_data.add(nearbyPlace);
                    }

                    TextView lLocation = (TextView) findViewById(R.id.textView5);
                    lLocation.setText(Place_data.get(1).place_name);

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Log.d("Exception", e.toString());
                }

        }

    }
}
