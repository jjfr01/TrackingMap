package com.example.superordenata.trackingmap;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String permission;
    static final int PERMISSION_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        controlPermission();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                final int dia = c.get(Calendar.DAY_OF_MONTH);
                final int mes = c.get(Calendar.MONTH);
                final int anio = c.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        String date;
                        String day;
                        monthOfYear++;
                        if(dayOfMonth<10){
                            day = "0" + dayOfMonth;
                        } else {
                            day = "" + dayOfMonth;
                        }
                        if(monthOfYear<10) {
                            date = "" + day + "-0" + monthOfYear + "-" + year;
                        } else {
                            date = "" + day + "-" + monthOfYear + "-" + year;
                        }
                        intent.putExtra("fecha", date);
                        startActivity(intent);
                    }
                }, anio, mes, dia);
                datePickerDialog.show();

            }
        });
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

}
