package com.example.guide.seniorproject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

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
import java.util.List;

/**
 * Created by guide on 4/14/2017.
 */

public class BigCDistance {

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public static List<NearbyPlace> BigCPlace_data;
    public static List<Integer> BigCDist;
    public static ArrayList<DistanceList> minPlace;



    public static class DistanceTask extends AsyncTask<String, String, String> {

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
            BigCDist = new ArrayList<>();
            try {
                JSONObject jsonRoot = new JSONObject(result1);
                JSONArray jArray = jsonRoot.getJSONArray("rows").getJSONObject(0)
                        .getJSONArray ("elements");

                // Extract data from json and store into ArrayList as class objects
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    BigCDist.add(json_data.getInt("value"));
                }

                AddedListProducts.BigCDist = String.valueOf(BigCDist.get(0));


            } catch (JSONException e) {
                // You to understand what actually error is and handle it appropriately
                Log.d("Exception", e.toString());
            }

        }

    }

    /*static ArrayList<DistanceList> getDistance(String latitude1, String longitude1) {
        ArrayList<Integer> BigCDistanceList = new ArrayList<>();
        try {
            String sensor1 = "false";
            String key2 = "AIzaSyBtzQ5SolvWlvApA63QW07iCqe0dz2VBqI";
            for (int i = 0; i < BigCPlace_data.size(); i++) {
                new DistanceTask().execute(latitude1, longitude1, BigCPlace_data.get(i).lat, BigCPlace_data.get(i).lng, sensor1, key2);
                BigCDistanceList.add(BigCDist.get(0));
            }
        }
        catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        try {
            int min = BigCDistanceList.get(0);
            int minIndex = 0;
            for (int i = 0; i < BigCDistanceList.size(); i++) {
                int number = BigCDistanceList.get(i);
                if (number < min) {
                    min = number;
                    minIndex = i;

                }
            }

            minPlace = new ArrayList<>();
            DistanceList m = new DistanceList();
            m.Distance = BigCDistanceList.get(minIndex);
            m.lat = BigCPlace_data.get(minIndex).lat;
            m.lng = BigCPlace_data.get(minIndex).lng;
            m.place_name = BigCPlace_data.get(minIndex).place_name;
            m.vicinity = BigCPlace_data.get(minIndex).vicinity;

            minPlace.add(m);
        }
        catch (Exception e){
            Log.d("Exception", e.toString());
        }

        return minPlace;
    }*/
}
