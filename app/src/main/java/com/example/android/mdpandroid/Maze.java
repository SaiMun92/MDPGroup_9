package com.example.android.mdpandroid;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by SaiMunLee on 5/2/16.
 */
public class Maze extends BaseAdapter { //BaseAdapter is used to bind data to a view
    private Context context;
    private final int[] mazeValues; //Declare the mazeValues array

    //Robot robot = setupRobot();

    public Maze(Context context, int[] mazeValues) {
        this.context = context;         //the context of the current state of the application
        this.mazeValues = mazeValues;   //mazeValues = arena_maze, passed in from MainActivity.java
    }                                   //gridView.setAdapter(new Maze(this, arena_maze));

    public View getView(int position, View convertView, ViewGroup parent) {     //its ised to populate the grid with the smaller cubes

        LayoutInflater inflater = (LayoutInflater) context      //the layoutInflater takes your layout xml files and create different view objects from its contents
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE); //LayoutInflater is used to get the View Object which you define in layout xml.
                                                                    //its the same as getLayoutInflater()
        View gridView;

        if (convertView == null) {  //convertView is used to recycle view if they are not displayed.

            gridView = new View(context);

            // get layout from maze.xml
            gridView = inflater.inflate(R.layout.maze, null);

            // set value into textview
            TextView textView = (TextView) gridView
                    .findViewById(R.id.grid_item_label);

            //textView.setText("");
            textView.setText(Integer.toString(position));       //numbering of the cubes

            int grid = mazeValues[position];

            if (grid==0) {
                textView.setBackgroundColor(Color.DKGRAY);  //refer to the arena_maze[] in MainActivity
            } else if (grid==1) {
                textView.setBackgroundColor(Color.parseColor("#FE9A2E"));   //orange color
            } else if (grid==2) {
                textView.setBackgroundColor(Color.WHITE);   //front of the robot
            } else {

            }

            //int mobile = mobileValues[position];

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
        public int getCount() {
        return mazeValues.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}


