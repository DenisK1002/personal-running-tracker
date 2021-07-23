package com.android.app.runnable_activitytracker;

class Constants {

    public static final  String API_WEATHER = "https://api.openweathermap.org/data/2.5/onecall?lat=48.8777333&lon=12.5801538&units=metric&exclude=minutely,hourly&appid=0d1e9fbe6bc6c6e92cec3cf7b8d55e14";

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 336;

    public static final int LOCATION_SERVICE_ID = 4;
    static final String startLocationService = "startLocationService";
    static final String pauseLocationService = "pauseLocationService";
    static final String resumeLocationService = "resumeLocationService";
    static final String stopLocationService = "stopLocationService";
    static final String resetLocationService = "resetLocationService";

    static final Float POLYLINE_WIDTH = Float.valueOf(8);

    public static final String intent_track_id_tag = "TRACK_ID_INTENT";
}
