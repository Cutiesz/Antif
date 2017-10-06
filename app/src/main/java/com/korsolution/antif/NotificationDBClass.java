package com.korsolution.antif;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Kontin58 on 14/3/2560.
 */

public class NotificationDBClass extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notidb";

    // Table Name
    private static final String TABLE_NOTI = "noti";

    public NotificationDBClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NOTI +
                "(ID INTEGER PRIMARY KEY  NOT NULL  UNIQUE," +
                " NOTI_ID VARCHAR," +
                " NOTI_NAME VARCHAR," +
                " READED VARCHAR," +
                " LATITUDE VARCHAR," +
                " LONGITUDE VARCHAR," +
                " PLACE VARCHAR," +
                " UPDATE_DATE VARCHAR);");

        Log.d("CREATE TABLE","Create Table Successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Insert Data
    public long Insert(String NOTI_ID, String NOTI_NAME, String READED, String LATITUDE, String LONGITUDE, String PLACE, String UPDATE_DATE) {
        // TODO Auto-generated method stub
        try {
            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            SQLiteStatement insertCmd;
            String strSQL = "INSERT INTO " + TABLE_NOTI + "(NOTI_ID, NOTI_NAME, READED, LATITUDE, LONGITUDE, PLACE, UPDATE_DATE) VALUES (?, ?, ?, ?, ?, ?, ?)";
            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, NOTI_ID);
            insertCmd.bindString(2, NOTI_NAME);
            insertCmd.bindString(3, READED);
            insertCmd.bindString(4, LATITUDE);
            insertCmd.bindString(5, LONGITUDE);
            insertCmd.bindString(6, PLACE);
            insertCmd.bindString(7, UPDATE_DATE);
            return insertCmd.executeInsert();

        } catch (Exception e) {
            return -1;
        }
    }

    // Update Data
    public long UpdateDataRead(String ID, String READED) {
        // TODO Auto-generated method stub

        try {

            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            //for API 11 and above
            SQLiteStatement insertCmd;
            String strSQL = "UPDATE " + TABLE_NOTI
                    + " SET READED = ? "
                    + " WHERE ID = ? ";

            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, READED);
            insertCmd.bindString(2, ID);

            return insertCmd.executeUpdateDelete();

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

            String strSQL = "SELECT  * FROM " + TABLE_NOTI/* + " Where BlockId = '" + strBlockId + "'"*/;
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
                        arrData[i][7] = cursor.getString(7);
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

    // Select All Data Array 2 dimention
    public String[][] SelectDataUnRead() {
        // TODO Auto-generated method stub
        try {
            String arrData[][] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_NOTI + " Where READED = '0'";
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
                        arrData[i][7] = cursor.getString(7);
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
    public long Delete(/*String logID*/) {
        // TODO Auto-generated method stub
        try {

            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            // for API 11 and above
            SQLiteStatement insertCmd;
            String strSQL = "DELETE FROM " + TABLE_NOTI/* + " WHERE ID = ? "*/;

            insertCmd = db.compileStatement(strSQL);
            //insertCmd.bindString(1, logID);

            return insertCmd.executeUpdateDelete();

        } catch (Exception e) {
            return -1;
        }
    }
}
