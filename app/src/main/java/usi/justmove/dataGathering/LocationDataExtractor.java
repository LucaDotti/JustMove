package usi.justmove.dataGathering;

import usi.justmove.database.LocalDbController;
import usi.justmove.database.LocalSQLiteDBHelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.data;
import static android.R.attr.id;
import static android.R.attr.y;

/**
 * Created by usi on 28/11/16.
 */

public class LocationDataExtractor implements LocationListener {
    private LocationManager locationManager;
    private HandlerThread locationHandlerThread;
    private long minTime;
    private float minDistance;
    private LocalDbController dbController;
    private Context context;

    public LocationDataExtractor(Context context, long minTime, float minDistance) {

        this.context = context;
        Log.d("LOCATION DATA", "STARTED");
        this.minTime = minTime;
        this.minDistance = minDistance;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationHandlerThread = new HandlerThread("LocationHandlerThread");
        locationHandlerThread.start();
        dbController = new LocalDbController(context, "JustMove");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this, locationHandlerThread.getLooper());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        HashMap<String, String> record = new HashMap<>();
        record.put(LocalSQLiteDBHelper.KEY_LOCATION_TIMESTAMP, Long.toString(location.getTime()));
        record.put(LocalSQLiteDBHelper.KEY_LOCATION_ID, null);
        record.put(LocalSQLiteDBHelper.KEY_LOCATION_LATITUDE, Double.toString(location.getLatitude()));
        record.put(LocalSQLiteDBHelper.KEY_LOCATION_LONGITUDE, Double.toString(location.getLongitude()));
        record.put(LocalSQLiteDBHelper.KEY_LOCATION_PROVIDER, location.getProvider());

        List<HashMap<String, String>> rList = new ArrayList<>();
        rList.add(record);
        dbController.insertRecords(LocalSQLiteDBHelper.TABLE_LOCATION, rList);
        Cursor c = dbController.rawQuery("SELECT * FROM " + LocalSQLiteDBHelper.TABLE_LOCATION, null);
        Log.d("COUNT", Integer.toString(c.getCount()));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void finalize() throws Throwable {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
            locationHandlerThread.quitSafely();
        }

    }
}
