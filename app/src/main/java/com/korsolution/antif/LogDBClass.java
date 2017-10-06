package com.korsolution.antif;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Kontin58 on 9/3/2560.
 */

public class LogDBClass extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "logdb";

    // Table Name
    private static final String TABLE_LOG = "event_log";

    public LogDBClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_LOG +
                "(ID INTEGER PRIMARY KEY  NOT NULL  UNIQUE," +
                " HEADER VARCHAR," +
                " LATITUDE VARCHAR," +
                " LONGITUDE VARCHAR," +
                " COMMENT VARCHAR," +
                " CREATE_BY VARCHAR," +
                " CREATE_DATE VARCHAR);");

        Log.d("CREATE TABLE","Create Table Successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Insert Data
    public long Insert(String HEADER, String LATITUDE, String LONGITUDE, String COMMENT, String CREATE_BY, String CREATE_DATE) {
        // TODO Auto-generated method stub
        try {
            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            SQLiteStatement insertCmd;
            String strSQL = "INSERT INTO " + TABLE_LOG + "(HEADER, LATITUDE, LONGITUDE, COMMENT, CREATE_BY, CREATE_DATE) VALUES (?, ?, ?, ?, ?, ?)";
            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, HEADER);
            insertCmd.bindString(2, LATITUDE);
            insertCmd.bindString(3, LONGITUDE);
            insertCmd.bindString(4, COMMENT);
            insertCmd.bindString(5, CREATE_BY);
            insertCmd.bindString(6, CREATE_DATE);
            return insertCmd.executeInsert();

        } catch (Exception e) {
            return -1;
        }
    }

    // Select All Data Array 2 dimention
    public String[][] SelectAll() {
        // TODO Auto-generated method stub
        try {
            String arrData[][] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_LOG/* + " Where BlockId = '" + strBlockId + "'"*/;
            Cursor cursor = db.rawQuery(strSQL, null);

            if(cursor != null)
            {
                if (cursor.moveToFirst()) {
                    arrData = new String[cursor.getCount()][cursor.getColumnCount()];
                    /***
                     *  [x][0] = MemberID
                     *  [x][1] = Name
                     *  [x][2] = Tel
                     */
                    int i= 0;
                    do {
                        arrData[i][0] = cursor.getString(0);
                        arrData[i][1] = cursor.getString(1);
                        arrData[i][2] = cursor.getString(2);
                        arrData[i][3] = cursor.getString(3);
                        arrData[i][4] = cursor.getString(4);
                        arrData[i][5] = cursor.getString(5);
                        arrData[i][6] = cursor.getString(6);
                        i++;
                    } while (cursor.moveToNext());

                }
            }
            cursor.close();

            return arrData;

        } catch (Exception e) {
            return null;
        }
    }

    // Delete Data
    public long Delete(String logID) {
        // TODO Auto-generated method stub
        try {

            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            // for API 11 and above
            SQLiteStatement insertCmd;
            String strSQL = "DELETE FROM " + TABLE_LOG + " WHERE ID = ? ";

            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, logID);

            return insertCmd.executeUpdateDelete();

        } catch (Exception e) {
            return -1;
        }
    }
}
