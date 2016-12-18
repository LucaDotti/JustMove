package usi.justmove.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Created by Luca Dotti on 29/11/16.
 */
public class LocalDbController extends AbstractDbController {
    private SQLiteDatabase sqliteDb;
    private LocalSQLiteDBHelper dbHelper;

    public LocalDbController(Context context, String dbName) {
        this.context = context;
        this.dbName = dbName;
        dbHelper = new LocalSQLiteDBHelper(context);
        sqliteDb = dbHelper.getReadableDatabase();
    }

    @Override
    public Cursor rawQuery(String query, String[] args) {
        Cursor cursor = sqliteDb.rawQuery(query, args);
//        Log.d("DB COUNT", Integer.toString(cursor.getCount()));
        return cursor;
    }

    @Override
    public void truncateTable(String tableName) {

    }

    @Override
    public void insertRecords(String tableName, List<HashMap<String, String>> records) {
        ContentValues values;

        for(HashMap<String, String> record: records) {
            values = new ContentValues();
            for(Map.Entry<String, String> entry: record.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
            sqliteDb.insert(LocalSQLiteDBHelper.TABLE_LOCATION, null, values);
        }
    }

    /**
     * Simple db call to insert 'true' (1) parameter to database
     */
    public void insertControlTrue(){
        try {
            sqliteDb.execSQL("INSERT INTO data (data_type) Values (1)");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to check if user agreed
     * @return double qotes if not agreed else 1
     */
    public String getIfAgreed(){

        String checkAgreed = "";

        try {
            String selectQuery = "SELECT data_type FROM data WHERE data_type==1";
            Cursor c = sqliteDb.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                checkAgreed = c.getString(c.getColumnIndex("data_type"));
            }
            c.close();

        } catch (Exception e){

        }

        return checkAgreed;
    }

    /**
     * Helper method to initialize database from desired fragment
     */
    public void initialize(){
        dbHelper.onCreate(sqliteDb);
    }
}

