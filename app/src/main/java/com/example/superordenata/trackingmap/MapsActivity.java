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
    private String permission;
    static final int PERMISSION_REQUEST_LOCATION = 1;

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

        controlPermission();

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapsActivity.this, "Permisos de GPS denegados, por favor activelos", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(false);
        /*mMap.setMinZoomPreference(10);
        mMap.setMaxZoomPreference(19);*/

        LatLng gr = new LatLng(37.183, -3.602028);

        myMarker = new MarkerOptions();
        myMarker.position(gr);
        myMarker.title("Mi Marcador");
        myMarker.draggable(true);

        marker = mMap.addMarker(myMarker);

        //moveCamera(gr);

        //Dibujamos la ruta
        drawRoute();

        Location last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        moveCamera(new LatLng(last.getLatitude(), last.getLongitude()));

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 5, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 5, this);

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

    private void controlPermission(){
        permission = Manifest.permission.ACCESS_FINE_LOCATION;
        checkPermission();
    }

    private void checkPermission() {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                permission);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // Ha aceptado
        } else {
            // Ha denegado o es la primera vez que se le pregunta
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // No se le ha preguntado aún
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_LOCATION);
            } else {
                // Ha denegado
                Toast.makeText(this, "Please, enable the request permission", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Estamos en el caso del teléfono
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION:

                String permission = permissions[0];
                int result = grantResults[0];

                if (permission.equals(permission)) {
                    // Comprobar si ha sido aceptado o denegado la petición de permiso
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        // Concedió su permiso
                    } else {
                        // No concendió su permiso
                        Toast.makeText(this, "You declined the access", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
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
                ArrayList<TrackingObject> result = db4o.getAllLocation(ruta);
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
