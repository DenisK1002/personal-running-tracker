package com.android.app.runnable_activitytracker;

import android.graphics.RectF;

import com.robinhood.spark.SparkAdapter;

import java.util.ArrayList;

public class SparkLineAdapter extends SparkAdapter {

    private ArrayList<Float> yData;

    public SparkLineAdapter(ArrayList<Float> ydata) {
        this.yData = ydata;
    }

    public void setyData(ArrayList<Float> yData) {
        this.yData = yData;
        notifyDataSetChanged();
    }

    public float getMinY() {
        float lowest = 10000000000f;
        for (Float f : yData) {
            if (f.longValue() < lowest) {
                lowest = f;
            }
        }
        return lowest;
    }

    public float getMaxY() {
        float highest = 0f;
        for (Float f : yData) {
            if (f.longValue() > highest) {
                highest = f;
            }
        }
        return highest;
    }

    @Override
    public float getX(int index) {
        return super.getX(index);
    }

    @Override
    public boolean hasBaseLine() {
        return super.hasBaseLine();
    }

    @Override
    public float getBaseLine() {
        return super.getBaseLine();
    }

    @Override
    public RectF getDataBounds() {
        RectF bounds = super.getDataBounds();
        return bounds;
    }

    @Override
    public int getCount() {
        return yData.size();
    }

    @Override
    public Object getItem(int index) {
        return yData.get(index);
    }

    @Override
    public float getY(int index) {
        return yData.get(index);
    }
}
