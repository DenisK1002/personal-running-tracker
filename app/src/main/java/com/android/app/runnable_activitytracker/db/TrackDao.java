package com.android.app.runnable_activitytracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrackDao {
    @Insert
    void insert(Track track);

    @Delete
    void delete(Track track);

    @Query("SELECT * FROM track_table ORDER BY date DESC")
    LiveData<List<Track>> getAllTracks();

    @Query("SELECT * FROM track_table ORDER BY date ASC")
    LiveData<List<Track>> getAllTracksASC();

    @Query("SELECT * FROM track_table ORDER BY date DESC LIMIT 2")
    LiveData<List<Track>> getRecentTracks();

    @Query("SELECT * FROM track_table ORDER BY distance_meters DESC LIMIT 1")
    LiveData<Track> getTrackLongestDistance();

    @Query("SELECT * FROM track_table ORDER BY distance_meters ASC LIMIT 1")
    LiveData<Track> getTrackLowestDistance();
}
