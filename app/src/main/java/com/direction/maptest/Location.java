package com.direction.maptest;

/**
 * Created by Hananideen on 13/9/2015.
 */
public class Location {

    public double Latitude;
    public double Longitude;
    public String ETA;

    public Location() {}

    public Location (double latitude, double longitude, String eta) {
        Latitude = latitude;
        Longitude = longitude;
        ETA = eta;
    }

    public Location (Json2Location jLocation){
        Latitude = jLocation.latitude;
        Longitude = jLocation.longitude;
        ETA = jLocation.eta;
    }

    public double getLatitude(){
        return Latitude;
    }

    public void setLatitude(double latitude){
        Latitude = latitude;
    }

    public double getLongitude(){
        return Longitude;
    }

    public void setLongitude(int longitude){
        Longitude = longitude;
    }

    public String getETA() {
        return ETA;
    }

    public void setETA (String eta) {
        ETA = eta;
    }
}

