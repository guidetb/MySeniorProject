package com.example.guide.seniorproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.List;

/**
 * Created by guide on 3/19/2017.
 */

public class BIGCTab extends Fragment  {
    //Overriden method onCreateView
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public static RecyclerView mRVProduct2;
    public static AdapterProduct mAdapter2;
    public static List<DataProduct> data2;
    public static ArrayList<DataProduct> selectedBigCItems;
    private static Context context = null;
    Button button1;
    Button button2;
    private Context applicationContext;

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        View view = inflater.inflate(R.layout.tab2, container, false);
        mRVProduct2 = (RecyclerView) view.findViewById(R.id.productPriceList2);
        context = getActivity();
        selectedBigCItems = new ArrayList<>();
        return view;

    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public static class AsyncFetch extends AsyncTask<String, String, String> {

        private ProgressDialog pdLoading ;
        HttpURLConnection conn;
        URL url = null;
        String searchQuery;

        public AsyncFetch(String searchQuery){this.searchQuery=searchQuery;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading = new ProgressDialog(context);
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        public String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.3.2/Myseniorproject/getData2.php");

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
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // add parameter to our above url
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("searchQuery", searchQuery);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }

            try {

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

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return("Connection error");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        public void onPostExecute(String result) {

            //this method will be running on UI thread
            pdLoading.dismiss();
            //List<DataProduct> data=new ArrayList<>();
            data2 = new ArrayList<>();
            pdLoading.dismiss();
            if(result.equals("no rows")) {
                Toast.makeText(context, "No Results found for entered query", Toast.LENGTH_LONG).show();
            }else{

                try {

                    JSONArray jArray = new JSONArray(result);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        DataProduct productData = new DataProduct();
                        productData.ProductImg = json_data.getString("img_scr");
                        productData.ProducrName = json_data.getString("product_name");
                        productData.price = json_data.getDouble("price");
                        data2.add(productData);
                    }

                    // Setup and Handover data to recyclerview

                    mAdapter2 = new AdapterProduct(context, data2);
                    mRVProduct2.setAdapter(mAdapter2);
                    mRVProduct2.setLayoutManager(new LinearLayoutManager(context));

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                }

            }

        }

    }
    public static ArrayList<DataProduct> getSelectedItems() {
        ArrayList<DataProduct> selectedItems2 = new ArrayList<>();
        for( DataProduct item : data2 ) {
            if( item.isSelected() )
                selectedItems2.add(item);
        }
        return selectedItems2;
    }
}