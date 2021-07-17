package com.android.app.runnable_activitytracker;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.runnable_activitytracker.db.Track;
import com.android.app.runnable_activitytracker.weather.WeatherForecastRecViewAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class RecentTrackMainRecVAdapter extends RecyclerView.Adapter<RecentTrackMainRecVAdapter.ViewHolder> {

    ArrayList<Track> tracks = new ArrayList<>();


    @NonNull
    @Override
    public RecentTrackMainRecVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workoutadapter, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.image_map.setImageBitmap(tracks.get(position).getImage());
        holder.txt_trackName.setText(tracks.get(position).getTrackName());
        Date date = new Date(tracks.get(position).getDate());
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        holder.txt_date.setText(formatter.format(date));
        holder.txt_distance.setText(tracks.get(position).getDistance_meters() + " m");

        long millis = tracks.get(position).getRun_duration();
        int hours = (int) (millis / (1000 * 60 * 60));
        int mins = (int) ((millis / (1000 * 60)) % 60);

        String diff = hours + " h " + mins + " min";
        holder.txt_time.setText(diff);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView image_map;
        private TextView txt_trackName, txt_date, txt_distance, txt_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_map = itemView.findViewById(R.id.imv_workout_adapter_map);
            txt_trackName = itemView.findViewById(R.id.txt_workout_adapter_trackName);
            txt_date = itemView.findViewById(R.id.txt_workout_adapter_date);
            txt_distance = itemView.findViewById(R.id.txt_workout_adapter_distance);
            txt_time = itemView.findViewById(R.id.txt_duration_adapter);
        }
    }
}
