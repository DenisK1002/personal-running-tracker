package com.android.app.runnable_activitytracker.db;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TrackRepository {
    private TrackDao trackDao;
    private LiveData<List<Track>> allTracks;
    private LiveData<List<Track>> allTracksASC;
    private LiveData<List<Track>> recentTracks;
    private LiveData<Track> track_longest_distance;
    private LiveData<Track> track_lowest_distance;


    public TrackRepository(Application application) {
        TrackDatabase database = TrackDatabase.getInstance(application);
        trackDao = database.trackDao();
        allTracks = trackDao.getAllTracks();
        allTracksASC = trackDao.getAllTracksASC();
        recentTracks = trackDao.getRecentTracks();
        track_longest_distance = trackDao.getTrackLongestDistance();
        track_lowest_distance = trackDao.getTrackLowestDistance();
    }

    public void insert(Track track) {
        new InsertTrackAsyncTask(trackDao).execute(track);
    }

    public void deleteTrack(Track track) {
        new DeleteTrackAsyncTask(trackDao).execute(track);
    }

    public LiveData<List<Track>> getAllTracks() {
        return allTracks;
    }

    public LiveData<List<Track>> getGetAllTracksASC() {
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

    private static class InsertTrackAsyncTask extends AsyncTask<Track, Void, Void> {

        private TrackDao trackDao;

        public InsertTrackAsyncTask(TrackDao trackDao) {
            this.trackDao = trackDao;
        }

        @Override
        protected Void doInBackground(Track... tracks) {
            trackDao.insert(tracks[0]);
            return null;
        }
    }

    private static class DeleteTrackAsyncTask extends AsyncTask<Track, Void, Void> {

        private TrackDao trackDao;

        public DeleteTrackAsyncTask(TrackDao trackDao) {
            this.trackDao = trackDao;
        }

        @Override
        protected Void doInBackground(Track... tracks) {
            trackDao.delete(tracks[0]);
            return null;
        }
    }
}
