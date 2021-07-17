package com.android.app.runnable_activitytracker;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class TrackerService extends Service {

    PowerManager pm;
    PowerManager.WakeLock wl;
    public static final String WL_TAG = "Mywltag";

    public static MutableLiveData<Boolean> isTracking = new MutableLiveData<>();

    public static MutableLiveData<LatLng> position = new MutableLiveData<>();

    public static MutableLiveData<Float> running_speed = new MutableLiveData<>();
    public static MutableLiveData<ArrayList<Float>> speeds_chart = new MutableLiveData<>();
    ArrayList<Float> prototype_speed = new ArrayList<>();

    public static MutableLiveData<ArrayList<Float>> distances = new MutableLiveData<>();
    ArrayList<Float> prototype_distances = new ArrayList<>();

    public static MutableLiveData<Float> average_running_speed = new MutableLiveData<>();

    public static MutableLiveData<ArrayList<Long>> run_duration = new MutableLiveData<>();
    Long run_started_time;
    ArrayList<Long> chapter_times = new ArrayList<>();

    public static MutableLiveData<Long> pace = new MutableLiveData<>();
    Long pace_time = Long.valueOf(0);
    ArrayList<ArrayList<LatLng>> pace_checker = new ArrayList<>();

    public static MutableLiveData<ArrayList<ArrayList<LatLng>>> pathLines_mld = new MutableLiveData<>();

    ArrayList<ArrayList<LatLng>> pathLines = new ArrayList<>();
    ArrayList<LatLng> pathLine = new ArrayList<>();

    ArrayList<Float> speeds = new ArrayList<>();


    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            wl.acquire(30*60*1000L /*10 minutes*/);
            super.onLocationResult(locationResult);

            //SAVE LOCATION DATA

            if (locationResult != null && locationResult.getLastLocation() != null) {
                if (isTracking.getValue()) {
                    Log.d("location", locationResult.getLastLocation().toString());
                    addLatestData(locationResult);
                    updateMutableLiveData(locationResult);
                    updateTime();
                }
            }
        }
    };


    private void updateMutableLiveData(LocationResult locationResult) {
        running_speed.postValue(locationResult.getLastLocation().getSpeed());
        prototype_speed.add(locationResult.getLastLocation().getSpeed());
        speeds_chart.postValue(prototype_speed);

        prototype_distances.add(trackLength(pathLines));
        distances.postValue(prototype_distances);

        double latitude = locationResult.getLastLocation().getLatitude();
        double longitute = locationResult.getLastLocation().getLongitude();
        LatLng latLng = new LatLng(latitude, longitute);
        position.postValue(latLng);
        pace_checker();
    }

    private void updateTime() {

        Long t = System.currentTimeMillis() - run_started_time;
        chapter_times.set(chapter_times.size()-1, t);
        run_duration.postValue(chapter_times);
    }

    private void addLatestData(LocationResult locationResult) {
        pathLine.add(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
        pathLines.set(pathLines.size() - 1, (ArrayList<LatLng>) pathLine.clone());
        pace_checker.set(pace_checker.size() - 1, (ArrayList<LatLng>) pathLine.clone());
        pathLines_mld.postValue(pathLines);
    }

    private void addEmptyList() {
        pathLine.clear();
        ArrayList<LatLng> initial_new = new ArrayList<>();
        pathLines.add(initial_new);
        pace_checker.add(initial_new);
        chapter_times.add(0L);
    }

    private void computeAverageSpeed() {
        Float allspeeds = 0f;
        for (int i = 0; i < speeds.size(); i++) {
            allspeeds = allspeeds + speeds.get(i);
        }
        average_running_speed.postValue(allspeeds/speeds.size());
    }

    private void pace_checker() {
        if (TrackingDataFormatter.trackLength(pace_checker) >= 1000.0f) {
            pace_time = System.currentTimeMillis() - pace_time;
            pace.postValue(pace_time);

            ArrayList<LatLng> initial_new = new ArrayList<>();
            pace_checker.clear();
            pace_checker.add(initial_new);
        }
    }


    @SuppressLint({"MissingPermission", "InvalidWakeLockTag"})
    private void startLocationService() {

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WL_TAG);

        run_started_time = System.currentTimeMillis();

        pace.postValue(Long.valueOf(0));
        pace_time = System.currentTimeMillis();

        addEmptyList();
        isTracking.postValue(true);


        String channelId = "runnable_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Runnable Activity Tracker");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Service Runnable", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by Location Service");
                notificationManager.createNotificationChannel(notificationChannel);
            }

        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService() {

        wl.release();

        isTracking.postValue(false);
        computeAverageSpeed();

        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(false);
        stopSelf();
    }

    private void pauseLocationService() {
        isTracking.postValue(false);
    }

    private void resumeLocationService() {
        addEmptyList();
        run_started_time = System.currentTimeMillis();
        isTracking.postValue(true);
    }

    private void resetLocationService() {
        isTracking.postValue(false);
        pathLines_mld.postValue(new ArrayList<ArrayList<LatLng>>());
        pathLines.clear();
        pathLine.clear();
        running_speed.postValue(0.0f);
        speeds.clear();
        prototype_speed.clear();
        prototype_distances.clear();
        distances.postValue(prototype_distances);
        speeds_chart.postValue(prototype_speed);
        stopLocationService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.startLocationService)) {
                    startLocationService();
                } else if (action.equals(Constants.stopLocationService)) {
                    stopLocationService();
                } else if (action.equals(Constants.pauseLocationService)) {
                    pauseLocationService();
                } else if (action.equals(Constants.resumeLocationService)) {
                    resumeLocationService();
                } else if (action.equals(Constants.resetLocationService)) {
                    resetLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
    }

    private Float trackLength(ArrayList<ArrayList<LatLng>> chapters) {

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
