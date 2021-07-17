package com.android.app.runnable_activitytracker.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.runnable_activitytracker.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class WeatherForecastRecViewAdapter extends RecyclerView.Adapter<WeatherForecastRecViewAdapter.ViewHolder>{

    private Context context;

    private ArrayList<Weather> weather = new ArrayList<>();

    public WeatherForecastRecViewAdapter(Context context) {
        this.context = context;
    }

    public void setWeather(ArrayList<Weather> weather) {
        this.weather = weather;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weatheradapter, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.forecast_day.setText(weather.get(position).getDay());
        holder.forecast_temp.setText(weather.get(position).getTemp() + " °C");

        String iconID = weather.get(position).getIconID();
        Glide.with(context).asBitmap().load("https://openweathermap.org/img/wn/" + iconID + "@2x.png").into(holder.forecast_weatherIcon);
    }

    @Override
    public int getItemCount() {
        return weather.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView forecast_day, forecast_temp;
        private ImageView forecast_weatherIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            forecast_day = itemView.findViewById(R.id.forecast_day);
            forecast_temp = itemView.findViewById(R.id.forecast_temp);
            forecast_weatherIcon = itemView.findViewById(R.id.forecast_weatherIcon);
        }
    }
}
