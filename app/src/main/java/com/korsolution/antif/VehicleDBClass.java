package com.korsolution.antif;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Kontin58 on 24/9/2559.
 */

public class VehicleDBClass extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "vehicledb";

    // Table Name
    private static final String TABLE_VEHICLE = "vehicle";

    public VehicleDBClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VEHICLE +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " IMEI TEXT(100)," +
                " VEHICLE_DISPLAY TEXT(100)," +
                " VEHICLE_NAME TEXT(100)," +
                " HISTORY_DATETIME TEXT(100)," +
                " LATITUDE TEXT(100)," +
                " LONGITUDE TEXT(100)," +
                " PLACE TEXT(100)," +
                " ANGLE TEXT(100)," +
                " SPEED TEXT(100)," +
                " STATUS TEXT(100)," +
                " IS_CUT_ENGINE TEXT(100)," +
                " IS_IQNITION TEXT(100)," +
                " IS_AUTHEN TEXT(100)," +
                " SIM TEXT(100)," +
                " TEL_EMERGING_1 TEXT(100)," +
                " TEL_EMERGING_2 TEXT(100)," +
                " TEL_EMERGING_3 TEXT(100)," +
                " IS_UNPLUG_GPS TEXT(100)," +
                " GSM_SIGNAL TEXT(100)," +
                " NUM_SAT TEXT(100)," +
                " CAR_IMAGE_FRONT TEXT(100)," +
                " CAR_IMAGE_BACK TEXT(100)," +
                " CAR_IMAGE_LEFT TEXT(100)," +
                " CAR_IMAGE_RIGHT TEXT(100)," +
                " STATUS_UPDATE TEXT(100)," +
                " VEHICLE_TYPE_NAME TEXT(100)," +
                " VEHICLE_BRAND_NAME TEXT(100)," +
                " MODEL TEXT(100)," +
                " YEAR TEXT(100)," +
                " VEHICLE_COLOR TEXT(100));");

        Log.d("CREATE TABLE","Create Table Successfully.");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // Insert Data
    public long Insert(String strIMEI, String strVEHICLE_DISPLAY, String strVEHICLE_NAME,
                       String strHISTORY_DATETIME, String strLATITUDE, String strLONGITUDE,
                       String strPLACE, String strANGLE, String strSPEED, String strSTATUS,
                       String strIS_CUT_ENGINE, String strIS_IQNITION, String strIS_AUTHEN, String strSIM,
                       String strTEL_EMERGING_1, String strTEL_EMERGING_2, String strTEL_EMERGING_3,
                       String strIS_UNPLUG_GPS, String strGSM_SIGNAL, String strNUM_SAT,
                       String strCAR_IMAGE_FRONT, String strCAR_IMAGE_BACK, String strCAR_IMAGE_LEFT,
                       String strCAR_IMAGE_RIGHT, String strSTATUS_UPDATE,
                       String strVEHICLE_TYPE_NAME, String strVEHICLE_BRAND_NAME, String strMODEL, String strYEAR, String strVEHICLE_COLOR) {
        // TODO Auto-generated method stub

        try {
            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            SQLiteStatement insertCmd;
            String strSQL = "INSERT INTO " + TABLE_VEHICLE
                    + "(IMEI, VEHICLE_DISPLAY, VEHICLE_NAME, HISTORY_DATETIME, LATITUDE, LONGITUDE, PLACE, ANGLE, SPEED, STATUS, IS_CUT_ENGINE, IS_IQNITION, IS_AUTHEN, SIM, TEL_EMERGING_1, TEL_EMERGING_2, TEL_EMERGING_3, IS_UNPLUG_GPS, GSM_SIGNAL, NUM_SAT, CAR_IMAGE_FRONT, CAR_IMAGE_BACK, CAR_IMAGE_LEFT, CAR_IMAGE_RIGHT, STATUS_UPDATE, VEHICLE_TYPE_NAME, VEHICLE_BRAND_NAME, MODEL, YEAR, VEHICLE_COLOR) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, strIMEI);
            insertCmd.bindString(2, strVEHICLE_DISPLAY);
            insertCmd.bindString(3, strVEHICLE_NAME);
            insertCmd.bindString(4, strHISTORY_DATETIME);
            insertCmd.bindString(5, strLATITUDE);
            insertCmd.bindString(6, strLONGITUDE);
            insertCmd.bindString(7, strPLACE);
            insertCmd.bindString(8, strANGLE);
            insertCmd.bindString(9, strSPEED);
            insertCmd.bindString(10, strSTATUS);
            insertCmd.bindString(11, strIS_CUT_ENGINE);
            insertCmd.bindString(12, strIS_IQNITION);
            insertCmd.bindString(13, strIS_AUTHEN);
            insertCmd.bindString(14, strSIM);
            insertCmd.bindString(15, strTEL_EMERGING_1);
            insertCmd.bindString(16, strTEL_EMERGING_2);
            insertCmd.bindString(17, strTEL_EMERGING_3);
            insertCmd.bindString(18, strIS_UNPLUG_GPS);
            insertCmd.bindString(19, strGSM_SIGNAL);
            insertCmd.bindString(20, strNUM_SAT);
            insertCmd.bindString(21, strCAR_IMAGE_FRONT);
            insertCmd.bindString(22, strCAR_IMAGE_BACK);
            insertCmd.bindString(23, strCAR_IMAGE_LEFT);
            insertCmd.bindString(24, strCAR_IMAGE_RIGHT);
            insertCmd.bindString(25, strSTATUS_UPDATE);
            insertCmd.bindString(26, strVEHICLE_TYPE_NAME);
            insertCmd.bindString(27, strVEHICLE_BRAND_NAME);
            insertCmd.bindString(28, strMODEL);
            insertCmd.bindString(29, strYEAR);
            insertCmd.bindString(30, strVEHICLE_COLOR);
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
                        arrData[i][4] = cursor.getString(4);
                        arrData[i][5] = cursor.getString(5);
                        arrData[i][6] = cursor.getString(6);
                        arrData[i][7] = cursor.getString(7);
                        arrData[i][8] = cursor.getString(8);
                        arrData[i][9] = cursor.getString(9);
                        arrData[i][10] = cursor.getString(10);
                        arrData[i][11] = cursor.getString(11);
                        arrData[i][12] = cursor.getString(12);
                        arrData[i][13] = cursor.getString(13);
                        arrData[i][14] = cursor.getString(14);
                        arrData[i][15] = cursor.getString(15);
                        arrData[i][16] = cursor.getString(16);
                        arrData[i][17] = cursor.getString(17);
                        arrData[i][18] = cursor.getString(18);
                        arrData[i][19] = cursor.getString(19);
                        arrData[i][20] = cursor.getString(20);
                        arrData[i][21] = cursor.getString(21);
                        arrData[i][22] = cursor.getString(22);
                        arrData[i][23] = cursor.getString(23);
                        arrData[i][24] = cursor.getString(24);
                        arrData[i][25] = cursor.getString(25);
                        arrData[i][26] = cursor.getString(26);
                        arrData[i][27] = cursor.getString(27);
                        arrData[i][28] = cursor.getString(28);
                        arrData[i][29] = cursor.getString(29);
                        arrData[i][30] = cursor.getString(30);
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
    public String[][] SelectAllByVehicleName(String _VEHICLE_NAME) {
        // TODO Auto-generated method stub

        try {
            String arrData[][] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_VEHICLE + " Where VEHICLE_NAME = '" + _VEHICLE_NAME + "'";
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
                        arrData[i][8] = cursor.getString(8);
                        arrData[i][9] = cursor.getString(9);
                        arrData[i][10] = cursor.getString(10);
                        arrData[i][11] = cursor.getString(11);
                        arrData[i][12] = cursor.getString(12);
                        arrData[i][13] = cursor.getString(13);
                        arrData[i][14] = cursor.getString(14);
                        arrData[i][15] = cursor.getString(15);
                        arrData[i][16] = cursor.getString(16);
                        arrData[i][17] = cursor.getString(17);
                        arrData[i][18] = cursor.getString(18);
                        arrData[i][19] = cursor.getString(19);
                        arrData[i][20] = cursor.getString(20);
                        arrData[i][21] = cursor.getString(21);
                        arrData[i][22] = cursor.getString(22);
                        arrData[i][23] = cursor.getString(23);
                        arrData[i][24] = cursor.getString(24);
                        arrData[i][25] = cursor.getString(25);
                        arrData[i][26] = cursor.getString(26);
                        arrData[i][27] = cursor.getString(27);
                        arrData[i][28] = cursor.getString(28);
                        arrData[i][29] = cursor.getString(29);
                        arrData[i][30] = cursor.getString(30);
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
    public String[][] SelectAllByVehicleDisplay(String _VEHICLE_DISPLAY) {
        // TODO Auto-generated method stub

        try {
            String arrData[][] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_VEHICLE + " Where VEHICLE_DISPLAY = '" + _VEHICLE_DISPLAY + "'";
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
                        arrData[i][8] = cursor.getString(8);
                        arrData[i][9] = cursor.getString(9);
                        arrData[i][10] = cursor.getString(10);
                        arrData[i][11] = cursor.getString(11);
                        arrData[i][12] = cursor.getString(12);
                        arrData[i][13] = cursor.getString(13);
                        arrData[i][14] = cursor.getString(14);
                        arrData[i][15] = cursor.getString(15);
                        arrData[i][16] = cursor.getString(16);
                        arrData[i][17] = cursor.getString(17);
                        arrData[i][18] = cursor.getString(18);
                        arrData[i][19] = cursor.getString(19);
                        arrData[i][20] = cursor.getString(20);
                        arrData[i][21] = cursor.getString(21);
                        arrData[i][22] = cursor.getString(22);
                        arrData[i][23] = cursor.getString(23);
                        arrData[i][24] = cursor.getString(24);
                        arrData[i][25] = cursor.getString(25);
                        arrData[i][26] = cursor.getString(26);
                        arrData[i][27] = cursor.getString(27);
                        arrData[i][28] = cursor.getString(28);
                        arrData[i][29] = cursor.getString(29);
                        arrData[i][30] = cursor.getString(30);
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

    public String[] SelectVehicleName() {
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
                        arrData[i] = cursor.getString(3);
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
