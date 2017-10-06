package com.korsolution.antif;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Kontin58 on 1/11/2559.
 */

public class VehicleBrandDBClass extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "vehiclebranddb";

    // Table Name
    private static final String TABLE_VEHICLE = "vehicle_brand";

    public VehicleBrandDBClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VEHICLE +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " VEHICLE_BRAND_ID TEXT(100)," +
                " VEHICLE_BRAND_NAME TEXT(100)," +
                " VEHICLE_BRAND_ICON TEXT(100));");

        Log.d("CREATE TABLE","Create Table Successfully.");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // Insert Data
    public long Insert(String strVEHICLE_BRAND_ID, String strVEHICLE_BRAND_NAME, String strVEHICLE_BRAND_ICON) {
        // TODO Auto-generated method stub

        try {
            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            SQLiteStatement insertCmd;
            String strSQL = "INSERT INTO " + TABLE_VEHICLE
                    + "(VEHICLE_BRAND_ID, VEHICLE_BRAND_NAME, VEHICLE_BRAND_ICON) VALUES (?, ?, ?)";
            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, strVEHICLE_BRAND_ID);
            insertCmd.bindString(2, strVEHICLE_BRAND_NAME);
            insertCmd.bindString(3, strVEHICLE_BRAND_ICON);
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

            String strSQL = "SELECT  * FROM " + TABLE_VEHICLE/* + " Where BlockId = '" + strBlockId + "'"*/;
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

    public String[] SelectName() {
        // TODO Auto-generated method stub
        try {
            String arrData[] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_VEHICLE/* + " Where BlockId = '" + strBlockId + "'"*/;
            Cursor cursor = db.rawQuery(strSQL, null);

            if(cursor != null) {
                if (cursor.moveToFirst()) {
                    arrData = new String[cursor.getCount()];
                    /***
                     *  [x][0] = MemberID
                     *  [x][1] = Name
                     *  [x][2] = Tel
                     */
                    int i= 0;
                    do {
                        arrData[i] = cursor.getString(2);
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

    public String[][] SelectAllByName(String strVEHICLE_BRAND_NAME) {
        // TODO Auto-generated method stub

        try {
            String arrData[][] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_VEHICLE + " Where VEHICLE_BRAND_NAME = '" + strVEHICLE_BRAND_NAME + "'";
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
    public long Delete(/*String strMemberID*/) {
        // TODO Auto-generated method stub

        try {

            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            // for API 11 and above
            SQLiteStatement insertCmd;
            String strSQL = "DELETE FROM " + TABLE_VEHICLE/* + " WHERE MemberID = ? "*/;

            insertCmd = db.compileStatement(strSQL);
            /*insertCmd.bindString(1, strMemberID);*/

            return insertCmd.executeUpdateDelete();

        } catch (Exception e) {
            return -1;
        }

    }
}
