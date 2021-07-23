package com.android.app.runnable_activitytracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.app.runnable_activitytracker.db.Track;
import com.android.app.runnable_activitytracker.db.TrackRepository;

import java.util.List;

public class TrackViewModel extends AndroidViewModel {

    private TrackRepository trackRepository;
    public static LiveData<List<Track>> allTracks;
    public static LiveData<List<Track>> allTracksASC;
    public static LiveData<List<Track>> recentTracks;
    public static LiveData<Track> track_longest_distance;
    private LiveData<Track> track_lowest_distance;

    public TrackViewModel(@NonNull Application application) {
        super(application);
        trackRepository = new TrackRepository(application);
        allTracks = trackRepository.getAllTracks();
        allTracksASC = trackRepository.getGetAllTracksASC();
        recentTracks = trackRepository.getRecentTracks();
        track_longest_distance = trackRepository.getTrack_longest_distance();
        track_lowest_distance = trackRepository.getTrack_lowest_distance();
    }

    public void insert(Track track) {
        trackRepository.insert(track);
    }

    public void delete(Track track) {trackRepository.deleteTrack(track);}

    public LiveData<List<Track>> getAllTracks() {
        return allTracks;
    }

    public LiveData<List<Track>> getAllTracksASC() {
        return allTracksASC;
    }

    public LiveData<List<Track>> getRecentTracks() {
        return recentTracks;
    }

    public LiveData<Track> getTrack_longest_distance() {
        return track_longest_distance;
    }

    public LiveData<Track> getTrack_lowest_distance() {
        return track_lowest_distance;
    }
}
