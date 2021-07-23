package com.android.app.runnable_activitytracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.app.runnable_activitytracker.db.Track;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityTrack extends AppCompatActivity {

    TrackViewModel trackViewModel;

    private int trackId;

    private TextView txt_title, txt_date, txt_track_pace, txt_pace_best;
    private ImageView image_track;
    private LinearProgressIndicator linebar;
    private LineChart distance_linechart;

    ArrayList<Track> allTracks = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        //getting id from clicked Run
        Intent intent = getIntent();
        trackId = intent.getIntExtra(Constants.intent_track_id_tag, 0);

        //Access Database for all tracks
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);

        //Initiate Views
        txt_title = findViewById(R.id.txt_activity_track_title);
        txt_date = findViewById(R.id.txt_activity_track_date);
        txt_track_pace = findViewById(R.id.txt_progress_pace);
        txt_pace_best = findViewById(R.id.txt_progress_best_pace);

        linebar = findViewById(R.id.progress_pace);

        image_track = findViewById(R.id.imv_activity_track_image);

        distance_linechart = findViewById(R.id.linechart_distance_activity_track);

        //method for accessing database content
        getData();

    }

    //get all track from database
    private void getData() {
        trackViewModel.getAllTracksASC().observe(this, new Observer<List<Track>>() {
            @Override
            public void onChanged(List<Track> tracks) {
                allTracks.addAll(tracks);
                setData();
                setDistance_linechart();
            }
        });
    }

    private void setData() {

        Date date;
        DateFormat formatter;

        Long track_pace = 0L;
        Long best_pace = 1000000000000000000L;

        if (allTracks.size() > 1) {
            for (Track track : allTracks) {
                if (track.getId() == trackId) {
                    txt_title.setText(track.getTrackName());

                    date = new Date(track.getDate());
                    formatter = new SimpleDateFormat("dd/MM/yyyy");

                    txt_date.setText(formatter.format(date));
                    image_track.setImageBitmap(track.getImage());

                    //setting pace information
                    if (track.getRun_pace() != null) {
                        track_pace = track.getRun_pace();
                        date = new Date(track_pace);
                        formatter = new SimpleDateFormat("mm:ss");
                        txt_track_pace.setText(formatter.format(date));
                    } else {
                        Toast.makeText(this, "No Pace available", Toast.LENGTH_SHORT).show();
                    }
                }

                //getting best pace of all tracks to textview
                if (track.getRun_pace() != null && track.getRun_pace() < best_pace) {
                    best_pace = track.getRun_pace();
                }
            }
            if (track_pace != 0L && best_pace != 0L) {

                if (track_pace.equals(best_pace)) {
                    linebar.setProgress(100, true);
                    txt_pace_best.setText(txt_track_pace.getText());
                    linebar.setIndicatorColor(getColor(R.color.green));
                    findViewById(R.id.check_pace_is_best).setVisibility(View.VISIBLE);
                }

                //setting best pace textview
                date = new Date(best_pace);
                formatter = new SimpleDateFormat("mm:ss");
                txt_pace_best.setText(formatter.format(date));

                Double p = (best_pace * 1.0/track_pace)*100.0;
                String pps[] = p.toString().split("\\.");
                int percent = Integer.parseInt(pps[0]);

                Log.d("progress",  String.valueOf(best_pace) + " / " + String.valueOf(track_pace) + " = " + String.valueOf(percent) + "   " + String.valueOf(p));
                linebar.setProgress(percent, true);
            } else {
                txt_pace_best.setText("00:00");
                txt_track_pace.setText("00:00");
                linebar.setProgress(100, true);
            }

        }
    }

    private ArrayList<Entry> lineChartDataSet() {
        ArrayList<Entry> dataSet = new ArrayList<>();


        if (allTracks.size() > 1) {
            for (Track track : allTracks) {
                dataSet.add(new Entry(track.getId(), track.getDistance_meters()));
            }
        }
        return dataSet;
    }

    private void setDistance_linechart() {
        XAxis xAxis = distance_linechart.getXAxis();
        xAxis.setValueFormatter(new DateValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        distance_linechart.getAxisRight().setEnabled(false);

        LineDataSet lineDataSet_distance = new LineDataSet(lineChartDataSet(), "Distance");
        ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
        iLineDataSets.add(lineDataSet_distance);



        LineData lineData_distance = new LineData(iLineDataSets);
        distance_linechart.setData(lineData_distance);
        distance_linechart.invalidate();

        distance_linechart.getAxisLeft().setDrawGridLines(false);
        distance_linechart.getXAxis().setDrawGridLines(false);

        lineDataSet_distance.setColor(getColor(R.color.linechart_color));
        lineDataSet_distance.setLineWidth(4f);


        Highlight high = new Highlight((float) trackId, 0, 0);
        distance_linechart.highlightValue(high);
        lineDataSet_distance.setHighLightColor(getColor(R.color.red));

        distance_linechart.setPadding(20, 0, 0, 20);

        distance_linechart.setPaddingRelative(20,0,0, 20);

        distance_linechart.animateX(1000);
    }

    public class DateValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {

            for (Track track : allTracks) {
                if (track.getId() == (int) value) {
                    Log.d("format", String.valueOf(track.getId()) + "    " + String.valueOf((int) value));
                    Date date = new Date(track.getDate());
                    @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("dd.MM");
                    return formatter.format(date);
                }
            }

            return "";
        }
    }
}
