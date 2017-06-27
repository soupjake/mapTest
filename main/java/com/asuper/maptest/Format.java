package com.asuper.maptest;

import android.view.animation.AlphaAnimation;

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

        //First check if date is "present" so to skip it
        if(date.equals("Present")){
            return date;
        } else {

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
    }

    //Method used for creating fading in or out animation
    public static AlphaAnimation fadeAnimation(int fadeIn, int fadeOut){
        AlphaAnimation animation = new AlphaAnimation(fadeIn, fadeOut);
        animation.setDuration(500);
        return animation;
    }
}
