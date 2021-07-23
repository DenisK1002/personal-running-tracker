package com.android.app.runnable_activitytracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.runnable_activitytracker.db.Track;
import com.android.app.runnable_activitytracker.weather.Weather;
import com.android.app.runnable_activitytracker.weather.WeatherForecastRecViewAdapter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.robinhood.spark.SparkView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SparkView sparkView_distance, sparkView_time;

    private TrackViewModel viewModel;

    private TextView weatherCityName, txtTemp, weatherDescription, alert_text;
    private TextView forecast_toggle;
    private TextView txt_seeAll;
    private ExtendedFloatingActionButton btn_newWorkout;

    private ImageView weatherStatusIcon, alert_icon;
    private RecyclerView forecast_RecyclerView, recentWorkouts_RecyclerView;

    private RequestQueue mReqQue;
    private ArrayList<Weather> weatherArrayList = new ArrayList<>();

    private List<Track> recentTracks = new ArrayList<>();
    private List<Track> allTracks = new ArrayList<>();

    private ArrayList<Float> yDataDistance = new ArrayList<>();
    private ArrayList<Float> yDataTime = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(TrackViewModel.class);


        //CURRENT WEATHER DATA IN CARDVIEW
        txtTemp = findViewById(R.id.txtTemp);
        weatherDescription = findViewById(R.id.weatherDescription);
        weatherCityName = findViewById(R.id.weatherCityName);
        alert_text = findViewById(R.id.weatherAlerts);

        weatherStatusIcon = findViewById(R.id.imv_weatherIcon);
        alert_icon = findViewById(R.id.icon_weatherAlerts);

        btn_newWorkout = findViewById(R.id.fab_new_workout);
        txt_seeAll = findViewById(R.id.txt_seeAll);


        //REC VIEW FORECAST

        forecast_RecyclerView = findViewById(R.id.recv_forecast);

        WeatherForecastRecViewAdapter weatherForecastRecViewAdapter = new WeatherForecastRecViewAdapter(this);
        weatherForecastRecViewAdapter.setWeather(weatherArrayList);

        forecast_RecyclerView.setAdapter(weatherForecastRecViewAdapter);
        forecast_RecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        mReqQue = Volley.newRequestQueue(this);


        //BUTTON TO TOGGLE RECVIEW OF FORECAST
        forecast_toggle = findViewById(R.id.txt_forecast_toggle);

        forecast_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (forecast_RecyclerView.getVisibility() != View.VISIBLE) {
                    forecast_RecyclerView.setVisibility(View.VISIBLE);
                    forecast_toggle.setText("Hide forecast");
                    
                } else {
                    forecast_RecyclerView.setVisibility(View.GONE);
                    forecast_toggle.setText("See forecast");
                }
            }
        });


        //NEW WORKOUT ACTIVITY
        setButtonState();

        btn_newWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RunningActivity.class);
                startActivity(intent);
            }
        });

        //ALL WORKOUTS TEXTVIEW(BUTTON)

        txt_seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainSeeAllTracksActivity.class);
                startActivity(intent);
            }
        });

        //REC VIEW RECENT WORKOUTS
        recentWorkouts_RecyclerView = findViewById(R.id.recv_workout_main);
        RecentTrackMainRecVAdapter recentTrackMainRecVAdapter = new RecentTrackMainRecVAdapter();
        viewModel.getRecentTracks().observe(this, new Observer<List<Track>>() {
            @Override
            public void onChanged(List<Track> tracks) {
                ArrayList<Track> passer = new ArrayList<>();
                passer.addAll(tracks);
                recentTrackMainRecVAdapter.setTracks(passer);
            }
        });

        recentWorkouts_RecyclerView.setAdapter(recentTrackMainRecVAdapter);
        recentWorkouts_RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentTrackMainRecVAdapter.setOnItemClickListener(new RecentTrackMainRecVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Track track) {
                Intent intent = new Intent(MainActivity.this, ActivityTrack.class);
                intent.putExtra(Constants.intent_track_id_tag, track.getId());
                startActivity(intent);
            }
        });

        //sparkView_distance = (SparkView) findViewById(R.id.sparkview_distance);
        sparkView_time = (SparkView) findViewById(R.id.sparkview_time);
        sparkView_distance = (SparkView) findViewById(R.id.sparkview_distance);

        SparkLineAdapter sparkLineAdapter = new SparkLineAdapter(yDataDistance);
        SparkLineAdapter sparkLineAdapter1 = new SparkLineAdapter(yDataTime);
        sparkView_distance.setAdapter(sparkLineAdapter);
        sparkView_time.setAdapter(sparkLineAdapter1);


        viewModel.getAllTracksASC().observe(this, new Observer<List<Track>>() {
            @Override
            public void onChanged(List<Track> tracks) {
                MaterialCardView statistics_card = findViewById(R.id.statistics_card);
                if (tracks.size() > 1) {
                    statistics_card.setVisibility(View.VISIBLE);
                    allTracks = tracks;
                    updateYData(tracks);
                    sparkLineAdapter.setyData(yDataDistance);
                    sparkLineAdapter1.setyData(yDataTime);


                } else {
                    statistics_card.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateYData(List<Track> tracks) {
        yDataDistance.clear();
        yDataTime.clear();
        for (Track t : tracks) {
            yDataDistance.add((float) t.getDistance_meters());
            yDataTime.add((float) t.getRun_duration());
        }
        setSparkLineColor();
    }

    private void setSparkLineColor() {
        if (yDataDistance.get(yDataDistance.size()-2) > yDataDistance.get(yDataDistance.size()-1)) {
            sparkView_distance.setLineColor(getResources().getColor(R.color.red));
        } else {
            sparkView_distance.setLineColor(getResources().getColor(R.color.sparkline_color));
        }

        if (yDataTime.get(yDataTime.size()-2) > yDataTime.get(yDataTime.size()-1)) {
            sparkView_time.setLineColor(getResources().getColor(R.color.red));
        } else {
            sparkView_time.setLineColor(getResources().getColor(R.color.sparkline_color));
        }
    }

    private void setButtonState() {
        if (isLocationServiceRunning()) {
            btn_newWorkout.setText("ACTIVE WORKOUT");
            btn_newWorkout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        } else {
            btn_newWorkout.setText("NEW WORKOUT");
            btn_newWorkout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
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




    @Override
    protected void onResume() {
        super.onResume();
        updateWeather(Constants.API_WEATHER);
        setButtonState();
    }

    public void updateWeather(String url) {
        getCurrentWeatherData(url);
    }

    private void getCurrentWeatherData(String url) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                
                try {

                    JSONObject current = response.getJSONObject("current");
                    JSONArray weatherArray = current.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);

                    String temp = String.valueOf(current.getInt("temp"));
                    String timezone = response.getString("timezone");
                    String icon = weather.getString("icon");
                    String description = weather.getString("description");

                    if (timezone != null && temp != null && icon != null && description != null) {

                        weatherCityName.setText(timezone);
                        txtTemp.setText(String.valueOf(current.getInt("temp")) + " °C");
                        txtTemp.setTextSize(25);
                        weatherDescription.setText(description);
                        Glide.with(MainActivity.this).asBitmap().load("https://openweathermap.org/img/wn/" + icon + "@2x.png").into(weatherStatusIcon);
                        weatherStatusIcon.setMaxHeight(100);
                        weatherStatusIcon.setMaxWidth(100);

                    }

                    //GET ALERT TAGS
                    try {
                        JSONArray alert_array = response.getJSONArray("alerts");
                        JSONObject alert_array_object = alert_array.getJSONObject(0);

                        String alert = alert_array_object.getString("description");

                        if (alert_array != null) {
                            alert_icon.setImageResource(R.drawable.ic_alert);
                            alert_text.setTextColor(getResources().getColor(R.color.Alert));
                            alert_text.setText(alert);
                        } else {
                            alert_icon.setImageResource(R.drawable.ic_alert_noalert);
                            alert_text.setText("No extreme weather condition");
                            alert_text.setTextColor(getResources().getColor(R.color.noAlert));
                        }
                    } catch (JSONException e) {
                        Log.e("ERROR ALERT", e.toString());
                        e.printStackTrace();
                    }

                    //GET FORECAST

                    if (weatherArrayList.isEmpty()) {
                        JSONArray forecast_daily = response.getJSONArray("daily");

                        for (int i = 1; i < forecast_daily.length(); i++) {
                            JSONObject forecast_day = forecast_daily.getJSONObject(i);
                            JSONObject forecast_temp = forecast_day.getJSONObject("temp");
                            JSONArray forecast_icon = forecast_day.getJSONArray("weather");
                            JSONObject forecast_icon_id = forecast_icon.getJSONObject(0);

                            if (forecast_day.length() != 0) {
                                weatherArrayList.add(new Weather(
                                        forecast_day.getInt("dt"),
                                        String.valueOf(forecast_temp.getInt("day")),
                                        forecast_icon_id.getString("icon")));
                            }
                        }
                    }


                } catch (JSONException e) {
                    Log.e("ERROR", e.toString());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        });
        mReqQue.add(request);
    }
}