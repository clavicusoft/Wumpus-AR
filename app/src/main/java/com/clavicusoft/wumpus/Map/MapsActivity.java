package com.clavicusoft.wumpus.Map;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.clavicusoft.wumpus.AR.Game_Multiplayer;
import com.clavicusoft.wumpus.AR.Game_World;
import com.clavicusoft.wumpus.Bluetooth.BluetoothChat;
import com.clavicusoft.wumpus.Database.AdminSQLite;
import com.clavicusoft.wumpus.Maze.CaveContent;
import com.clavicusoft.wumpus.Maze.Graph;
import com.clavicusoft.wumpus.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.widget.Spinner;

/**
 * Shows the real location in the map and generates the labyrinth from where the user wishes. Storing them in the database with a game id
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    GoogleMap mMap;

    private double latitude;
    private double longitude;

    double selectedLatitude;
    double selectedLongitude;
    double meterToCoordinates;

    int graph_ID;
    String info;
    int numberCaves;
    int game_id;
    double distance;

    Button btnContinue;
    Button btnTerrain;
    Button btnHybrid;
    Button btnListo;
    Button btnShare;

    SpinnerActivity sp;
    Spinner spn_distances; //Displays the available distances between caves.

    String tipo = "";

    boolean creado;

    Boolean multiplayer;

    String gameRoom;
    String username;

    Long dateTime;

    String msj = "";
    String msjValues[] = null;

    Graph graph;
    CaveContent[] caveContents;
    /**
     * Obtain the SupportMapFragment and get notified when the map is ready to be used. Further,
     * gets the number of caves and the relationships according to the id of the graph in the database.
     *
     * @param savedInstanceState State of the instance saved
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateTime = System.currentTimeMillis()/1000;
        Bundle b;
        b = getIntent().getExtras();
        tipo = getIntent().getStringExtra("tipo");
        multiplayer = false;

        if (tipo.equals("multijugador")) {
            multiplayer = true;

            username = dateTime.toString();

            setContentView(R.layout.activity_multiplayer_maps);
            btnListo = (Button) findViewById(R.id.bListo);

            btnListo.setOnClickListener(this);

            msj = getIntent().getStringExtra("data");
            //msj = laberinto+"%"+numberCaves+"%"+latitude+"%"+longitude+"%"+distance+"%"+cavesInf;
            //laberinto [relations,caves,name]
            //numberCaves = [int]
            //latitude = [double]
            //longitude = [double]
            //distance = [double]
            //cavesInf = numberCaves*6  -> [int, int, int, double, double, int]
            //GameRoom = dateTime + BluetoothName
            msjValues = tokenizer(msj);

            graph_ID = createGraph(msjValues[2], msjValues[0], Integer.parseInt(msjValues[1]));

            numberCaves = Integer.parseInt(msjValues[3]);

            latitude = Double.parseDouble(msjValues[4]);
            longitude = Double.parseDouble(msjValues[5]);
            selectedLatitude = latitude;
            selectedLongitude = longitude;

            distance = Double.parseDouble(msjValues[6]);

            gameRoom = msjValues[8];

            meterToCoordinates = 0.0000095;

            creado = true;

            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

            if (status == ConnectionResult.SUCCESS) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            } else {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, (Activity) getApplicationContext(), 10);
                dialog.show();
            }

            accessBD(graph_ID);

        } else {
            setContentView(R.layout.activity_maps);
            btnContinue = (Button) findViewById(R.id.bcontinuar);
            btnHybrid = (Button) findViewById(R.id.bhibrido);
            btnTerrain = (Button) findViewById(R.id.bterreno);
            btnListo = (Button) findViewById(R.id.bListo);
            btnShare = (Button) findViewById(R.id.btCompar);
            btnContinue.setOnClickListener(this);
            btnTerrain.setOnClickListener(this);
            btnHybrid.setOnClickListener(this);
            btnListo.setOnClickListener(this);
            btnShare.setOnClickListener(this);
            creado = false;
            longitude = b.getDouble("Longitud");
            latitude = b.getDouble("Latitud");
            graph_ID = b.getInt("graphID");
            distance = b.getDouble("Distancia");
            selectedLatitude = 0.0;
            selectedLongitude = 0.0;
            meterToCoordinates = 0.0000095;

            spn_distances = (Spinner) findViewById(R.id.spn_distancias);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.distances, R.layout.spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spn_distances.setAdapter(adapter);
            sp = new SpinnerActivity();
            spn_distances.setOnItemSelectedListener(sp);

            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            if (status == ConnectionResult.SUCCESS) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            } else {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, (Activity) getApplicationContext(), 10);
                dialog.show();
            }

            accessBD(graph_ID);
            generateCaveContent();
        }
    }

    /**
     * Button Functions, change the terrain map type to hybrid. On the other hand, you can start creating the
     * labyrinth from the point the user chooses and displays it on the map.
     *
     * @param view Used view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bterreno:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.bhibrido:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.bcontinuar: {
                if (selectedLatitude != 0.0 && selectedLongitude != 0.0) {
                    //Create caves
                    putCave(numberCaves, selectedLatitude, selectedLongitude);
                } else {//Create caves
                    putCave(numberCaves, latitude, longitude);
                }
                creado = true;
            }
            break;
            case R.id.bListo:
                if (creado) {
                    startGame();
                } else {
                    Toast.makeText(MapsActivity.this, "Debe crear un mapa de juego", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btCompar:
                if (creado) {
                    String laberinto = getLaberinto(graph_ID);
                    String caves[] = getCaves();
                    String cavesInf = "";
                    for (int i = 1; i <= numberCaves; i++) {
                        cavesInf = cavesInf + "%" + caves[i - 1];
                    }

                    msj = laberinto + "%" + numberCaves + "%" + latitude + "%" + longitude + "%" + distance + "%" + cavesInf + "%" + dateTime.toString();

                    Intent i = new Intent(MapsActivity.this, BluetoothChat.class);
                    i.putExtra("game_ID", game_id);
                    i.putExtra("number_of_caves", numberCaves);
                    i.putExtra("funcion", "enviarEmplazamiento");
                    i.putExtra("data", msj);
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(MapsActivity.this, R.anim.fade_in, R.anim.fade_out);
                    startActivity(i, options.toBundle());
                } else {
                    Toast.makeText(MapsActivity.this, "Debe crear un mapa de juego", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * Also, with a long click, it displays a new marker.
     *
     * @param googleMap google Map that is shown
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        // Add a marker in the current point and move the camera
        LatLng current = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(current).title("Ubicación Actual").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        float zoomLevel = 16;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, zoomLevel));

        if (tipo.equals("multijugador")) {
            mMap.clear();
            clearDB();
            for (int i = 8; i <= (numberCaves+1)*6; i = i + 6) {
                int cave_number = Integer.parseInt(msjValues[i + 2]);
                double lat = Double.parseDouble(msjValues[i + 3]);
                double lon = Double.parseDouble(msjValues[i + 4]);
                int contenido = Integer.parseInt(msjValues[i + 5]);
                createCaveExp(cave_number, lat, lon, contenido);
            }
        } else {
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLngChosen) {
                    mMap.clear();
                    LatLng actual = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(actual).title("Ubicación Actual").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    mMap.addMarker(new MarkerOptions().title("Posicion Deseada").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(latLngChosen));
                    selectedLatitude = latLngChosen.latitude;
                    selectedLongitude = latLngChosen.longitude;
                }
            });

        }
    }

    /**
     * Gets the number of caves and the relationships according to the id of the graph in the database.
     *
     * @param graph_id id of the selected graph.
     */

    public void accessBD(int graph_id) {

        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cell = db.rawQuery("SELECT GRAPH.relations, GRAPH.number_of_caves FROM GRAPH WHERE GRAPH.id = " + graph_id + ";", null);
        if (cell.moveToFirst()) {
            info = cell.getString(0);
            numberCaves = cell.getInt(1);
            cell.close();
        } else {
            Toast.makeText(this, "Error obteniendo el las relaciones y el ID!", Toast.LENGTH_LONG).show();
            db.close();
        }
        cell.close();
        cell = db.rawQuery("SELECT MAX(id) FROM GAME;", null);
        if (cell.moveToFirst()) {
            game_id = cell.getInt(0) + 1;
            cell.close();
        } else {
            Toast.makeText(this, "Error obteniendo el las ID del juego!", Toast.LENGTH_LONG).show();
            db.close();
        }
        cell.close();
    }

    public void generateCaveContent() {
        graph = new Graph(numberCaves);
        //TODO posible cambio
        caveContents = graph.randomEntitiesGen(0);
    }

    /**
     * Generates the location for the Maze's caves.
     *
     * @param cave         Number of caves in the Graph.
     * @param latitudeGPS  Player's latitude.
     * @param longitudeGPS Player's longitude.
     */
    public void putCave(int cave, double latitudeGPS, double longitudeGPS) {
        mMap.clear();
        clearDB();
        switch (numberCaves) {
            case 2:
                /*
                *  1 - 2
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                break;
            case 3:
                /*
                *     3
                *     |
                * 1 - 2
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(3, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                break;
            case 4:
                /*
                *  3 - 4
                *  |   |
                *  1 - 2
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(3, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(4, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                break;
            case 5:
                /*
                *  4  -  2
                *  |  1  |
                *  5  -  3
                */
                createCave(1, latitudeGPS, longitudeGPS);
                createCave(2, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(14, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 2, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(14, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(15, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(14, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(15, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(16, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(14, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(15, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(16, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(17, addMetersToLatitude(latitudeGPS, distance, 2, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(14, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(15, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(16, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(17, addMetersToLatitude(latitudeGPS, distance, 2, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(18, addMetersToLatitude(latitudeGPS, distance, 2, false), longitudeGPS);
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(14, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(15, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(16, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(17, addMetersToLatitude(latitudeGPS, distance, 2, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(18, addMetersToLatitude(latitudeGPS, distance, 2, false), longitudeGPS);
                createCave(19, addMetersToLatitude(latitudeGPS, distance, 2, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
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
                createCave(2, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(3, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(4, addMetersToLatitude(latitudeGPS, distance, 1, true), longitudeGPS);
                createCave(5, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(6, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(7, addMetersToLatitude(latitudeGPS, distance, 1, false), longitudeGPS);
                createCave(8, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(9, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(10, addMetersToLatitude(latitudeGPS, distance, 2, true), longitudeGPS);
                createCave(11, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(12, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(13, addMetersToLatitude(latitudeGPS, distance, 1, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(14, latitudeGPS, addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(15, addMetersToLatitude(latitudeGPS, distance, 1, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(16, addMetersToLatitude(latitudeGPS, distance, 2, true),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                createCave(17, addMetersToLatitude(latitudeGPS, distance, 2, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, true));
                createCave(18, addMetersToLatitude(latitudeGPS, distance, 2, false), longitudeGPS);
                createCave(19, addMetersToLatitude(latitudeGPS, distance, 2, false),
                        addMetersToLongitude(longitudeGPS, distance, 1, false));
                createCave(20, addMetersToLatitude(latitudeGPS, distance, 2, false),
                        addMetersToLongitude(longitudeGPS, distance, 2, false));
                break;
        }
    }

    /**
     * This method start the game.
     */
    public void startGame() {
        Intent i;
        if (multiplayer)
        {
            i = new Intent(this, Game_Multiplayer.class);
            i.putExtra("gameRoom", gameRoom);
            i.putExtra("username", username);
        }
        else
        {
            i = new Intent(this, Game_World.class);
        }
        i.putExtra("game_ID", game_id);
        i.putExtra("number_of_caves", numberCaves);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                R.anim.fade_out);
        startActivity(i, options.toBundle());
    }

    /**
     * Stores the cave in the DB.
     *
     * @param cave_number The number of the cave inside the graph.
     * @param coordX      The latitude of the cave.
     * @param coordY      The longitude of the cave.
     */
    public void createCave(int cave_number, double coordX, double coordY) {
        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        ContentValues data = new ContentValues();
        data.put("id", game_id);
        data.put("graph_id", graph_ID);
        data.put("cave_number", cave_number);
        data.put("latitude", String.valueOf(coordX));
        data.put("longitude", String.valueOf(coordY));
        data.put("content", caveContents[cave_number - 1].getValue());
        db.insert("GAME", null, data);

        LatLng newCave = new LatLng(coordX, coordY);

        if (cave_number == 1) {
            mMap.addMarker(new MarkerOptions().position(newCave).title("Cueva " + cave_number)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        } else {
            mMap.addMarker(new MarkerOptions().position(newCave).title("Cueva " + cave_number)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

    }


    /**
     * Adds meters to the latitude of a location.
     *
     * @param latitude Actual latitude.
     * @param meters   Amount of meters to add.
     * @param times    Number of times you want to add those meters.
     * @param sum      Checks if you want to add or subtract the meters.
     * @return The new latitude.
     */
    public double addMetersToLatitude(double latitude, double meters, double times, boolean sum) {
        double result;
        if (sum) {
            result = latitude + (times * (180 / Math.PI) * (meters / 6378137));
        } else {
            result = latitude - (times * (180 / Math.PI) * (meters / 6378137));
        }
        return result;
    }

    /**
     * Adds meters to the longitude of a location.
     *
     * @param longitude Actual longitude.
     * @param meters    Amount of meters to add.
     * @param times     Number of times you want to add those meters.
     * @param sum       Checks if you want to add or subtract the meters.
     * @return The new longitude.
     */
    public double addMetersToLongitude(double longitude, double meters, double times, boolean sum) {
        double result;
        if (sum) {
            //result = longitude + (times * (180/Math.PI) * (meters/6378137) / Math.cos(Math.PI/180.0 * longitude));
            result = longitude + (times * (180 / Math.PI) * (meters / 6378137));
        } else {
            //result = longitude - (times * (180/Math.PI) * (meters/6378137) / Math.cos(Math.PI/180.0 * longitude));
            result = longitude - (times * (180 / Math.PI) * (meters / 6378137));
        }
        return result;
    }

    /**
     * Clears the DB from the previous coordinates.
     */
    public void clearDB() {
        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        db.execSQL("DELETE FROM GAME WHERE id = " + String.valueOf(game_id));
    }

    /**
     * Get information about the lab.
     *
     * @param id labs id
     * @return labs information.
     */
    public String getLaberinto(int id) {
        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor cell = db.rawQuery("SELECT * FROM GRAPH WHERE GRAPH.id = \"" + id + "\";", null);
        String name = "";
        String relations = "";
        String number_of_caves = "";
        if (cell.moveToFirst()) {
            relations = cell.getString(1);
            number_of_caves = cell.getString(2);
            name = cell.getString(3);
            cell.close();
        } else {
            Toast.makeText(this, "The Wumpus isn't around this caves. Try another one!", Toast.LENGTH_LONG).show();
            db.close();
        }
        return relations + "%" + number_of_caves + "%" + name;
    }


    /**
     * Save all the caves information in caves[] and returns the array.
     *
     * @return array that contains all of created caves information.
     */
    public String[] getCaves() {
        String caves[] = new String[numberCaves];
        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();
        for (int i = 1; i <= numberCaves; i++) {
            Cursor cell = db.rawQuery("SELECT * FROM GAME WHERE GAME.cave_number = \"" + i + "\" AND GAME.id = " + game_id + ";", null);
            if (cell.moveToFirst()) {
                caves[i - 1] = cell.getString(0) + "%" + cell.getString(1) + "%" + cell.getString(2) + "%" + cell.getString(3) + "%" + cell.getString(4) + "%" + cell.getString(5);
                cell.close();
            }
        }
        return caves;
    }

    /**
     * This method split the message and interprets the information.
     *
     * @param msj msg received
     * @return the information interpreted
     */
    public String[] tokenizer(String msj) {
        String[] mensaje = msj.split("%");
        return mensaje;
    }


    /**
     * This method creates the graph, but first seeks to know if the graph exists, if the graph exists and the relations
     * are equals, the return the graph_id, if the name is the same but the relations are different, creates a new graph
     * with name+1 as name.
     * If the method doesn't find any graph with this name, then creates a new one.
     *
     * @param name
     * @param relations
     * @param number_of_caves
     * @return the graph id.
     */
    public int createGraph(String name, String relations, int number_of_caves) {
        AdminSQLite admin = new AdminSQLite(MapsActivity.this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor cell = db.rawQuery("SELECT * FROM GRAPH WHERE GRAPH.name = \"" + name + "\";", null);
        if (cell.moveToFirst()) {
            String test = cell.getString(1);
            if (cell.getString(1).equals(relations)) {
                graph_ID = Integer.parseInt(cell.getString(0));
            } else {
                ContentValues data = new ContentValues();
                data.put("name", name + "1");
                data.put("relations", relations);
                data.put("number_of_caves", number_of_caves);
                db.insert("GRAPH", null, data);
                Cursor cell2 = db.rawQuery("SELECT * FROM GRAPH WHERE GRAPH.name = \"" + name + "1" + "\";", null);
                if (cell.moveToFirst()) {
                    graph_ID = Integer.parseInt(cell2.getString(0));
                }
            }
        } else {
            ContentValues data = new ContentValues();
            data.put("name", name);
            data.put("relations", relations);
            data.put("number_of_caves", number_of_caves);
            db.insert("GRAPH", null, data);
            Cursor cell2 = db.rawQuery("SELECT * FROM GRAPH WHERE GRAPH.name = \"" + name + "\";", null);
            if (cell.moveToFirst()) {
                graph_ID = Integer.parseInt(cell2.getString(0));
            }
        }
        return graph_ID;
    }


    /**
     * Stores the cave in the DB from emplacement shared.
     *
     * @param cave_number The number of the cave inside the graph.
     * @param coordX      The latitude of the cave.
     * @param coordY      The longitude of the cave.
     * @param content     The content of the cave.
     */
    public void createCaveExp(int cave_number, double coordX, double coordY, int content) {
        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        ContentValues data = new ContentValues();
        data.put("id", game_id);
        data.put("graph_id", graph_ID);
        data.put("cave_number", cave_number);
        data.put("latitude", String.valueOf(coordX));
        data.put("longitude", String.valueOf(coordY));
        data.put("content", content);
        db.insert("GAME", null, data);

        LatLng newCave = new LatLng(coordX, coordY);

        mMap.addMarker(new MarkerOptions().position(newCave).title("Cueva " + cave_number).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    /**
     * Spinner class
     */
    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        boolean selected; //Option selected status.

        /**
         * Invoked when an item in this view has been selected.
         *
         * @param parent AdapterView where the selection happened
         * @param view View within the AdapterView that was clicked
         * @param pos Position of the view in the adapter
         * @param id Iow id of the item that is selected
         */
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            selected = true;
            switch(pos){
                //Sets the distance between caves depending on ehich item was selected.
                case 0:
                    //Sets distance to 5 meters.
                    distance = 5;
                    break;
                case 1:
                    //Sets distance to 10 meters.
                    distance = 10;
                    break;
                case 2:
                    //Sets distance to 25 meters.
                    distance = 25;
                    break;
                case 3:
                    //Sets distance to 50 meters.
                    distance = 50;
                    break;
                case 4:
                    //Sets distance to 100 meters.
                    distance = 100;
                    break;
                default:
                    break;
            }
        }

        /**
         * Invoked when an item in this view has not been selected.
         *
         * @param parent AdapterView where the selection is missing.
         */
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            selected = false;
        }
    }
}




