package com.example.guide.seniorproject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by guide on 4/15/2017.
 */

public class BigCNearby {
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public static ArrayList<NearbyPlace> BPlace_data;

    public static class BigC_NearbyTask extends AsyncTask<String, String, String> {

        private ProgressDialog pdLoading ;
        HttpURLConnection conn1;
        URL url1 = null;
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
            BPlace_data = new ArrayList<>();

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
                /*BigCLat = BPlace_data.get(0).lat;
                BigCLng = BPlace_data.get(0).lng;*/

                /*TextView BigCLa = (TextView) findViewById(R.id.textView4);
                BigCLa.setText(BigCLat);*/

            } catch (JSONException e) {
                // You to understand what actually error is and handle it appropriately
                Log.d("Exception", e.toString());
            }

        }

    }
    public static ArrayList<NearbyPlace> getBPlaceItems() {
        ArrayList<NearbyPlace> BItems2 = new ArrayList<>();
        for( NearbyPlace item : BPlace_data ) {
                BItems2.add(item);
        }
        return BItems2;
    }
}
