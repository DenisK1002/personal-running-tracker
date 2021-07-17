package com.android.app.runnable_activitytracker.db;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.util.List;

@Entity (tableName = "track_table")
@TypeConverters(Converter.class)
public class Track {

    @PrimaryKey (autoGenerate = true)
    private int id;

    private String trackName;

    private Bitmap image;

    private int distance_meters;

    private Float average_speed;

    private Long run_pace;

    private Long date;

    private Long run_duration;

    public Track(String trackName, Bitmap image, int distance_meters, Float average_speed, Long run_pace, Long date, Long run_duration) {
        this.trackName = trackName;
        this.image = image;
        this.distance_meters = distance_meters;
        this.average_speed = average_speed;
        this.run_pace = run_pace;
        this.date = date;
        this.run_duration = run_duration;
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTrackName() {
        return trackName;
    }

    public Bitmap getImage() {
        return image;
    }

    public int getDistance_meters() {
        return distance_meters;
    }

    public Float getAverage_speed() {
        return average_speed;
    }

    public Long getDate() {
        return date;
    }

    public Long getRun_pace() {
        return run_pace;
    }

    public Long getRun_duration() {
        return run_duration;
    }
}
