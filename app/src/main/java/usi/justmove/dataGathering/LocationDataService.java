package usi.justmove.dataGathering;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

/**
 * Created by Luca Dotti on 28/11/16.
 */
public class LocationDataService extends Service {
    private LocationDataExtractor dataExtractor;

    public LocationDataService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataExtractor = new LocationDataExtractor(getApplicationContext(), 1000, 5);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
