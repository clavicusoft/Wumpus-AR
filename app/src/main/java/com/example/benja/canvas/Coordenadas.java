package com.example.benja.canvas;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class Coordenadas extends AppCompatActivity  {

    LocationManager locationManager;
    SpinnerActivity sp;
    double latitudeGPS;
    double longitudeGPS;
    int distancia;
    TextView tv_info, tv_dist;
    boolean flag;
    Spinner spn_distances;
    String info;
    int caves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordenadas);
        tv_info = (TextView) findViewById(R.id.tv_information);
        tv_dist = (TextView) findViewById(R.id.tv_dist);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeGPS = 0.0;
        latitudeGPS = 0.0;
        flag = false;

        //Spinner
        spn_distances = (Spinner) findViewById(R.id.spn_distancias);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.distances, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_distances.setAdapter(adapter);
        sp = new SpinnerActivity();
        spn_distances.setOnItemSelectedListener(sp);

        //Recibe el id del grafo
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        String graphID = b.getString("graphID");

        //Acceso a la BD
        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 5);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cell = db.rawQuery("SELECT GRAPH.relations, GRAPH.number_of_caves FROM GRAPH WHERE GRAPH.id = \"" + graphID +"\";", null);
        if (cell.moveToFirst()){
            info=cell.getString(0);
            caves=cell.getInt(1);
            cell.close();
            Toast.makeText(this, "ID: " + graphID + "\nrelaciones: " + info + "\ncaves: " + caves,  Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Error obteniendo el las relaciones y el ID!", Toast.LENGTH_LONG).show();
            db.close();
        }
        cell.close();
    }

    public void getCurrentLocation(View v) {
        flag = displayGpsStatus();
        if (flag) {
            if(sp.selected){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 20 * 1000, 10, locationListenerGPS);
                tv_dist.setText(distancia + " metros.");
            }
            else {
                Toast.makeText(this, "Por favor indique la distancia deseada.", Toast.LENGTH_LONG).show();
            }
        } else {
            createAlertDialog("Estado del GPS", "El GPS está desactivado.");
        }
    }

    private boolean displayGpsStatus() {
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;
        }
        else {
            return false;
        }
    }

    protected void createAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Activar GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(i);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public double getLatitudeGPS() { return latitudeGPS;  }

    public double getLongitudeGPS() {
        return longitudeGPS;
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String coordinates = "Longitude: " + getLongitudeGPS() + "\nLatitude: " + getLatitudeGPS();
                    tv_info.setText(coordinates);
                }
            });
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
    };

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        boolean selected;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            selected = true;
            switch(pos){
                case 0:
                    distancia = 6;
                    break;
                case 1:
                    distancia = 8;
                    break;
                case 2:
                    distancia = 10;
                    break;
                case 3:
                    distancia = 12;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            selected = false;
        }
    }

    public void putCaves () {
        for (int i = 0; i < caves; ++i) {
            putCave(i);
        }
    }

    public void putCave (int cave) {
        switch (caves) {
            case 2:
                /*
                *  1 - 2
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS + distancia, longitudeGPS);
                break;
            case 3:
                /*
                *   3
                * 1 - 2
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS + distancia, longitudeGPS);
                createCave(3, latitudeGPS + (distancia/2), longitudeGPS + distancia);
                break;
            case 4:
                /*
                *  3 - 4
                *  |   |
                *  1 - 2
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS + distancia, longitudeGPS);
                createCave(3, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(4, latitudeGPS, longitudeGPS + distancia);
                break;
            case 5:
                /*
                *  4  -  2
                *  |  1  |
                *  5  -  3
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(3, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(4, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(5, latitudeGPS - distancia, longitudeGPS - distancia);
                break;
            case 6:
                /*
                *  2 - 5
                *  |   |
                *  1 - 4
                *  |   |
                *  3 - 6
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                break;
            case 7:
                /*
                *      2 - 5
                *      |   |
                *  7 - 1 - 4
                *      |   |
                *      3 - 6
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                break;
            case 8:
                /*
                *  8 - 2 - 5
                *  |   |   |
                *  7 - 1 - 4
                *      |   |
                *      3 - 6
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                break;
            case 9:
                /*
                *  8 - 2 - 5
                *  |   |   |
                *  7 - 1 - 4
                *  |   |   |
                *  9 - 3 - 6
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                break;
            case 10:
                /*
                *  8 - 2 - 5
                *  |   |   |
                *  7 - 1 - 4 - 10
                *  |   |   |
                *  9 - 3 - 6
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                break;
            case 11:
                /*
                *  8 - 2 - 5 - 11
                *  |   |   |   |
                *  7 - 1 - 4 - 10
                *  |   |   |
                *  9 - 3 - 6
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                break;
            case 12:
                /*
                *  8 - 2 - 5 - 11
                *  |   |   |   |
                *  7 - 1 - 4 - 10
                *  |   |   |   |
                *  9 - 3 - 6 - 12
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                break;
            case 13:
                /*
                *  8 - 2 - 5 - 11
                *  |   |   |   |
                *  7 - 1 - 4 - 10
                *  |   |   |   |
                *  9 - 3 - 6 - 12
                *  |
                *  13
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - (distancia * 2), longitudeGPS - distancia);
                break;
            case 14:
                /*
                *  8 - 2 - 5 - 11
                *  |   |   |   |
                *  7 - 1 - 4 - 10
                *  |   |   |   |
                *  9 - 3 - 6 - 12
                *  |   |
                *  13- 14
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - distancia, longitudeGPS - (distancia * 2));
                createCave(14, latitudeGPS, longitudeGPS - (distancia * 2));
                break;
            case 15:
                /*
                *  8 - 2 - 5 - 11
                *  |   |   |   |
                *  7 - 1 - 4 - 10
                *  |   |   |   |
                *  9 - 3 - 6 - 12
                *  |   |   |
                *  13- 14- 15
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - distancia, longitudeGPS - (distancia * 2));
                createCave(14, latitudeGPS, longitudeGPS - (distancia * 2));
                createCave(15, latitudeGPS + distancia, longitudeGPS - (distancia * 2));
                break;
            case 16:
                /*
                *  8 - 2 - 5 - 11
                *  |   |   |   |
                *  7 - 1 - 4 - 10
                *  |   |   |   |
                *  9 - 3 - 6 - 12
                *  |   |   |   |
                *  13- 14- 15- 16
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - distancia, longitudeGPS - (distancia * 2));
                createCave(14, latitudeGPS, longitudeGPS - (distancia * 2));
                createCave(15, latitudeGPS + distancia, longitudeGPS - (distancia * 2));
                createCave(16, latitudeGPS + (distancia * 2), longitudeGPS - (distancia * 2));
                break;
            case 17:
                /*
                *  17- 8 - 2 - 5 - 11
                *      |   |   |   |
                *      7 - 1 - 4 - 10
                *      |   |   |   |
                *      9 - 3 - 6 - 12
                *      |   |   |   |
                *      13- 14- 15- 16
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - distancia, longitudeGPS - (distancia * 2));
                createCave(14, latitudeGPS, longitudeGPS - (distancia * 2));
                createCave(15, latitudeGPS + distancia, longitudeGPS - (distancia * 2));
                createCave(16, latitudeGPS + (distancia * 2), longitudeGPS - (distancia * 2));
                createCave(17, latitudeGPS - (distancia * 2), longitudeGPS + distancia);
                break;
            case 18:
                /*
                *  17- 8 - 2 - 5 - 11
                *  |   |   |   |   |
                *  18- 7 - 1 - 4 - 10
                *      |   |   |   |
                *      9 - 3 - 6 - 12
                *      |   |   |   |
                *      13- 14- 15- 16
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - distancia, longitudeGPS - (distancia * 2));
                createCave(14, latitudeGPS, longitudeGPS - (distancia * 2));
                createCave(15, latitudeGPS + distancia, longitudeGPS - (distancia * 2));
                createCave(16, latitudeGPS + (distancia * 2), longitudeGPS - (distancia * 2));
                createCave(17, latitudeGPS - (distancia * 2), longitudeGPS + distancia);
                createCave(18, latitudeGPS - (distancia * 2), longitudeGPS);
                break;
            case 19:
                /*
                *  17- 8 - 2 - 5 - 11
                *  |   |   |   |   |
                *  18- 7 - 1 - 4 - 10
                *  |   |   |   |   |
                *  19- 9 - 3 - 6 - 12
                *      |   |   |   |
                *      13- 14- 15- 16
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - distancia, longitudeGPS - (distancia * 2));
                createCave(14, latitudeGPS, longitudeGPS - (distancia * 2));
                createCave(15, latitudeGPS + distancia, longitudeGPS - (distancia * 2));
                createCave(16, latitudeGPS + (distancia * 2), longitudeGPS - (distancia * 2));
                createCave(17, latitudeGPS - (distancia * 2), longitudeGPS + distancia);
                createCave(18, latitudeGPS - (distancia * 2), longitudeGPS);
                createCave(19, latitudeGPS - (distancia * 2), longitudeGPS - distancia);
                break;
            case 20:
                /*
                *  17- 8 - 2 - 5 - 11
                *  |   |   |   |   |
                *  18- 7 - 1 - 4 - 10
                *  |   |   |   |   |
                *  19- 9 - 3 - 6 - 12
                *  |   |   |   |   |
                *  20- 13- 14- 15- 16
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, latitudeGPS, longitudeGPS + distancia);
                createCave(3, latitudeGPS, longitudeGPS - distancia);
                createCave(4, latitudeGPS + distancia, longitudeGPS);
                createCave(5, latitudeGPS + distancia, longitudeGPS + distancia);
                createCave(6, latitudeGPS + distancia, longitudeGPS - distancia);
                createCave(7, latitudeGPS - distancia, longitudeGPS);
                createCave(8, latitudeGPS - distancia, longitudeGPS + distancia);
                createCave(9, latitudeGPS - distancia, longitudeGPS - distancia);
                createCave(10, latitudeGPS + (distancia * 2), longitudeGPS);
                createCave(11, latitudeGPS + (distancia * 2), longitudeGPS + distancia);
                createCave(12, latitudeGPS + (distancia * 2), longitudeGPS - distancia);
                createCave(13, latitudeGPS - distancia, longitudeGPS - (distancia * 2));
                createCave(14, latitudeGPS, longitudeGPS - (distancia * 2));
                createCave(15, latitudeGPS + distancia, longitudeGPS - (distancia * 2));
                createCave(16, latitudeGPS + (distancia * 2), longitudeGPS - (distancia * 2));
                createCave(17, latitudeGPS - (distancia * 2), longitudeGPS + distancia);
                createCave(18, latitudeGPS - (distancia * 2), longitudeGPS);
                createCave(19, latitudeGPS - (distancia * 2), longitudeGPS - distancia);
                createCave(20, latitudeGPS - (distancia * 2), longitudeGPS - (distancia * 2));
                break;
        }
    }

    public void createCave (int numberOfCave, double coordX, double coordY) {
        //TODO: INSERTAR EN LA BASE DE DATOS
    }

}