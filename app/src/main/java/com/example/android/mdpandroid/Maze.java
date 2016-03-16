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
public class Maze extends BaseAdapter {
    private Context context;
    private final int[] mazeValues;

    //Robot robotView = setupRobot();

    public Maze(Context context, int[] mazeValues) {
        this.context = context;
        this.mazeValues = mazeValues;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            // get layout from maze.xml
            gridView = inflater.inflate(R.layout.maze, null);

            // set value into textview
            TextView textView = (TextView) gridView
                    .findViewById(R.id.grid_item_label);

            //textView.setText("");
            textView.setText(Integer.toString(position));

            int grid = mazeValues[position];

            if (grid == 0) {
                textView.setBackgroundColor(Color.DKGRAY); // maze
            } else if (grid == 1) {
                textView.setBackgroundColor(Color.parseColor("#FE9A2E")); //robotView - bright orange
            } else if (grid == 2) {
                textView.setBackgroundColor(Color.WHITE); // robotView head
            } else if (grid == 3){
                textView.setBackgroundColor(Color.parseColor("#FFFF00")); //obstacles   yelllow color
            } else if (grid == 4) {
                textView.setBackgroundColor(Color.RED); //unexplored
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

