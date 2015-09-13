package com.direction.maptest;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Hananideen on 13/9/2015.
 */
public class ManualActivity extends AppCompatActivity {
    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    TextView tvDistanceDuration, tvBefore;
    GPSTracker gps;
    LatLng myLoc, destination;
    double latitude, longitude, latServer, longServer;
    String sendLat, sendLong, tapLat, tapLong;
    Handler mHandler;
    Marker driver;
    Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.melodelivery_logo);

        tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);
        tvBefore = (TextView) findViewById(R.id.textViewBefore);

        markerPoints = new ArrayList<LatLng>();

        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        map = fm.getMap();
        map.setMyLocationEnabled(true);

        JsonArrayRequest locationRequest = new JsonArrayRequest(ApplicationLoader.getIp("restaurant/userlocation.php"), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject obj = response.getJSONObject(i);
                        Location coordinates = new Location(new Json2Location(obj));
                        latServer = coordinates.getLatitude();
                        longServer = coordinates.getLongitude();
                        destination = new LatLng(latServer, longServer);
                        map.addMarker(new MarkerOptions().position(destination).title("Destination " +String.format("%.3f", latServer)
                                + ", " +String.format("%.3f", longServer))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_finish)));

                        LatLng origin = myLoc;
                        LatLng dest = destination;

                        String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        downloadTask.execute(url);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("VolleyServer", "Error: " + error.getMessage());
                Toast.makeText(getApplication(), "Oops! Have you checked your internet connection?", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance().getRequestQueue().add(locationRequest);

        gps = new GPSTracker(ManualActivity.this);
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

        } else {
            gps.showSettingsAlert();
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                driver.remove();
                line.remove();

                tapLat = String.format("%.3f", latLng.latitude);
                tapLong = String.format("%.3f", latLng.longitude);

                driver = map.addMarker(new MarkerOptions().position(latLng).title("My Location " + tapLat
                        + ", " + tapLong)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.melody_logo)));

                new Send().execute("http://mynetsys.com/restaurant/deliverylocation.php?latitude="+tapLat+"&longitude="+tapLong);

                LatLng origin = latLng;
                LatLng dest = destination;

                String url = getDirectionsUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
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


    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";
            if(result.size()<1){
                //Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){
                        duration = (String)point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.BLUE);
            }

            tvDistanceDuration.setText("Distance:" + distance + ", Duration:" + duration);
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
