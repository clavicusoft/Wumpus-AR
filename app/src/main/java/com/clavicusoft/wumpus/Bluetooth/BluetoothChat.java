package com.clavicusoft.wumpus.Bluetooth;

import android.app.Activity;
import android.app.ActivityOptions;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clavicusoft.wumpus.AR.Game_Multiplayer;
import com.clavicusoft.wumpus.Database.AdminSQLite;
import com.clavicusoft.wumpus.Map.MapsActivity;
import com.clavicusoft.wumpus.R;
import com.clavicusoft.wumpus.Select.SelectPolyActivity;

public class BluetoothChat extends Activity {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "";
    public static final String TOAST = "";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public String laberinto = "";
    public String msj ="";
    public String nombreLaberinto = "";
    public String funcion = "";
    private Button mSendButton;
    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;

    private String roomName = "";
    private String username = "";
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    String readMessage="";

    Button btnSend;
    Button btnVisibility;
    Button btnFinish;

    Boolean sending;
    Boolean multiplayer;

    String numberCaves;
    String game_id;

    /**
     * On create of the  Activity, creates the Bluetooth Chat.
     * @param savedInstanceState Activity's previous saved state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        funcion = getIntent().getStringExtra("funcion");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(funcion.equals("enviar")){
            sending = true;
            multiplayer = false;
            setContentView(R.layout.multiplayer_send_menu);
            laberinto = getIntent().getStringExtra("laberinto");
            nombreLaberinto = getIntent().getStringExtra("nombreLaberinto");
        }else if(funcion.equals("enviarEmplazamiento")){
            sending = true;
            multiplayer = true;
            setContentView(R.layout.layout_multiplayer_send);
            msj = getIntent().getStringExtra("data");
            Long tsLong = System.currentTimeMillis()/1000;
            roomName = mBluetoothAdapter.getName() + "-" + tsLong.toString();
            username = tsLong.toString();
            game_id = getIntent().getStringExtra("gameID");
            numberCaves = getIntent().getStringExtra("number_of_caves");

        }else{
            sending = false;
            setContentView(R.layout.multiplayer_menu);
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        }
        startButtons();
    }

    /**
     * Starts the buttons listeners depending on the function you are using.
     */
    public void startButtons () {

        if (sending) {
            if (multiplayer) {
                btnSend = (Button) findViewById(R.id.btSendData);
                btnVisibility = (Button) findViewById(R.id.btSelectDevice);
                btnFinish = (Button) findViewById(R.id.btStart);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setupChat(view);
                    }
                });
                btnVisibility.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        connect(view);
                    }
                });
                btnFinish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMultiplayer();
                    }
                });
            }
            else {
                btnSend = (Button) findViewById(R.id.btSend);
                btnVisibility = (Button) findViewById(R.id.btDevice);
                btnFinish = (Button) findViewById(R.id.btFinish);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setupChat(view);
                    }
                });
                btnVisibility.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        connect(view);
                    }
                });
                btnFinish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }
        }
        else {
            btnSend = (Button) findViewById(R.id.btLabs);
            btnVisibility = (Button) findViewById(R.id.btVisibility);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    send(view);
                }
            });
            btnVisibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    discoverable(view);
                }
            });
        }
    }

    /**
     * Starts the send activity, and sets the animation for the transition.
     *
     * @param view current view.
     */
    public void send(View view){
        Intent i = new Intent(this, SelectLabToShare.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                R.anim.fade_out);
        startActivity(i, options.toBundle());
    }

    /**
     * On start of the  Activity, enables bluetooth.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null){
                mChatService = new BluetoothChatService(this, mHandler);
                mOutStringBuffer = new StringBuffer("");
            }
        }
    }

    /**
     * On Resume of the  Activity, starts BluetoothChatService.
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    /**
     * Recognize if the button send is pressed and send the lab.
     */
    public void setupChat(View view) {
        if(funcion.equals("enviar")){
            String message = laberinto;
            sendMessage(message);
        }
        if(funcion.equals("enviarEmplazamiento")){
            String message = msj;
            sendMessage(message);
        }
    }

    /**
     * On Pause of the  Activity, Override.
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    /**
     * On Stop of the  Activity, Override.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * On Destroy of the  Activity, stops BluetoothChatService.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null){
            mChatService.stop();
        }
    }

    /**
     * Set the device in discoverable mode.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Take the message and send it to mChatService.write.
     * @param message Message to send.
     */
    private void sendMessage(String message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {

        /**
         * If the action is a key-up event on the return key, send the message.
         * @param view View to be shown.
         * @param actionId action's id.
         * @param event event received.
         * @return True if sends message.
         */
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {

        /**
         * According to msgId, this method identifies what to do.
         * @param msg message received
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    readMessage = new String(readBuf, 0, msg.arg1);
                    final String[] splitMessage = tokenizer(readMessage);
                    if (splitMessage.length == 3) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(BluetoothChat.this);
                        alert.setTitle("Invitación para compartir laberinto");
                        alert.setMessage("¿Quiere aceptar el laberinto recibido?\nNombre: " + splitMessage[2] + "\nRelaciones: " + splitMessage[0] + "\nNúmero de cuevas: " + splitMessage[1]);
                        alert.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AdminSQLite admin = new AdminSQLite(BluetoothChat.this, "WumpusDB", null, 6);
                                SQLiteDatabase db = admin.getWritableDatabase();
                                ContentValues data = new ContentValues();
                                data.put("name", splitMessage[2]);
                                data.put("relations", splitMessage[0]);
                                data.put("number_of_caves", splitMessage[1]);
                                db.insert("GRAPH", null, data);
                                db.close();
                                AlertDialog.Builder newDialog = new AlertDialog.Builder(BluetoothChat.this);
                                newDialog.setTitle("Se ha guardado el laberinto");
                                newDialog.setMessage("¿Desea continuar intercambiando laberintos?");
                                newDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                newDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent i = new Intent(BluetoothChat.this, SelectPolyActivity.class);
                                        ActivityOptions options = ActivityOptions.makeCustomAnimation(BluetoothChat.this, R.anim.slide_in_up, R.anim.slide_out_up);
                                        startActivity(i, options.toBundle());
                                    }
                                });
                                newDialog.show();
                                dialog.dismiss();
                            }
                        });
                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                    }else{
                        AlertDialog.Builder alert = new AlertDialog.Builder(BluetoothChat.this);
                        alert.setTitle("Invitación para compartir emplazamiento");
                        alert.setMessage("¿Quiere aceptar el emplazamiento recibido?");
                        alert.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(BluetoothChat.this, MapsActivity.class);
                                i.putExtra("tipo","multijugador");
                                i.putExtra("data",readMessage);
                                ActivityOptions options = ActivityOptions.makeCustomAnimation(BluetoothChat.this, R.anim.fade_in, R.anim.fade_out);
                                startActivity(i, options.toBundle());
                            }
                        });
                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Conectado a " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    /**
     * Connects to another device and validates the bluetooth's state.
     * @param requestCode function to do.
     * @param resultCode result of the function's invocation.
     * @param data data received.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    mChatService = new BluetoothChatService(this, mHandler);
                    mOutStringBuffer = new StringBuffer("");
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * Starts activity DeviceListActivity.
     * @param v View to be shown.
     */
    public void connect(View v) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /**
     * Call to ensureDiscoverable method.
     * @param v View to be shown.
     */
    public void discoverable(View v) {
        ensureDiscoverable();
    }

    /**
     * This method split the message and interprets the information.
     * @param msj msg received
     * @return the information interpreted
     */
    public String[] tokenizer(String msj){
        String[] mensaje = msj.split("%");
        return mensaje;
    }

    /**
     * Sets the animation for the onBackPressed function.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    /**
     * Gets the ID of a graph based on it's name.
     *
     * @param graphName The name of the graph.
     * @return The DB ID of the graph
     */
    public int getGraphID(String graphName) {
        int graphID = 0;
        AdminSQLite admin = new AdminSQLite(BluetoothChat.this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor cell = db.rawQuery("SELECT GRAPH.id FROM GRAPH WHERE GRAPH.name = \"" + graphName +"\";", null);
        if (cell.moveToFirst()){
            graphID = cell.getInt(0);
            cell.close();
            return graphID;
        }
        else {
            Toast.makeText(this, "The Wumpus isn't around this caves. Try another one!", Toast.LENGTH_LONG).show();
            db.close();
        }
        cell.close();
        return graphID;
    }

    public void startMultiplayer () {
        Intent i = new Intent(this, Game_Multiplayer.class);
        i.putExtra("game_ID", game_id);
        i.putExtra("number_of_caves", numberCaves);
        i.putExtra("room", roomName);
        i.putExtra("username", username);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                R.anim.fade_out);
        startActivity(i, options.toBundle());
    }

}