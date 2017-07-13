package com.asuper.maptest;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.animation.AlphaAnimation;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by super on 27/06/2017.
 * Here are the methods that I use for formatting data
 */

public class Format {

    //Method for capitalising each word in string
    public static String stringCapitalise(String str) {
        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        str = builder.toString();
        return str;
    }

    //Method for formatting date
    public static String formatDate(String date){

        //Get time, month and date substrings
        String time = date.substring(11, 13);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        //Change time to friendly format
        int timeInt = Integer.parseInt(time);

        if (timeInt == 0) {
            timeInt += 12;
            time = (timeInt + "am");
        } else if (timeInt < 12) {
            time = (timeInt + "am");
        } else if (timeInt == 12) {
            time = (timeInt + "pm");
        } else {
            timeInt -= 12;
            time = (timeInt + "pm");
        }

        //Combine for rearranged date UK format
        String formattedDate = new String(time + " " + day + "/" + month);

        return formattedDate;
    }

    //Method for rounding precipitation volumes to 3 decimal places
    public static double roundVolume(double volume){

        //Code from: https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
        long factor = (long)Math.pow(10, 3);
        volume *= factor;
        long temp = Math.round(volume);
        return (double) temp/factor;

    }

    //Method for converting dp to pixel
    public static int dpToPx(int dp){
        float floatPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
        return Math.round(floatPx);
    }

    //Method used for creating fading in or out animation
    public static AlphaAnimation fadeAnimation(int fadeIn, int fadeOut){
        AlphaAnimation animation = new AlphaAnimation(fadeIn, fadeOut);
        animation.setDuration(500);
        return animation;
    }



}
