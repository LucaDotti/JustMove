package usi.justmove.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.version;

/**
 * Created by usi on 27/11/16.
 */

public class LocalSQLiteDBHelper extends SQLiteOpenHelper {
    //db information
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "JustMove";

    //table names
    public static final String TABLE_LOCATION = "location";
    public static final String TABLE_WIFI = "wifi";
    public static final String TABLE_ACCELEROMETER = "accelerometer";

    //columns names of location table
    public static final String KEY_LOCATION_ID = "id_location";
    public static final String KEY_LOCATION_TIMESTAMP = "ts";
    public static final String KEY_LOCATION_PROVIDER = "provider";
    public static final String KEY_LOCATION_LATITUDE = "latitude";
    public static final String KEY_LOCATION_LONGITUDE = "longitude";

    //columns names of wifi table
    public static final String KEY_WIFI_ID = "id_wifi";
    public static final String KEY_WIFI_TIMESTAMP = "ts";
    public static final String KEY_WIFI_SSID = "ssid";
    public static final String KEY_WIFI_FREQ = "freq";
    public static final String KEY_WIFI_LEVEL = "level";

    //columns names of wifi table
    public static final String KEY_ACCELEROMETER_ID = "id_accelerometer";
    public static final String KEY_ACCELEROMETER_TIMESTAMP = "ts";
    public static final String KEY_ACCELEROMETER_X = "x";
    public static final String KEY_ACCELEROMETER_Y = "y";
    public static final String KEY_ACCELEROMETER_Z = "z";


    public LocalSQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //check table exists
        String CREATE_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION +
                "(" +
                KEY_LOCATION_ID + " INTEGER PRIMARY KEY," +
                KEY_LOCATION_TIMESTAMP + " INTEGER DEFAULT CURRENT_TIMESTAMP, " +
                KEY_LOCATION_PROVIDER + " TEXT, " +
                KEY_LOCATION_LATITUDE + " REAL NOT NULL, " +
                KEY_LOCATION_LONGITUDE + " REAL NOT NULL " +
                ")";

        String CREATE_WIFI_TABLE = "CREATE TABLE " + TABLE_WIFI +
                "(" +
                KEY_WIFI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_WIFI_TIMESTAMP + " INTEGER DEFAULT CURRENT_TIMESTAMP, " +
                KEY_WIFI_SSID + " TEXT NOT NULL, " +
                KEY_WIFI_FREQ + " INTEGER, " +
                KEY_WIFI_LEVEL + " INTEGER " +
                ")";

        String CREATE_ACCELEROMETER_TABLE = "CREATE TABLE " + TABLE_ACCELEROMETER +
                "(" +
                KEY_ACCELEROMETER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ACCELEROMETER_TIMESTAMP + " INTEGER DEFAULT CURRENT_TIMESTAMP, " +
                KEY_ACCELEROMETER_X + " REAL NOT NULL, " +
                KEY_ACCELEROMETER_Y + " REAL NOT NULL, " +
                KEY_ACCELEROMETER_Z + " REAL NOT NULL " +
                ")";

        db.execSQL(CREATE_LOCATION_TABLE);
//        db.execSQL(CREATE_WIFI_TABLE);
//        db.execSQL(CREATE_ACCELEROMETER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        return;
    }
}
