package com.example.superordenata.trackingmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private MarkerOptions myMarker;
    private Marker marker;
    private String ruta;

    private LocationManager locationManager;
    private PolylineOptions polylineOptions;
    private ArrayList<LatLng> arrayUbicacion = new ArrayList<LatLng>();
    private Intent serviceTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ruta = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/db.db4o";
        Toast.makeText(this, ruta, Toast.LENGTH_LONG).show();

        serviceTracking = new Intent(this, TrackingService.class);

        this.askGPS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.askGPS();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        polylineOptions = new PolylineOptions()
                        .geodesic(true)
                        .color(Color.BLUE)
                        .width(10);

        locationManager = (LocationManager) MapsActivity.this.getSystemService(LOCATION_SERVICE);

        loadRoute();

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(false);
        /*mMap.setMinZoomPreference(10);
        mMap.setMaxZoomPreference(19);*/

        LatLng gr = new LatLng(37.183, -3.602028);

        myMarker = new MarkerOptions();
        myMarker.position(gr);
        myMarker.draggable(true);

        marker = mMap.addMarker(myMarker);

        //moveCamera(gr);

        //Dibujamos la ruta
        drawRoute();

        Location last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(last != null) {
            moveCamera(new LatLng(last.getLatitude(), last.getLongitude()));
        }
        last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(last != null) {
            moveCamera(new LatLng(last.getLatitude(), last.getLongitude()));
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 120000, 50, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 50, this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(MapsActivity.this, "Lat: " + latLng.latitude + "\nLon: " + latLng.longitude, Toast.LENGTH_SHORT).show();
                moveCamera(latLng);
                marker.setPosition(latLng);
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
                Toast.makeText(MapsActivity.this, "Lat: " + latLng.latitude + "\nLon: " + latLng.longitude, Toast.LENGTH_SHORT).show();
                moveCamera(latLng);
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopService(serviceTracking);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getProvider().equals("gps")) {
            Toast.makeText(MapsActivity.this, "Cambiado!", Toast.LENGTH_SHORT).show();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = df.format(c);
            logLocation(location.getLatitude(), location.getLongitude(), formattedDate);
        } else if(location.getProvider().equals("network")) {
            Toast.makeText(MapsActivity.this, "Cambiado!", Toast.LENGTH_SHORT).show();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = df.format(c);
            logLocation(location.getLatitude(), location.getLongitude(), formattedDate);
        } else {
            Toast.makeText(MapsActivity.this, "Error al registras la ubicacion", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }



    public void askGPS(){
        try {
            int gpsSignal = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

            if(gpsSignal == 0){
                //No está activo el GPS
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void moveCamera(LatLng latLng){
        CameraPosition camera = new CameraPosition.Builder()
                .target(latLng)
                .zoom(18)//Limite 21
                .bearing(0)//Giro desde el Este
                .tilt(0)//Inclinacion de la camara, limite 90
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));
    }

    public void drawRoute(){
        for(LatLng value : arrayUbicacion){
            polylineOptions.add(value);
        }

        mMap.addPolyline(polylineOptions);
    }

    private void logLocation(double latitud, double longitud, String fecha){
        serviceTracking.putExtra("latitud", latitud);
        serviceTracking.putExtra("longitud", longitud);
        serviceTracking.putExtra("fecha", fecha);
        serviceTracking.putExtra("ruta", ruta);
        this.startService(serviceTracking);
    }

    public void loadRoute(){
        Thread hilo = new Thread(){
            @Override
            public void run() {
                super.run();
                Db4o db4o = new Db4o();
                //ArrayList<TrackingObject> result = db4o.getAllLocation(ruta);
                ArrayList<TrackingObject> result = db4o.getLocationByDate(getIntent().getStringExtra("fecha"), ruta);
                for(TrackingObject value : result){
                    arrayUbicacion.add(value.getLatLng());
                }
            }
        };

        hilo.start();
        try {
            hilo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
