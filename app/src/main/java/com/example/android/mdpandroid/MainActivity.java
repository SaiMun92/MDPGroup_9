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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -------------------------------Robot & Maze-------------------------
        gridView = (GridView) findViewById(R.id.gridView1);
        robot[0] = 0;
        robot[1] = 1;
        robot[2] = 2;
        robot[3] = 20;
        robot[4] = 21;
        robot[5] = 22;
        robot[6] = 40;
        robot[7] = 41;
        robot[8] = 42;
        int x = 0;
        for(int i=0; i<arena_maze.length; i++) {
            if(x<9 && i==robot[x]) {
                if( x != 5)
                    arena_maze[i] = 1;
                else
                    arena_maze[i] = 2;
                x++;
            }else {
                arena_maze[i] = 0;
            }
        }
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


    //Grid initialisation
    GridView gridView;
    int robot [] = new int[9];
    static final int[] arena_maze = new int [300];
    String robotpos = "UP";



}
