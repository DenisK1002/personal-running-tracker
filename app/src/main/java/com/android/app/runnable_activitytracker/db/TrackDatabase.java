package com.android.app.runnable_activitytracker.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = Track.class, version = 1)
@TypeConverters(Converter.class)
public abstract class TrackDatabase extends RoomDatabase {

    private static TrackDatabase instance;

    public abstract TrackDao trackDao();

    public static synchronized TrackDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), TrackDatabase.class, "track_database").fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
