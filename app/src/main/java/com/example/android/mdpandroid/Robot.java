package com.example.android.mdpandroid;

/**
 * Created by SaiMunLee on 16/2/16.
 */
public class Robot {
    private int[][] position;

    public Robot() {

    }

    public Robot(int[][] position){
        position[0][0] = 0;
        position[0][1] = 0;
        position[0][2] = 0;
        position[1][0] = 0;
        position[1][1] = 0;
        position[1][2] = 0;
        position[2][0] = 0;
        position[2][1] = 0;
        position[2][2] = 0;
    }

    public void setPosition(int [][]position){
        this.position = position;
    }

    public int [][] getPosition (){
        return position;
    }
}
