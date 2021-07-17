package com.android.app.runnable_activitytracker.weather;

import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Weather {

    private int day;
    private String temp;
    private String iconID;
    private String description;

    public Weather(int day, String temp, String iconID, String description) {
        this.day = day;
        this.temp = temp;
        this.iconID = iconID;
        this.description = description;
    }

    public Weather(int day, String temp, String iconID) {
        this.day = day;
        this.temp = temp;
        this.iconID = iconID;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String dtToDay(int dt) {
        String date = String.valueOf(Instant.ofEpochSecond( dt ));
        String day = String.valueOf(date.charAt(8)) + String.valueOf(date.charAt(9));
        String month = String.valueOf(date.charAt(5)) + String.valueOf(date.charAt(6));

        String now = String.valueOf(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
        String today = String.valueOf(now.charAt(8)) + String.valueOf(now.charAt(9));
        int tomorrow = Integer.parseInt(today) + 1;

        if (Integer.parseInt(day) == tomorrow) {
            return "Tomorrow";
        }

        return day + "." + month;
    }

    public String getDay() {
        return dtToDay(day);
    }

    public String getTemp() {
        return temp;
    }

    public String getIconID() {
        return iconID;
    }

    public String getDescription() {
        return description;
    }
}
