package com.example.superordenata.trackingmap;


import com.google.android.gms.maps.model.LatLng;

public class TrackingObject {

    private LatLng latLng;
    private String date;

    public TrackingObject() {
    }

    public TrackingObject(LatLng latLng, String date) {
        this.latLng = latLng;
        this.date = date;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
