package com.clavicusoft.wumpus.AR;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.clavicusoft.wumpus.Database.UserDetails;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.clavicusoft.wumpus.Database.UserDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class GameDataBase {

    private String room_id;
    private String player_id;
    private Game_Multiplayer game_multiplayer;// Supongo que esto se puede usar para llamar a algun metodo al finalizar el juego
    private FirebaseDatabase db;


    public GameDataBase(String room_id, String player_id, final Game_Multiplayer game_multiplayer) {
        this.room_id = room_id;
        this.player_id = player_id;
        this.game_multiplayer = game_multiplayer;
        this.db = FirebaseDatabase.getInstance();

        insertPlayer();

        //Listener para oir datos ( status del jugador )

        DatabaseReference playerReference = db.getReference(this.room_id+ "/" + this.player_id + "/STATUS");

        playerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    game_multiplayer.showMessage();//TODO TERMINE JUEGO
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        //Listener para oir datos ( status del room )
        DatabaseReference roomReference = db.getReference(this.room_id+ "/STATUS");
        roomReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    game_multiplayer.showMessage();//TODO TERMINE JUEGO
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


        /**Ambos listeners oyen todos los cambios en la tabla, entonces si hay cambios en alguna de
         * las referencia, haga algo todo se mete a un mapa por que son varios datos, pero no estoy
         * seguro de que sirva al 100% **/
    }

    //TODO Poner metodos, al morir, matar, actualizar cueva, etc

    public boolean roomState() {

        return false;
    }

    public boolean playerState() {
        return false;
    }

    public void changePlayerCave(String cave){
        DatabaseReference myRef = db.getReference(this.room_id);
        myRef.child(this.player_id).child("CAVE").setValue(cave);
    }

    public void changePlayerStatus(String status) {
        DatabaseReference myRef = db.getReference(this.room_id);
        myRef.child(this.player_id).child("STATUS").setValue(status);

        if (status.equals("0")) {
            checkLastActivePlayer();
        }
    }

    private void checkLastActivePlayer () {
        String url = "https://wumpus-ar-7c538.firebaseio.com/" + room_id + ".json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError){
            }
        });

    }

    private  void doOnSuccess(String s) {
        ArrayList<UserDetails> users = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(s);
            Iterator i = obj.keys();
            String key = "";
            while (i.hasNext()) {
                key = i.next().toString();
                users.add(new UserDetails(key, obj.getJSONObject(key).getString("CAVE"), obj.getJSONObject(key).getString("STATUS")));
            }
        }catch (JSONException e) {

        }
        checkPlayersStatus(users);
    }

    private void checkPlayersStatus(ArrayList<UserDetails> users) {
        boolean active = false;
        Integer i = 0;
        while (!active && i < users.size() - 1){
            if (users.get(i).getStatus().equals("1")) {
                active = true;
            }
            ++i;
        }

    }

    private void insertPlayer(){
        DatabaseReference myRef = db.getReference(room_id);
        myRef.child(this.player_id).setValue("USERNAME");
        myRef.child(this.player_id).child("STATUS").setValue("1");
        myRef.child(this.player_id).child("CAVE").setValue("1");
    }

}
