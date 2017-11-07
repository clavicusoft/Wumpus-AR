package com.clavicusoft.wumpus.Draw;

import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.clavicusoft.wumpus.Database.AdminSQLite;
import com.clavicusoft.wumpus.Maze.Graph;
import com.clavicusoft.wumpus.R;

public class DrawMazeActivity extends Activity {

    private DrawCanvas myCanvas; //Instance of the canvas
    private Graph customMaze; //Object graph used to store the maze
    private String name; //Name of the created maze
    AlertDialog.Builder alert; //Dialog used to show important information


    /**
     * Creates a draw  Activity, creates the canvas.
     * @param savedInstanceState Activity's previous saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_draw);
        myCanvas = findViewById(R.id.viewDrawCanvas);
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Instrucciones");
        alert.setMessage("- Para agregar una cueva debe presionar la pantalla donde desea colocarla.\n\n- Para eliminar una cueva debe presionar la pantalla dos veces donde esta se encuentra, esto eliminará a su vez los caminos conectados a la cueva.\n\n- Para agregar o eliminar un camino entre dos cuevas, presione donde se encuentra la primera cueva y luego donde se encuentra la segunda.\n\n- Una vez finalizado el dibujo presione el botón \"Guardar Dibujo\" lo que almacenará el laberinto en la biblioteca y permitirá utilizarlo para jugar.");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /**
     * Shows information on how to create a cave maze
     * @param v View to be shown
     */
    public void info (View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Instrucciones");
        alert.setMessage("- Para agregar una cueva debe presionar la pantalla donde desea colocarla.\n\n- Para eliminar una cueva debe presionar la pantalla dos veces donde esta se encuentra, esto eliminará a su vez los caminos conectados a la cueva.\n\n- Para agregar o eliminar un camino entre dos cuevas, presione donde se encuentra la primera cueva y luego donde se encuentra la segunda.\n\n- Una vez finalizado el dibujo presione el botón \"Guardar Dibujo\" lo que almacenará el laberinto en la biblioteca y permitirá utilizarlo para jugar.");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /**
     * Restarts the drawing
     * @param v View to be affected
     */
    public void newD(View v){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Reiniciar laberinto");
        newDialog.setMessage("¿Está seguro que desea comenzar un nuevo dibujo? Perderá el progreso actual.");
        newDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                myCanvas.newDraw();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    /**
     * Checks that the current maze is valid
     * @param v View to be affected
     */
    public void checkD(View v){
        customMaze = new Graph(myCanvas.getTotalCaves(), myCanvas.getCaves());
        customMaze.fillGraph(myCanvas.getRelations());
        if (customMaze.valid()) {
            askMazeName();
        }
        else {
            alert.setTitle("Error");
            alert.setMessage("No se puede capturar al Wumpus en este dibujo creado, porfavor verifique que las siguientes restricciones se cumplan:\n\n-Deben haber al menos 2 cuevas.\n\n- No pueden existir cuevas aisladas, es decir, se puede llegar de una cueva a cualquier otra a través de uno o varios caminos.");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    /**
    * Saves the actual maze in the DB
    */
    public void saveMaze() {
        String relations = customMaze.arrayToString();

        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = "-" + tsLong.toString();

        ContentValues data = new ContentValues();
        data.put("name", name + ts);
        data.put("relations", relations);
        data.put("number_of_caves", customMaze.getMaximumCaves());
        db.insert("GRAPH", null, data);

        Cursor cell = db.rawQuery("SELECT id FROM GRAPH WHERE GRAPH.relations = \"" + relations + "\"", null);
        if (cell.moveToFirst()) {
            final String graphID = cell.getString(0);
            cell.close();
            db.close();
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("El dibujo ha sido guardado");
            newDialog.setMessage("¿Desea iniciar una partida con este laberinto?");
            newDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                    startGame(graphID);
                }
            });
            newDialog.setNegativeButton("No", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    myCanvas.newDraw();
                    dialog.dismiss();
                }
            });
            newDialog.show();
        } else {
            alert.setTitle("Error");
            alert.setMessage("Hubo un problema guardando el laberinto.");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        cell.close();
        db.close();
    }

    /**
     * Asks the name of the maze created
     */
    public void askMazeName () {
        final Dialog dialogAddArc = new Dialog(this);
        dialogAddArc.setContentView(R.layout.layout_maze_name);
        final EditText edtTxtName = dialogAddArc.findViewById(R.id.editTxtNameMaze);
        Button btnAccept = dialogAddArc.findViewById(R.id.btnAcceptName);
        Button btnCancel = dialogAddArc.findViewById(R.id.btnCancelName);
        btnAccept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!edtTxtName.getText().toString().equals("")){
                    name =  edtTxtName.getText().toString();
                    dialogAddArc.dismiss();
                    saveMaze();
                }
                else {
                    alert.setTitle("Error");
                    alert.setMessage("Debe introducir un nombre para el dibujo");
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialogAddArc.cancel();
            }
        });
        dialogAddArc.show();
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
     * Starts the game with the ID identifying the created maze
     * @param stringGraphID String used to identify the created maze
     */
    public void startGame (String stringGraphID) {
        Intent i = new Intent(this, com.clavicusoft.wumpus.Map.Coordinates.class);
        i.putExtra("graphID",stringGraphID);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out);
        startActivity(i, options.toBundle());
    }

}