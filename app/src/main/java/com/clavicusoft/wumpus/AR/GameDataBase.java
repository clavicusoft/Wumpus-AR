package com.clavicusoft.wumpus.AR;


import com.google.android.gms.games.Game;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameDataBase extends Thread {

    private String room_id;
    private String player_id;
    private Game_Multiplayer game_multiplayer;// Supongo que esto se puede usar para llamar a algun metodo al finalizar el juego
    
    public GameDataBase(String room_id, String player_id, Game_Multiplayer game_multiplayer) {
        this.room_id = room_id;
        this.player_id = player_id;
        this.game_multiplayer = game_multiplayer;
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
        myRef.child("status").setValue("1");//up
        myRef.child("DATETIME").child("CAVEID").setValue("1");
        myRef.child("DATETIME").child("STATUS").setValue("1");//ALIVE


    }
}
