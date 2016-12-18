package usi.justmove.dataAnalisys;

import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usi on 11/12/16.
 */

public class DataAnalyzer {

    public Trace computeSpeedPath(Cursor records, int speedDiffThreshold, boolean cleanErrors, int speedDiffErrorTreshold) {
        Trace sp = new Trace();
        List<LatLng> currPath = new ArrayList<>();
        List<Long> currPathTimes = new ArrayList<>();

        double currSpeed = 0;
        double tempSpeed = 0;
        long prevTime = 0;
        long currTime = 0;
        LatLng currPoint;
        LatLng prevPoint;

        records.moveToNext();

        prevPoint = new LatLng(records.getDouble(3), records.getDouble(4));
        prevTime = records.getLong(1);

        records.moveToNext();

        currPoint = new LatLng(records.getDouble(3), records.getDouble(4));
        currTime = records.getLong(1);

        currSpeed = computeSpeed(prevPoint, currPoint, prevTime, currTime);

        currPath.add(prevPoint);
        currPathTimes.add(prevTime);
        currPath.add(currPoint);
        currPathTimes.add(currTime);

        while(records.moveToNext()) {
            prevPoint = currPoint;
            prevTime = currTime;

            currPoint = new LatLng(records.getDouble(3), records.getDouble(4));
            currTime = records.getLong(1);

            tempSpeed = computeSpeed(prevPoint, currPoint, prevTime, currTime);

            if(tempSpeed >= currSpeed-speedDiffThreshold && tempSpeed <= currSpeed+speedDiffThreshold) {
                currPath.add(currPoint);
                currPathTimes.add(currTime);
            } else {
                if(cleanErrors && (Math.abs(tempSpeed-currSpeed) >= speedDiffErrorTreshold || tempSpeed <= 0)) {
                    currPath.add(currPoint);
                    currPathTimes.add(currTime);
                } else {
                    sp.addSubPath(currPath, currPathTimes, (int) currSpeed);
                    currPath = new ArrayList<>();
                    currPathTimes = new ArrayList<>();
                    currPath.add(prevPoint);
                    currPath.add(currPoint);
                    currPathTimes.add(prevTime);
                    currPathTimes.add(currTime);
                    currSpeed = tempSpeed;
                }
            }
        }

        return sp;
    }

    private double computeSpeed(LatLng start, LatLng end, long tsStart, long tsEnd) {
        long tDiff = (tsEnd-tsStart)/1000;
        Location l1 = new Location("");
        l1.setLatitude(start.latitude);
        l1.setLongitude(start.longitude);
        Location l2 = new Location("");
        l2.setLatitude(end.latitude);
        l2.setLongitude(end.longitude);

        double distance = l1.distanceTo(l2);
        if(tDiff == 0) {
            return 0;
        }

        double speedMs = distance/tDiff;

        return (speedMs/1000)*3600;
    }
}
