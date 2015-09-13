package com.direction.maptest;

import org.json.JSONObject;

/**
 * Created by Hananideen on 13/9/2015.
 */
public class Json2Location {

    public JSONObject jsonObject;
    public double latitude, longitude;
    public String eta;

    public Json2Location() { }

    public Json2Location(JSONObject json){
        jsonObject = json;
        if (json!=null)
        {
            latitude = json.optDouble("Latitude");
            longitude = json.optDouble("Longitude");
            eta = json.optString("ETA");

        }
        else{
            latitude = 0;
            longitude = 0;
            eta = "";
        }
    }
}
