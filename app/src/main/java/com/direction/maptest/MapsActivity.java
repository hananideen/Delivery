package com.direction.maptest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends AppCompatActivity {

    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    TextView tvDistanceDuration, tvBefore;
    GPSTracker gps;
    LatLng myLoc, destination;
    double latitude, longitude;
    String sendLat, sendLong;
    Handler mHandler;
    Marker driver;
    Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.melodelivery_logo);

        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable, 30000);

        tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);
        tvBefore = (TextView) findViewById(R.id.textViewBefore);

        markerPoints = new ArrayList<LatLng>();

        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        map = fm.getMap();
        map.setMyLocationEnabled(true);

        //TODO get destination from server
        destination = new LatLng(2.923, 101.638);
        map.addMarker(new MarkerOptions().position(destination).title("Destination " +"2.923"
                + ", " +"101.638")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_finish)));

        gps = new GPSTracker(MapsActivity.this);
        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            myLoc = new LatLng(latitude, longitude);
            sendLat =  String.format("%.3f", latitude);
            sendLong = String.format("%.3f", longitude);
            new Send().execute("http://mynetsys.com/restaurant/deliverylocation.php?latitude="+sendLat+"&longitude="+sendLong);

            CameraUpdate zoomLocation = CameraUpdateFactory.newLatLngZoom(myLoc, 15);
            driver = map.addMarker(new MarkerOptions().position(myLoc).title("My Location " + String.format("%.3f", latitude)
                    + ", " + String.format("%.3f", longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.melody_logo)));
            map.animateCamera(zoomLocation);

            //TODO send location to server

            LatLng origin = myLoc;
            LatLng dest = destination;

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

        } else {
            gps.showSettingsAlert();
        }
    }

    private final Runnable m_Runnable = new Runnable() {
        public void run() {
            driver.remove();
            line.remove();

            if(gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                myLoc = new LatLng(latitude, longitude);
                sendLat =  String.format("%.3f", latitude);
                sendLong = String.format("%.3f", longitude);
                new Send().execute("http://mynetsys.com/restaurant/deliverylocation.php?latitude="+sendLat+"&longitude="+sendLong);

                driver = map.addMarker(new MarkerOptions().position(myLoc).title("My Location" + String.format("%.3f", latitude)
                        + ", " + String.format("%.3f", longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.melody_logo)));

                //TODO send location to server
                LatLng origin = myLoc;
                LatLng dest = destination;

                String url = getDirectionsUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);

            }else {
                gps.showSettingsAlert();
            }

            Toast.makeText(MapsActivity.this,"update",Toast.LENGTH_SHORT).show();
            MapsActivity.this.mHandler.postDelayed(m_Runnable, 30000);
        }
    };

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.BLUE);
            }

            tvDistanceDuration.setText("Distance:" + distance + ", Duration:" + duration);

            // Drawing polyline in the Google Map for the i-th route
            line = map.addPolyline(lineOptions);
        }
    }

    class Send extends AsyncTask<String, Void, Boolean> {
        String result;
        @Override
        protected Boolean doInBackground(String... params) {
            try{
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                HttpURLConnection conn = (HttpURLConnection)connection;
                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is,"UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + " sucess");
                    }
                    result = sb.toString();
                    return true;
                }
            }catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
