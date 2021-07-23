package com.android.app.runnable_activitytracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.app.runnable_activitytracker.db.Track;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.robinhood.spark.SparkView;

import java.math.BigDecimal;
import java.util.ArrayList;


public class RunningActivity extends AppCompatActivity implements OnMapReadyCallback {


    private TrackViewModel trackViewModel;

    private EditText et_trackName;

    private Button btn_start_stop_workout;

    private TextView txt_distance, txt_speed, txt_resetData, txt_time;

    private ImageView imv_pause, imv_resume;

    private GoogleMap mMap;

    public static Bitmap image_run;

    public static ArrayList<ArrayList<LatLng>> path = new ArrayList<>();

    private int distance;

    private Long time_run;

    private Long pace;

    public SupportMapFragment mapFragment;

    private SparkView sparkView_distance, sparkView_speed;
    private SparkLineAdapter sparkLineAdapter_distance, sparkLineAdapter_speed;



    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);


        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);

        //SET UP MAP
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_activity);
        mapFragment.getMapAsync(this);


        et_trackName = findViewById(R.id.track_name);

        imv_pause = findViewById(R.id.imv_activity_pause);
        imv_resume = findViewById(R.id.imv_activity_resume);

        txt_speed = findViewById(R.id.txt_speed);
        txt_distance = findViewById(R.id.txt_activity_distance);
        txt_resetData = findViewById(R.id.txt_reset);
        txt_time = findViewById(R.id.txt_duration);

        if (isLocationServiceRunning()) {txt_resetData.setVisibility(View.VISIBLE);}


        btn_start_stop_workout = findViewById(R.id.btn_activity_start_stop);
        setStartStopButtonState(btn_start_stop_workout);

        setButtonListeners();

        getRecentData();
        listenToLiveData();

        sparkView_distance = findViewById(R.id.activity_running_sparkview_distance);
        sparkView_speed = findViewById(R.id.activity_running_sparkview_speed);

        sparkLineAdapter_distance = new SparkLineAdapter(new ArrayList<Float>());
        sparkLineAdapter_speed = new SparkLineAdapter(new ArrayList<Float>());

        sparkView_distance.setAdapter(sparkLineAdapter_distance);
        sparkView_speed.setAdapter(sparkLineAdapter_speed);

        sparkView_distance.setFillType(SparkView.FillType.DOWN);
        sparkView_distance.setFill(true);
        sparkView_speed.setFillType(SparkView.FillType.DOWN);
        sparkView_speed.setFill(true);
    }

    private void setSparkViewDistance(ArrayList<Float> distances) {

        if (distances.size() > 1) {
            if (sparkView_distance.getVisibility() == View.GONE) {
                sparkView_distance.setVisibility(View.VISIBLE);
            }
            if (distances.get(distances.size()-2) > distances.get(distances.size()-1)) {
                sparkView_distance.setLineColor(getResources().getColor(R.color.red));
            } else {
                sparkView_distance.setLineColor(getResources().getColor(R.color.sparkline_color));
            }
        }



    }

    private void setSparkViewSpeed(ArrayList<Float> speeds) {
        if (speeds.size() > 1) {
            if (sparkView_speed.getVisibility() == View.GONE) {
                sparkView_speed.setVisibility(View.VISIBLE);
            }
            if (speeds.get(speeds.size()-2) > speeds.get(speeds.size()-1)) {
                sparkView_speed.setLineColor(getResources().getColor(R.color.red));
            } else {
                sparkView_speed.setLineColor(getResources().getColor(R.color.sparkline_color));
            }
        }
    }

    private void setButtonListeners() {
        btn_start_stop_workout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RunningActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION);
                } else {

                    if (btn_start_stop_workout.getText().equals("Start")) {
                        startLocationServices();
                        btn_start_stop_workout.setText("Stop");
                        btn_start_stop_workout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                        imv_resume.setVisibility(View.GONE);
                        imv_pause.setVisibility(View.VISIBLE);
                        if (mMap != null) {
                            mMap.setMyLocationEnabled(true);
                        }

                    } else {
                        btn_start_stop_workout.setText("Start");
                        btn_start_stop_workout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                        imv_pause.setVisibility(View.GONE);
                        imv_resume.setVisibility(View.VISIBLE);
                        stopLocationServices();
                        mMap.setMyLocationEnabled(false);
                    }
                }
            }
        });

        imv_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLocationServiceRunning()) {
                    resumeLocationServices();
                }
            }
        });

        imv_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLocationServiceRunning()) {
                    pauseLocationServices();
                }
            }
        });

        txt_resetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetData();
            }
        });


        //mcardv_screen_off.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        wl.acquire(30*60*1000L /*30 minutes*/);

        //    }
        //});
    }

    private void setPauseResumeButtonState(Boolean isTracking) {
        if (isTracking) {
            imv_resume.setVisibility(View.GONE);
            imv_pause.setVisibility(View.VISIBLE);
        } else {
            imv_pause.setVisibility(View.GONE);
            imv_resume.setVisibility(View.VISIBLE);
        }
    }

    private void setStartStopButtonState(Button btn_start_stop_workout) {
        if (isLocationServiceRunning()) {
            btn_start_stop_workout.setText("Stop");
            btn_start_stop_workout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
        } else {
            btn_start_stop_workout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            btn_start_stop_workout.setText("Start");
        }
    }

    private void listenToLiveData() {
        TrackerService.pathLines_mld.observe(this, new Observer<ArrayList<ArrayList<LatLng>>>() {
            @Override
            public void onChanged(ArrayList<ArrayList<LatLng>> arrayLists) {
                Log.d("LIST", arrayLists.toString());
                path = arrayLists;
                updateDistance(arrayLists);
                if (mMap != null) {
                    addAllPolylines(arrayLists);
                }
            }
        });
        TrackerService.running_speed.observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                updateSpeed(aFloat);
            }
        });
        TrackerService.position.observe(this, new Observer<LatLng>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onChanged(LatLng latLng) {
                if (mMap != null) {
                    updateLocation(latLng);
                    if (!mMap.isMyLocationEnabled()) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            }
        });
        TrackerService.isTracking.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                setPauseResumeButtonState(aBoolean);
            }
        });
        TrackerService.run_duration.observe(this, new Observer<ArrayList<Long>>() {
            @Override
            public void onChanged(ArrayList<Long> longs) {
                Long time_calc = 0L;

                for (Long times : longs) {
                    time_calc = time_calc + times;
                }
                updateRunTime(time_calc);
            }
        });
        TrackerService.pace.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                updatePace(aLong);
            }
        });
        TrackerService.distances.observe(this, new Observer<ArrayList<Float>>() {
            @Override
            public void onChanged(ArrayList<Float> floats) {
                updateDistanceSparkChart(floats);
            }
        });
        TrackerService.speeds_chart.observe(this, new Observer<ArrayList<Float>>() {
            @Override
            public void onChanged(ArrayList<Float> floats) {
                updateSpeedSparkChart(floats);
            }
        });
    }

    private void updateSpeedSparkChart(ArrayList<Float> speeds) {
        if (sparkLineAdapter_speed != null) {
            sparkLineAdapter_speed.setyData(speeds);
            sparkLineAdapter_speed.notifyDataSetChanged();
            setSparkViewSpeed(speeds);
        }

    }

    private void updateDistanceSparkChart(ArrayList<Float> distances) {
        if (sparkLineAdapter_distance != null)
        {
            sparkLineAdapter_distance.setyData(distances);
            sparkLineAdapter_distance.notifyDataSetChanged();
            setSparkViewDistance(distances);
        }
    }

    private void updateSpeed(Float afloat) {
        BigDecimal bd = new BigDecimal(Float.toString(afloat));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        txt_speed.setText(bd.toString() + " Km/h");
    }

    private void updateDistance(ArrayList<ArrayList<LatLng>> chapters) {
        BigDecimal bd = new BigDecimal(Float.toString(TrackingDataFormatter.trackLength(chapters)));
        bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        txt_distance.setText(bd.toString() + " m");
        distance = Integer.valueOf(bd.toString());
    }

    @SuppressLint("MissingPermission")
    private void updateLocation(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    private void updateRunTime(Long time) {
        int hours = (int) (time /  (1000 * 60 * 60));
        int mins = (int) ((time / (1000 * 60)) % 60);

        String t = hours + " h " + mins + " min";

        txt_time.setText(t);
        time_run = time;
    }

    private void updatePace(Long pace_time) {
        /*int mins = (int) ((pace_time / (1000 * 60)) % 60);
        int seconds = (int) (pace_time / 1000) % 60;
        String pace = mins + ":" + seconds; */
        pace = pace_time;

    }


    private void addLatestPolyline(ArrayList<ArrayList<LatLng>> chapters) {
        if (!chapters.isEmpty() && chapters.get(chapters.size()-1).size() > 1) {
            ArrayList<LatLng> lastlist = chapters.get(chapters.size()-1);
            LatLng previousLtnLng = lastlist.get(lastlist.size()-2);
            LatLng currentLtnLng = lastlist.get(lastlist.size()-1);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(getResources().getColor(R.color.green))
                    .width(Constants.POLYLINE_WIDTH)
                    .add(previousLtnLng)
                    .add(currentLtnLng);
            mMap.addPolyline(polylineOptions);
        }
    }



    private void getRecentData() {
        path = TrackerService.pathLines_mld.getValue();
        if (path == null) {
            path = new ArrayList<>();
        }
        updateDistance(path);
        resumeMap();
    }

    private void resumeMap() {
        if (mMap != null && !path.isEmpty() && isLocationServiceRunning()) {
            mMap.clear();
            path = TrackerService.pathLines_mld.getValue();
            addAllPolylines(path);
        }
    }

    private void addAllPolylines(ArrayList<ArrayList<LatLng>> chapters) {
        Log.d("chapters", chapters.toString());
        mMap.clear();
        if (!chapters.isEmpty()) {
            for (int i = 0; i < chapters.size(); i++) {
                if (chapters.get(i).size() > 1) {
                    ArrayList<LatLng> sublist = chapters.get(i);
                    for (int j = 0; j < sublist.size() - 1; j++) {
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .color(getResources().getColor(R.color.green))
                                .width(Constants.POLYLINE_WIDTH)
                                .addAll(sublist);
                        mMap.addPolyline(polylineOptions);
                    }
                }
            }
        }
    }

    private void resetData() {
        mMap.clear();
        resetLocationServices();
        setPauseResumeButtonState(TrackerService.isTracking.getValue());

        btn_start_stop_workout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        btn_start_stop_workout.setText("Start");
    }

    @SuppressLint("MissingPermission")
    private void zoomToWholeTrack() {
        mMap.setMyLocationEnabled(false);
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (int i = 0; i < path.size(); i++) {
            ArrayList<LatLng> sublist = path.get(i);
            for (int j = 0; j < sublist.size(); j++) {
                bounds.include(sublist.get(j));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), mapFragment.getView().getWidth(), mapFragment.getView().getHeight(), Integer.valueOf((int) (mapFragment.getView().getHeight() * 0.05))));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                btn_start_stop_workout.setText("Stop");
                btn_start_stop_workout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                startLocationServices();
            } else {
            }
        }
    }

    @SuppressWarnings("deprecation")
    private Boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (TrackerService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void saveRun() {
        zoomToWholeTrack();
        SystemClock.sleep(1000);

        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(@Nullable Bitmap bitmap) {
                Float average_speed = TrackerService.average_running_speed.getValue();
                image_run = bitmap;
                String trackName = et_trackName.getText().toString();
                Long date = System.currentTimeMillis();
                Long runTime = time_run;
                Track newTrack = new Track(trackName, image_run, distance, average_speed, pace, date, runTime);
                trackViewModel.insert(newTrack);
            }
        });

        Toast.makeText(RunningActivity.this, "Run saved", Toast.LENGTH_SHORT).show();

    }

    private void clearAll() {
        mMap.clear();
        distance = 0;
        path.clear();
    }

    private void resumeLocationServices() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), TrackerService.class);
            intent.setAction(Constants.resumeLocationService);
            startService(intent);
        }
    }

    private void pauseLocationServices() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), TrackerService.class);
            intent.setAction(Constants.pauseLocationService);
            startService(intent);
        }
    }

    private void startLocationServices() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), TrackerService.class);
            intent.setAction(Constants.startLocationService);
            startService(intent);
            clearAll();
            txt_resetData.setVisibility(View.VISIBLE);
        }
    }

    private void stopLocationServices() {
        if (isLocationServiceRunning()) {
            saveRun();
            Intent intent = new Intent(getApplicationContext(), TrackerService.class);
            intent.setAction(Constants.stopLocationService);
            startService(intent);

        }
    }

    private void resetLocationServices() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), TrackerService.class);
            intent.setAction(Constants.resetLocationService);
            startService(intent);
            setPauseResumeButtonState(false);
            setStartStopButtonState(btn_start_stop_workout);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapFragment.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapFragment.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //wl.release();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapFragment.onSaveInstanceState(outState);
    }
}