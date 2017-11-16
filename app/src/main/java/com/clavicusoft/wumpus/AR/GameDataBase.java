package com.clavicusoft.wumpus.AR;


import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.games.Game;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class GameDataBase extends Thread {

    private String room_id;
    private String player_id;
    private Game_Multiplayer game_multiplayer;// Supongo que esto se puede usar para llamar a algun metodo al finalizar el juego

    public GameDataBase(String room_id, String player_id, Game_Multiplayer game_multiplayer) {
        this.room_id = room_id;
        this.player_id = player_id;
        this.game_multiplayer = game_multiplayer;

        //Listener para oir datos ( status del jugador )

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference playerReference = database.getReference(this.player_id + "/STATUS");

        playerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    //TODO TERMINE JUEGO
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        //Listener para oir datos ( status del room )
        DatabaseReference roomReference = database.getReference(this.room_id+ "/STATUS");
        roomReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    //TODO TERMINE JUEGO
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

    @Override
    public void run() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("DATETIME + BLUETOOTHNAME");
        myRef.child("STATUS").setValue("1");//up
        myRef.child("DATETIME").child("CAVEID").setValue("1");
        myRef.child("DATETIME").child("STATUS").setValue("1");//ALIVE


    }
}
