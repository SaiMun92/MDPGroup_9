package com.example.android.mdpandroid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    //Grid initialisation
    GridView gridView;
    int robot [] = new int[9];
    static final int[] arena_maze = new int [300];  //create an array of 300 spaces
    String robotpos = "UP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //oncreate method is to save the data in case u log out and u want to log in back the app again.
        setContentView(R.layout.activity_main);

        // -------------------------------Robot & Maze-------------------------
        gridView = (GridView) findViewById(R.id.gridView1); //save the gridview1 into gridView.
        robot[0] = 0;   //mapping the robot to the gridview
        robot[1] = 1;
        robot[2] = 2;
        robot[3] = 20;
        robot[4] = 21;
        robot[5] = 22;
        robot[6] = 40;
        robot[7] = 41;
        robot[8] = 42;
        int x = 0;
        for(int i=0; i<arena_maze.length; i++) {    //populate the arena_maze array with data.
            if(x<9 && i==robot[x]) {                //study this again
                if( x != 5)                         //x=0 i=0 i=robot[0]=0 arena_maze[0]=1
                    arena_maze[i] = 1;              //x=1 i=1 i=robot[1]=1 arena_maze[1]=1
                else                                //x=2 i=2 i=robot[2]=2 arena_maze[2]=1
                    arena_maze[i] = 2;              //x=3 i=3 i!=robot[3]=20 arena_maze[3]=0
                x++;                                //x=3 i=4 i!=robot[3]=20 arena_maze[4]=0
            }else {                                 //x=3 i=20 i=robot[3]=20 arena_maze[20]=1   u get the point.
                arena_maze[i] = 0;                  //0 represents the middle of the robot.
            }                                       //1 represents the dark grey areas of the robot
        }                                           //2 represents the white part, or the front of the robot.
        gridView.setAdapter(new Maze(this, arena_maze));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }






}
