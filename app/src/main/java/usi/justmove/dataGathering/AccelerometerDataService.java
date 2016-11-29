package usi.justmove.dataGathering;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import usi.justmove.R;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

//put that in a asynchtask
//put service in manifest

// Define a HandlerThread
// Define a runnable (this will do the acc collection)
// For every ACC value (or a buffer) you compute X and depending on X you startService(Location)
// Execute the runnable from the HandlerThread

public class AccelerometerDataService extends Service implements SensorEventListener {
    private HandlerThread sensorThreadHandler;
    private Handler sensorHandler;
    private SensorManager sensorManager;
    private Sensor accSensor;

    @Override
    public void onCreate() {
        super.onCreate();
        
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorThreadHandler = new HandlerThread("AccelerometerThread", Thread.MAX_PRIORITY);
        sensorThreadHandler.start();
        sensorHandler = new Handler(sensorThreadHandler.getLooper());
        sensorManager.registerListener(this, accSensor, SENSOR_DELAY_NORMAL, sensorHandler);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        sensorThreadHandler.quitSafely();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("X", Float.toString(event.values[0]));
        Log.d("Y", Float.toString(event.values[1]));
        Log.d("Z", Float.toString(event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
