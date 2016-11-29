package usi.justmove.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by usi on 27/11/16.
 */

public abstract class AbstractDbController implements DbController {
    protected String dbName;
    protected Context context;
}

interface DbController {
    Cursor rawQuery(String query, String[] args);
    void truncateTable(String tableName);
    void insertRecords(String tableName, List<HashMap<String, String>> records);
}



