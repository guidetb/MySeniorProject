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

public class AddedListProducts extends SearchActivity  {
    //Define request code to send to Google play service
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public double currentLatitude;
    public double currentLongitude;


    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public static ArrayList<NearbyPlace> BPlace_data;
    public static ArrayList<NearbyPlace> TPlace_data;
    public Double TescoPriceSum = 0.00;
    public Double BigCPriceSum = 0.00;
    ArrayList<DataProduct> selectedTescoItems = new ArrayList<>();
    ArrayList<DataProduct> selectedBigCItems = new ArrayList<>();
    public static ArrayList<Integer> BigCDistItems ;
    public static String BigCLat;
    public static String BigCLng;
    public static String TesCoLat;
    public static String TesCoLng;
    public static String BigCDist;
    public static String TescoDist;
    public static Double TCDIST;
    public static Double BCDIST;
    public static Double TCEXP;
    public static Double BCEXP;


    TextView BigCDi;
    TextView TesCoDi;

    GPSTracker gps;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addedlist);

        //Current Location
        gps = new GPSTracker(AddedListProducts.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            currentLatitude = gps.getLatitude();
            currentLongitude = gps.getLongitude();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        //Initial Arguments for BigC_NearbyTask
        String latitude = String.valueOf(currentLatitude);
        String longitude = String.valueOf(currentLongitude);
        String types = "department_store";
        String keyword1 = "BigC";
        String key = "AIzaSyBtzQ5SolvWlvApA63QW07iCqe0dz2VBqI";

        BigC_NearbyTask myNearbytask = new BigC_NearbyTask();
        myNearbytask.execute(latitude,longitude,types,keyword1,key);

        Bundle bundle = getIntent().getExtras();
        selectedTescoItems = (ArrayList<DataProduct>) bundle
                .getSerializable("selectedTescoItems");

        selectedBigCItems = (ArrayList<DataProduct>) bundle
                .getSerializable("selectedBigCItems");



        for (DataProduct Tesco : selectedTescoItems) {
            TescoPriceSum += Tesco.price;
        }
        for (DataProduct BigC : selectedBigCItems) {
            BigCPriceSum += BigC.price;
        }

        TextView TescoPrice = (TextView) findViewById(R.id.textView6);
        TextView BigCPrice = (TextView) findViewById(R.id.textView3);

        TescoPrice.setText(String.valueOf(TescoPriceSum + "Baht"));
        BigCPrice.setText(String.valueOf(BigCPriceSum + "Baht"));



        //Initial Arguments for Tesco_NearbyTask

        String keyword2 = "Tesco";

        /*Tesco_NearbyTask tescoNearbyTask = new Tesco_NearbyTask();
        tescoNearbyTask.execute(latitude,longitude,types,keyword2,key);*/

        /*DistanceTask BigCDistanceTask =  new DistanceTask();
       BigCDistanceTask.execute(latitude,longitude,"13.791298","100.54956","false",key);*/

        BigCDi = (TextView) findViewById(R.id.textView5);
        TesCoDi = (TextView) findViewById(R.id.textView8);

    }

    public class BigC_NearbyTask extends AsyncTask<String, String, String> {

        private ProgressDialog pdLoading ;
        HttpURLConnection conn1;
        URL url1 = null;
        @Override
        public void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            pdLoading = new ProgressDialog(AddedListProducts.this);
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        public String doInBackground(String... args) {
            try {
                Looper.prepare();
                String latitude = args[0];
                String longitude = args[1];
                String types = args[2];
                String keyword = args[3];
                String key = args[4];

                String uri = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                        + "rankby=distance"
                        + "&location=" + latitude + "," + longitude
                        + "&types" + types
                        + "&keyword=" + keyword
                        + "&key="+ key; // you can add more options here

                // Enter URL address where your php file resides
                url1 = new URL(uri);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }

            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn1 = (HttpURLConnection) url1.openConnection();
                conn1.setReadTimeout(READ_TIMEOUT);
                conn1.setConnectTimeout(CONNECTION_TIMEOUT);
                conn1.setRequestMethod("GET");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn1.setDoInput(true);
                conn1.setDoOutput(true);
                conn1.connect();


                int response_code1 = conn1.getResponseCode();
                // Check if successful connection made
                if (response_code1 == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input1 = conn1.getInputStream();
                    BufferedReader reader1 = new BufferedReader(new InputStreamReader(input1));
                    StringBuilder result1 = new StringBuilder();
                    String line1;

                    while ((line1 = reader1.readLine()) != null) {
                        result1.append(line1);
                    }
                    input1.close();
                    // Pass data to onPostExecute method
                    return (result1.toString());

                } else {
                    return("Connection error");
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            } finally {
                conn1.disconnect();

            }


        }


        @Override
        public void onPostExecute(String result1) {

            //List<Place> data=new ArrayList<>();
            //this method will be running on UI thread
            pdLoading.dismiss();
            BPlace_data = new ArrayList<>();
            //this method will be running on UI thread
            pdLoading.dismiss();
            try {
                JSONObject jsonRoot = new JSONObject(result1);
                JSONArray jArray = jsonRoot.getJSONArray("results");

                // Extract data from json and store into ArrayList as class objects
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    NearbyPlace nearbyPlace = new NearbyPlace();
                    nearbyPlace.place_name = json_data.getString("name");
                    nearbyPlace.vicinity = json_data.getString("vicinity");
                    nearbyPlace.lat = json_data.getJSONObject("geometry").getJSONObject("location").getString("lat");
                    nearbyPlace.lng = json_data.getJSONObject("geometry").getJSONObject("location").getString("lng");
                    nearbyPlace.reference = json_data.getString("reference");

                    BPlace_data.add(nearbyPlace);
                }

                //return BigCPlace_data;
                BigCLat = BPlace_data.get(0).lat;
                BigCLng = BPlace_data.get(0).lng;



            } catch (JSONException e) {
                // You to understand what actually error is and handle it appropriately
                Log.d("Exception", e.toString());
            }
            String Blatitude = String.valueOf(currentLatitude);
            String Blongitude = String.valueOf(currentLongitude);
            String Bdeslatitude = BigCLat;
            String Bdeslongitude = BigCLng;
            String Bsensor = "false";
            String Bkey = "AIzaSyBtzQ5SolvWlvApA63QW07iCqe0dz2VBqI";
            new BigC_DistanceTask().execute(Blatitude,Blongitude,Bdeslatitude,Bdeslongitude,Bsensor,Bkey);

            //BigCla.setText(BigCLat);
        }

    }

    public class Tesco_NearbyTask extends AsyncTask<String, String, String> {

        private ProgressDialog pdLoading ;
        HttpURLConnection conn2;
        URL url2 = null;
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public String doInBackground(String... args) {
            try {
                String latitude = args[0];
                String longitude = args[1];
                String types = args[2];
                String keyword = args[3];
                String key = args[4];

                String urt = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                        + "rankby=distance"
                        + "&location=" + latitude + "," + longitude
                        + "&types" + types
                        + "&keyword=" + keyword
                        + "&key="+ key; // you can add more options here

                // Enter URL address where your php file resides
                url2 = new URL(urt);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }

            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn2 = (HttpURLConnection) url2.openConnection();
                conn2.setReadTimeout(READ_TIMEOUT);
                conn2.setConnectTimeout(CONNECTION_TIMEOUT);
                conn2.setRequestMethod("GET");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn2.setDoInput(true);
                conn2.setDoOutput(true);
                conn2.connect();


                int response_code2 = conn2.getResponseCode();
                // Check if successful connection made
                if (response_code2 == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input2 = conn2.getInputStream();
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(input2));
                    StringBuilder result2 = new StringBuilder();
                    String line2;

                    while ((line2 = reader2.readLine()) != null) {
                        result2.append(line2);
                    }
                    input2.close();
                    // Pass data to onPostExecute method
                    return (result2.toString());

                } else {
                    return("Connection error");
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            } finally {
                conn2.disconnect();

            }


        }


        @Override
        public void onPostExecute(String result2) {

            //List<Place> data=new ArrayList<>();
            TPlace_data = new ArrayList<>();

            try {
                JSONObject jsonRoot = new JSONObject(result2);
                JSONArray jArray = jsonRoot.getJSONArray("results");

                // Extract data from json and store into ArrayList as class objects
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    NearbyPlace nearbyT_Place = new NearbyPlace();
                    nearbyT_Place.place_name = json_data.getString("name");
                    nearbyT_Place.vicinity = json_data.getString("vicinity");
                    nearbyT_Place.lat = json_data.getJSONObject("geometry").getJSONObject("location").getString("lat");
                    nearbyT_Place.lng = json_data.getJSONObject("geometry").getJSONObject("location").getString("lng");
                    nearbyT_Place.reference = json_data.getString("reference");

                    TPlace_data.add(nearbyT_Place);
                }

                //return BigCPlace_data;
                TesCoLat = TPlace_data.get(0).lat;
                TesCoLng = TPlace_data.get(0).lng;

            } catch (JSONException e) {
                // You to understand what actually error is and handle it appropriately
                Log.d("Exception", e.toString());
            }
            String Tlatitude = String.valueOf(currentLatitude);
            String Tlongitude = String.valueOf(currentLongitude);
            String Tdeslatitude = TesCoLat;
            String Tdeslongitude = TesCoLng;
            String Tsensor = "false";
            String Tkey = "AIzaSyBtzQ5SolvWlvApA63QW07iCqe0dz2VBqI";
            new Tesco_DistanceTask().execute(Tlatitude,Tlongitude,Tdeslatitude,Tdeslongitude,Tsensor,Tkey);

        }

    }

    public class BigC_DistanceTask extends AsyncTask<String, String, String> {

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
                String latitude = args[0];
                String longitude = args[1];
                String deslatitude = args[2];
                String deslongitude = args[3];
                String sensor = args[4];
                String key = args[5];

                String uri = "https://maps.googleapis.com/maps/api/distancematrix/json?"
                        + "origins=" + latitude + "," + longitude
                        + "&destinations=" + deslatitude + "," + deslongitude
                        + "&sensor" + sensor
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
                    StringBuilder result1 = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result1.append(line);
                    }
                    input.close();
                    // Pass data to onPostExecute method
                    return (result1.toString());

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
        public void onPostExecute(String result1) {

            //List<Place> data=new ArrayList<>();
            //BigCDistItems = new ArrayList<>();
            try {
                JSONObject jsonRoot = new JSONObject(result1);
                JSONArray jArray = jsonRoot.getJSONArray("rows").getJSONObject(0)
                        .getJSONArray ("elements");
               JSONObject newDisT = jArray.getJSONObject(0);

                JSONObject distOb = newDisT.getJSONObject("distance");
                // Extract data from json and store into ArrayList as class objects


                BCDIST = distOb.getDouble("value");


                BigCDist = String.valueOf(BCDIST);




            } catch (JSONException e) {
                // You to understand what actually error is and handle it appropriately
                Log.d("Exception", e.toString());
            }
            String mlatitude = String.valueOf(currentLatitude);
            String mlongitude = String.valueOf(currentLongitude);
            String mtypes = "department_store";
            String mkeyword = "Tesco";
            String mkey = "AIzaSyBtzQ5SolvWlvApA63QW07iCqe0dz2VBqI";
            new Tesco_NearbyTask().execute(mlatitude,mlongitude,mtypes,mkeyword,mkey);

        }

    }

    public class Tesco_DistanceTask extends AsyncTask<String, String, String> {

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
                String latitude = args[0];
                String longitude = args[1];
                String deslatitude = args[2];
                String deslongitude = args[3];
                String sensor = args[4];
                String key = args[5];

                String uri = "https://maps.googleapis.com/maps/api/distancematrix/json?"
                        + "origins=" + latitude + "," + longitude
                        + "&destinations=" + deslatitude + "," + deslongitude
                        + "&sensor" + sensor
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
                    StringBuilder result1 = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result1.append(line);
                    }
                    input.close();
                    // Pass data to onPostExecute method
                    return (result1.toString());

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
        public void onPostExecute(String result1) {

            //List<Place> data=new ArrayList<>();
            //BigCDistItems = new ArrayList<>();
            try {
                JSONObject jsonRoot = new JSONObject(result1);
                JSONArray jArray = jsonRoot.getJSONArray("rows").getJSONObject(0)
                        .getJSONArray ("elements");
                JSONObject newDisT = jArray.getJSONObject(0);

                JSONObject distOb = newDisT.getJSONObject("distance");
                // Extract data from json and store into ArrayList as class objects


                TCDIST = distOb.getDouble("value");


                TescoDist = String.valueOf(TCDIST);




            } catch (JSONException e) {
                // You to understand what actually error is and handle it appropriately
                Log.d("Exception", e.toString());
            }

            if((BCDIST/1000)<=1){
                BCEXP = 35.00;
            }
            else if(((BCDIST/1000) <= 10)&&((BCDIST/1000)>1)){
                BCEXP = 35.00+(((BCDIST/1000)-1)*5.50);
            }
            else if (((BCDIST/1000) <= 20)&&((BCDIST/1000) > 10)&&((BCDIST/1000)>1)){
                BCEXP = Math.ceil(35.00+(9*5.50)+(((BCDIST/1000)-10)*6.50));
            }else if (((BCDIST/1000) <= 40)&&((BCDIST/1000) > 20)&&((BCDIST/1000) > 10)&&((BCDIST/1000)>1)){
                BCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(((BCDIST/1000)-20)*7.50));
            }else if (((BCDIST/1000) <= 60)&&((BCDIST/1000) > 40)&&((BCDIST/1000) > 20)&&((BCDIST/1000) > 10)&&((BCDIST/1000)>1)){
                BCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(20*7.50)+(((BCDIST/1000)-40)*8.00));
            }else if (((BCDIST/1000) <= 80)&&((BCDIST/1000) > 60)&&((BCDIST/1000) > 40)&&((BCDIST/1000) > 20)&&((BCDIST/1000) > 10)&&((BCDIST/1000)>1)){
                BCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(20*7.50)+(20*8.00)+(((BCDIST/1000)-60)*9.00));
            }else if(((BCDIST/1000) > 80)&&((BCDIST/1000) > 60)&&((BCDIST/1000) > 40)&&((BCDIST/1000) > 20)&&((BCDIST/1000) > 10)&&((BCDIST/1000)>1)){
                BCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(20*7.50)+(20*8.00)+(20*9.00)+(((BCDIST/1000)-80)*10.50));
            }

            if((TCDIST/1000)<=1){
                TCEXP = 35.00;
            }
            else if(((TCDIST/1000) <= 10)&&((TCDIST/1000)>1)){
                TCEXP = 35.00+(((TCDIST/1000)-1)*5.50);
            }
            else if (((TCDIST/1000) <= 20)&&((TCDIST/1000) > 10)&&((TCDIST/1000)>1)){
                TCEXP = Math.ceil(35.00+(9*5.50)+(((TCDIST/1000)-10)*6.50));
            }else if (((TCDIST/1000) <= 40)&&((TCDIST/1000) > 20)&&((TCDIST/1000) > 10)&&((TCDIST/1000)>1)){
                TCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(((TCDIST/1000)-20)*7.50));
            }else if (((TCDIST/1000) <= 60)&&((TCDIST/1000) > 40)&&((TCDIST/1000) > 20)&&((TCDIST/1000) > 10)&&((TCDIST/1000)>1)){
                TCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(20*7.50)+(((TCDIST/1000)-40)*8.00));
            }else if (((TCDIST/1000) <= 80)&&((TCDIST/1000) > 60)&&((TCDIST/1000) > 40)&&((TCDIST/1000) > 20)&&((TCDIST/1000) > 10)&&((TCDIST/1000)>1)){
                TCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(20*7.50)+(20*8.00)+(((TCDIST/1000)-60)*9.00));
            }else if(((TCDIST/1000) > 80)&&((TCDIST/1000) > 60)&&((TCDIST/1000) > 40)&&((TCDIST/1000) > 20)&&((TCDIST/1000) > 10)&&((TCDIST/1000)>1)){
                TCEXP = Math.ceil(35.00+(9*5.50)+(10*6.50)+(20*7.50)+(20*8.00)+(20*9.00)+(((TCDIST/1000)-80)*10.50));
            }


            BigCDi.setText(String.valueOf(Math.round(BCEXP))+".00 Baht");
            TesCoDi.setText(String.valueOf(Math.round(TCEXP))+".00 Baht");


        }

    }





}
