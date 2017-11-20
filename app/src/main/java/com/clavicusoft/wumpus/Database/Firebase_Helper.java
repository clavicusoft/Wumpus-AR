package com.clavicusoft.wumpus.Database;



import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.clavicusoft.wumpus.AR.Game_Multiplayer;
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


public class Firebase_Helper {

    private String room_id;
    private String player_id;
    private Game_Multiplayer game_multiplayer;
    private FirebaseDatabase db;
    Boolean active;
    private Boolean killed_By_Player;

    /**
     * Starts a new Firebase DB for the game.
     * @param room_id ID if the room in the DB.
     * @param player_id ID of the player in the DB.
     * @param game_multiplayer Current Game context.
     */
    public Firebase_Helper(String room_id, String player_id, final Game_Multiplayer game_multiplayer) {
        this.room_id = room_id;
        this.player_id = player_id;
        this.game_multiplayer = game_multiplayer;
        this.db = FirebaseDatabase.getInstance();
        this.killed_By_Player = true;
        this.active = true;

        insertPlayer();
        startPlayerListener();
        startRoomListener();
    }

    public void startPlayerListener () {
        DatabaseReference playerReference = db.getReference(this.room_id+ "/" + this.player_id + "/STATUS");

        playerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    if (killed_By_Player) {
                        game_multiplayer.manageGettingKilled();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    public void startRoomListener () {
        DatabaseReference roomReference = db.getReference(this.room_id+ "/STATUS");
        roomReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    if (active) {
                        game_multiplayer.finishGame();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    /**
     * Changes the DB player current cave.
     * @param cave New current cave.
     */
    public void changePlayerCave(String cave){
        DatabaseReference myRef = db.getReference(this.room_id);
        myRef.child(this.player_id).child("CAVE").setValue(cave);
    }

    /**
     * Changes the DB player status.
     * @param status New status.
     */
    public void changePlayerStatus(String status) {
        DatabaseReference myRef = db.getReference(this.room_id);
        myRef.child(this.player_id).child("STATUS").setValue(status);

        if (status.equals("0")) {
            this.killed_By_Player = false;
            this.active = false;
            checkLastActivePlayer();
        }
    }

    /**
     * Check if the player is the last one standing.
     */
    private void checkLastActivePlayer () {
        String url = "https://wumpus-ar-7c538.firebaseio.com/" + room_id + ".json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String request) {
                doOnSuccess(request, false, 0);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError){
            }
        });
        Volley.newRequestQueue(game_multiplayer).add(request);
    }

    /**
     * Check if the player is the last one standing.
     */
    public void shootArrowMultiplayer(final int finalCave) {
        String url = "https://wumpus-ar-7c538.firebaseio.com/" + room_id + ".json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String request) {
                doOnSuccess(request, true, finalCave);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError){
            }
        });
        Volley.newRequestQueue(game_multiplayer).add(request);
    }

    /**
     * Gets all the users information.
     * @param request StringRequest with the DB request information.
     */
    private  void doOnSuccess(String request, Boolean arrow_Request, int caveArrow) {
        ArrayList<UserDetails> users = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(request);
            Iterator i = obj.keys();
            String key = "";
            while (i.hasNext()) {
                key = i.next().toString();
                if (!key.equals("STATUS")) {
                    users.add(new UserDetails(key, obj.getJSONObject(key).getString("CAVE"),
                            obj.getJSONObject(key).getString("STATUS")));
                }
            }
        }catch (JSONException e) {
            //DO NOTHING
        }
        if (arrow_Request) {
            killPlayers(users, caveArrow);
        }
        else {
            checkPlayersStatus(users);
        }
    }


    private void killPlayers(ArrayList<UserDetails> users, int caveArrow) {
        //Si el jugador tira una flecha, manejar el evento por si mata a algun jugador.
        UserDetails user;
        DatabaseReference myRef = db.getReference(room_id);
        Iterator<UserDetails> usersIterator = users.iterator();
        while(usersIterator.hasNext()){
            user = usersIterator.next();
            int cave = Integer.parseInt(user.getCave());
            String targetPlayerId = user.getUserName();
            if(cave == caveArrow){
                myRef.child(targetPlayerId).child("STATUS").setValue("0");
                game_multiplayer.manageKillPlayer();
            }
        }
    }

    /**
     *
     * @param users ArrayList with all the users.
     */
    private void checkPlayersStatus(ArrayList<UserDetails> users) {
        boolean active = false;
        /*Integer i = 0;
        while (!active && i < users.size() - 1){
            if (users.get(i).getStatus().equals("1")) {
                active = true;
            }
            ++i;
        }*/
        UserDetails user;
        Iterator<UserDetails> usersIterator = users.iterator();
        while(usersIterator.hasNext()){
            user = usersIterator.next();
            String playerStatus = user.getStatus();
            if(playerStatus.equals("1")){
                active = true;
            }
        }
        if (!active){
            finishGame();
        }
    }

    /**
     * Sets the room status to 0.
     */
    private void finishGame() {
        DatabaseReference myRef = db.getReference(room_id);
        myRef.child("STATUS").setValue("0");
        //TODO: STOP THE LISTENERS.
        //TODO: REMOVE THE ROOM FROM THE DB.
    }

    /**
     * Inserts a player into the database.
     */
    private void insertPlayer(){
        DatabaseReference myRef = db.getReference(room_id);
        myRef.child(this.player_id).setValue("USERNAME");
        myRef.child(this.player_id).child("STATUS").setValue("1");
        myRef.child(this.player_id).child("CAVE").setValue("1");
    }

    /**
     * Sets the room status to 0 and declares the player as the winner.
     */
    public void winGame () {
        active = false;
        finishGame();
    }

}
