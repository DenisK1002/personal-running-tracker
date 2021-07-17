package com.android.app.runnable_activitytracker;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class TrackingDataFormatter {

    public static Float trackLength(ArrayList<ArrayList<LatLng>> chapters) {

        Float distance = new Float(0);
        if (chapters != null) {
            if (!chapters.isEmpty()) {
                for (int i = 0; i < chapters.size(); i++) {
                    ArrayList<LatLng> latLngs = chapters.get(i);
                    float[] distance_chapter = new float[1];

                    if (!chapters.get(i).isEmpty()) {
                        for (int j = 0; j < latLngs.size() - 1; j++) {
                            double lat_start = latLngs.get(j).latitude;
                            double long_start = latLngs.get(j).longitude;

                            double lat_end = latLngs.get(j + 1).latitude;
                            double long_end = latLngs.get(j + 1).longitude;
                            Location.distanceBetween(lat_start, long_start, lat_end, long_end, distance_chapter);
                            distance = distance + distance_chapter[0];
                        }
                    }
                }
            }
        }


        return distance;
    }
}
