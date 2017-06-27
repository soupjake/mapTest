package com.asuper.maptest;

import android.graphics.Color;

/**
 * Created by super on 27/06/2017.
 */

public class TemperatureColors {

    //Color array for temperature overlay
    final static int[] COLORS = {
            //50+
            Color.argb(150, 255, 0, 0),
            //45+
            Color.argb(150, 255, 51, 51),
            //40+
            Color.argb(150, 255, 102, 102),
            //35+
            Color.argb(150, 255, 128, 0),
            //30+
            Color.argb(150, 255, 153, 51),
            //25+
            Color.argb(150, 255, 178, 102),
            //20+
            Color.argb(150, 255, 255, 0),
            //15+
            Color.argb(150, 255, 255, 102),
            //10+
            Color.argb(150, 255, 255, 153),
            //5+
            Color.argb(150, 255, 255, 204),
            //0+
            Color.argb(150, 204, 255, 255),
            //-0+
            Color.argb(150, 153, 255, 255),
            //-5+
            Color.argb(150, 102, 255, 255),
            //-10+
            Color.argb(150, 51, 255, 255),
            //-15+
            Color.argb(150, 102, 178, 255),
            //-20+
            Color.argb(150, 51, 153, 255),
            //-30+
            Color.argb(150, 0, 128, 255),
            //-40+
            Color.argb(150, 51, 51, 255),
            //-50+
            Color.argb(150, 0, 0, 255),
    };

    public static int getTemperatureColor(int temperatureColor){

        if(temperatureColor >= 50){
            temperatureColor = COLORS[0];
        } else if(temperatureColor >= 45){
            temperatureColor = COLORS[1];
        } else if(temperatureColor >= 40){
            temperatureColor = COLORS[2];
        } else if(temperatureColor >= 35){
            temperatureColor = COLORS[3];
        } else if(temperatureColor >= 30){
            temperatureColor = COLORS[4];
        } else if(temperatureColor >= 25){
            temperatureColor = COLORS[5];
        } else if(temperatureColor >= 20){
            temperatureColor = COLORS[6];
        } else if(temperatureColor >= 15){
            temperatureColor = COLORS[7];
        } else if(temperatureColor >= 10){
            temperatureColor = COLORS[8];
        } else if(temperatureColor >= 5){
            temperatureColor = COLORS[9];
        } else if(temperatureColor >= 0){
            temperatureColor = COLORS[10];
        } else if(temperatureColor <= 0){
            temperatureColor = COLORS[11];
        } else if(temperatureColor <= -5){
            temperatureColor = COLORS[12];
        } else if(temperatureColor <= -10){
            temperatureColor = COLORS[13];
        } else if(temperatureColor <= -15){
            temperatureColor = COLORS[14];
        } else if(temperatureColor <= -20){
            temperatureColor = COLORS[15];
        } else if(temperatureColor <= -30){
            temperatureColor = COLORS[16];
        } else if(temperatureColor <= -40){
            temperatureColor = COLORS[17];
        } else if(temperatureColor <= -50){
            temperatureColor = COLORS[18];
        }
        return temperatureColor;
    }
}
