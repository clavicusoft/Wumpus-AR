package com.clavicusoft.wumpus.Draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.clavicusoft.wumpus.Maze.Cave;
import com.clavicusoft.wumpus.R;

import java.util.ArrayList;

public class DrawCanvas extends View {

    private Path drawPath; //Saves de drawing path
    private Paint drawPaint, canvasPaint; //Drawing brush
    private Canvas drawCanvas; //Canvas
    private Bitmap canvasBitmap; //Stores canvas bit state
    private ArrayList<IntPair> relations; //Stores all current relations
    private ArrayList<Cave> caves; //Stores all current caves
    private float touchX, touchY, touchX2, touchY2; //Stores coordinates
    private int numCave; //Counter to assign an ID to each cave
    private int totalCaves; //Counter of drawn caves
    private int maxCaves; //Maximun number of caves allowed

    /**
     * Creates a canvas
     * @param context Context to access DrawCanvas class
     * @param attrs AttributeSet of the DrawCanvas configurations
     */
    public DrawCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    /**
     * Sets the configuration of the area to draw
     */
    public void setupDrawing(){
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(0xFFFFFFFF); //White
        drawPaint.setAntiAlias(true); //Soft brush
        drawPaint.setStrokeWidth(20); //Brush width
        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG); //Difuminated draw
        relations = new ArrayList<>();
        caves = new ArrayList<>();
        touchX = 0;
        touchY = 0;
        totalCaves = 0;
        numCave = 0;
        maxCaves = 20;
    }

    /**
     * Size given to the drawing area
     * @param w width of the area after change
     * @param h height of the area after change
     * @param oldw old width of the area changed
     * @param oldh old height of the area changed
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w,h,oldw,oldh);
        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(getResources().getColor(R.color.beige));
    }

    /**
     * Draws to the canvas, used from OnTouchEvent
     * @param drawCanvas receives the canvas to be affected by the drawing
     */
    @Override
    protected void onDraw(Canvas drawCanvas){
        drawCanvas.drawBitmap(canvasBitmap,0,0,canvasPaint); //Puts the drawing in memory in this format
        drawCanvas.drawPath(drawPath, drawPaint);
        invalidate();
    }

    /**
     * Registers the user´s touch events
     *
     * One touch where there isn't a cave -> add a new cave
     * One touch on two different caves -> if there is an arc delete it, else add an arc
     * Two touch on the same cave -> delete cave
     *
     * @param event The event of a touch in the canvas
     * @return Always returns true for a touch
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Cave c1,c2;
        int a;
        touchX = event.getX();
        touchY = event.getY();
        c1 = searchCaveByCoordinates(touchX, touchY);
        if (c1 != null) {
            if (touchX2 != 0 && touchY2 != 0) {
                c2 = searchCaveByCoordinates(touchX2,touchY2);
                if (c1.getId() != c2.getId()) {
                    a = searchArc(c1.getId(),c2.getId());
                    if (a < relations.size()){
                        deleteArc(c1.getId(), c2.getId());
                        touchX = 0;
                        touchY = 0;
                        touchX2 = 0;
                        touchY2 = 0;
                    } else {
                        addArc(c1.getId(),c2.getId(),c1.getCorX(),c1.getCorY(),c2.getCorX(),c2.getCorY());
                        touchX = 0;
                        touchY = 0;
                        touchX2 = 0;
                        touchY2 = 0;
                    }
                }
                else {
                    deleteCave(c1.getId());
                    touchX = 0;
                    touchY = 0;
                    touchX2 = 0;
                    touchY2 = 0;
                }
            } else {
                touchX2 = touchX;
                touchY2 = touchY;
                touchX = 0;
                touchY = 0;
            }
        }
        else
        {
            addCave(touchX, touchY);
            touchX = 0;
            touchY = 0;
            touchX2 = 0;
            touchY2 = 0;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Restarts the drawing
     */
    public void newDraw(){
        setupDrawing();
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    /**
     * Draws a circle with a number on the canvas in the indicated coordinates to represent a cave with the id given
     */
    public void drawCave(float x, float y, String id)
    {
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawPaint.setColor(getResources().getColor(R.color.pinkish));
        drawPath.addCircle(x, y, 50, Path.Direction.CW); //Draw the cave in these coordinates
        drawCanvas.drawPath(drawPath, drawPaint);
        drawPath.reset();
        drawPaint.setStrokeWidth(3);
        drawPaint.setTextSize(30);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setColor(getResources().getColor(R.color.white));
        drawCanvas.drawText(id, x - 5, y + 5, drawPaint); //Draw the cave ID
    }

    /**
     * Draws a line between two caves and redraws the cave's id
     * @param c1 First cave
     * @param c2 Second cave
     */
    public void drawArc(int c1, int c2, float x1, float y1, float x2, float y2){
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawPaint.setColor(getResources().getColor(R.color.pinkish));//White
        drawPath.moveTo(x1,y1);
        drawPath.lineTo(x2,y2);
        drawCanvas.drawPath(drawPath, drawPaint);
        drawPath.reset();
        drawPaint.setStrokeWidth(3);
        drawPaint.setTextSize(30);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setColor(getResources().getColor(R.color.white));
        drawCanvas.drawText(Integer.toString(c1), x1 - 5, y1 + 5, drawPaint);
        drawCanvas.drawText(Integer.toString(c2), x2 - 5, y2 + 5, drawPaint);
        invalidate();
    }

    /**
     * Adds a cave to the canvas and the array, using coordinates and giving it a unique ID
     */
    public void addCave(float x, float y){
        if(touchX > 0 && touchY > 0) {
            if (totalCaves < maxCaves) {
                caves.add(new Cave(numCave, x, y)); //Add cave to the list
                drawCave(x, y, Integer.toString(numCave));
                totalCaves++;
                numCave++;
                invalidate();
            }
        }
    }

    /**
     * Adds an arc to the canvas and the array, using the caves id and the coordinates given
     */
    public void addArc(int c1, int c2, float x1, float y1, float x2, float y2){
        drawArc(c1,c2,x1,y1,x2,y2);
        relations.add(new IntPair(c1,c2));
    }

    /**
     * Removes all arcs associated to the specified cave from the array
     * @param c cave's id
     */
    public void removeAssociatedArcs(int c)
    {
        int l = 0;
        IntPair pair;
        while(l < relations.size()) {
            pair = relations.get(l);
            if (pair.x == c || pair.y == c){
                relations.remove(l);
            }
            else{
                l++;
            }
        }
    }

    /**
     * Removes the cave from the array
     * @param c cave's id
     */
    public void removeCave(int c)
    {
        int j = 0;
        Boolean found = false;
        while (j < caves.size() && !found)
        {
            if (caves.get(j).getId() == c) {
                caves.remove(j);
                totalCaves--;
                found = true;
            }
            else {
                j++;
            }
        }
    }

    /**
     * Removes an arc between two caves from the array
     * @param c1 first cave's id
     * @param c2 second cave's id
     */
    public void removeArc(int c1, int c2)
    {
        int l = 0;
        IntPair pair;
        Boolean found = false;
        while(l < relations.size() && !found) {
            pair = relations.get(l);
            if ((pair.x == c1 && pair.y == c2) || (pair.y == c1 && pair.x == c2)){
                relations.remove(l);
                found = true;
            }
            else{
                l++;
            }
        }
    }

    /**
     * Redraws all the rest of caves and arcs to the canvas
     */
    public void redrawsCavesAndArcs() {
        Cave c1, c2;
        float x,y;
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        int i = 0;
        while(i < caves.size()) {
            c1 = caves.get(i);
            x = c1.getCorX();
            y = c1.getCorY();
            drawPath.moveTo(x, y);
            drawCave(x, y, Integer.toString(c1.getId()));
            i++;
        }
        int k = 0;
        while (k < relations.size()) {
            c1 = searchCaveById(relations.get(k).x);
            c2 = searchCaveById(relations.get(k).y);
            drawArc(c1.getId(),c2.getId(),c1.getCorX(),c1.getCorY(),c2.getCorX(),c2.getCorY());
            k++;
        }
        drawPath.reset();
        invalidate();
    }

    /**
     * Deletes a cave from the canvas and the array
     * @param c cave's id to delete
     */
    public void deleteCave(int c){
        removeAssociatedArcs(c);
        removeCave(c);
        redrawsCavesAndArcs();
    }

    /**
     * Deletes an arc between two caves from the canvas and the array
     * @param c1 First cave's id
     * @param c2 Second cave's id
     */
    public void deleteArc(int c1, int c2){
        removeArc(c1,c2);
        redrawsCavesAndArcs();
    }

    /**
     * Searches the cave with the specified id and returns it
     * @param id Cave identifier
     * @return Cave with the specified id
     */
    public Cave searchCaveById(int id){
        int i = 0;
        while(i < totalCaves){
            if (caves.get(i).getId() == id) {
                return caves.get(i);
            }
            else{
                i++;
            }
        }
        return null;
    }

    /**
     * Searches the cave based on the received coordinates and returns it
     * @param x Coordinate x of the touch event
     * @param y Coordinate y of the touch event
     * @return Cave with the corresponding coordinates
     */
    public Cave searchCaveByCoordinates(float x, float y){
        float currentX, currentY;
        int i = 0;
        while(i < caves.size()) {
            currentX = caves.get(i).getCorX();
            currentY = caves.get(i).getCorY();
            if ((x >= currentX-50 && x < currentX+50) && (y >= currentY-50 && y < currentY+50))
            {
                return caves.get(i);
            }
            else{
                i++;
            }
        }
        return null;
    }

    /**
     * Searches an arc based on the received caves' number and returns it
     * @param c1 first cave
     * @param c2 second cave
     * @return l the position of the arc in the relation's array
     */
    public int searchArc(int c1, int c2)
    {
        int l = 0;
        IntPair pair;
        while(l < relations.size()) {
            pair = relations.get(l);
            if ((pair.x == c1 && pair.y == c2) || (pair.y == c1 && pair.x == c2)){
                return l;
            }
            else{
                l++;
            }
        }
        return l;
    }

    /**
     * Returns the relations' array in the current graph
     * @return relations
     */
    public ArrayList<IntPair> getRelations() {
        return relations;
    }

    /**
     * Returns the array of created caves
     * @return caves
     */
    public ArrayList<Cave> getCaves() {
        return caves;
    }

    /**
     * Returns the total number of created caves
     * @return totalCaves
     */
    public int getTotalCaves() {
        return totalCaves;
    }
}