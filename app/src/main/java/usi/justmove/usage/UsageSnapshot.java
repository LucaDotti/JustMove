package usi.justmove.usage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;

import java.io.File;
import java.util.List;

import usi.justmove.database.LocalDbController;
import usi.justmove.database.LocalSQLiteDBHelper;

/**
 * Created by usi on 16/12/16.
 */

public class UsageSnapshot {
    private WifiUsage wifiUsage;
    private double gpsUsage;
    private double batteryUsage;
    private long memoryUsage;
    private Context context;

    public UsageSnapshot(Context context) {
        this.context = context;
    }

    public void takeSnaphot() {
        computeCurrentBatteryUsage();
        computeCurrentGpsUsage();
        computeCurrentMemoryUsage();
        computeCurrentWifiUsage();
    }

    private void computeCurrentWifiUsage() {
        PackageManager pm = context.getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        int uid = -1;

        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals("JustMove")){
                uid = packageInfo.uid;
                break;
            }
        }

        wifiUsage = new WifiUsage(TrafficStats.getUidRxBytes(uid), TrafficStats.getUidTxBytes(uid));
    }

    private void computeCurrentGpsUsage() {

    }

    private void computeCurrentBatteryUsage() {

    }

    private void computeCurrentMemoryUsage() {
        LocalSQLiteDBHelper dbHelper = new LocalSQLiteDBHelper(context);
        SQLiteDatabase sqliteDb = dbHelper.getReadableDatabase();
        LocalDbController db = new LocalDbController(context, "JustMove");
        Cursor c = db.rawQuery("pragma page_count", null);
        c.moveToNext();
        int page_count = c.getInt(0);
        c = db.rawQuery("pragma page_size", null);
        c.moveToNext();
        int page_size = c.getInt(0);
//        memoryUsage = new File(sqliteDb.getPath()).length();
        memoryUsage = page_count*page_size;

    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public WifiUsage getWifiUsage() {
        return wifiUsage;
    }
}