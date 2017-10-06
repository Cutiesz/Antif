package com.korsolution.antif;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Kontin58 on 3/5/2559.
 */
public class AccountDBClass extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "accountdb";

    // Table Name
    private static final String TABLE_ACCOUNT = "account";

    public AccountDBClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_ACCOUNT +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " USER_ID TEXT(100)," +
                " PERMISSION_ID TEXT(100)," +
                " EMAIL TEXT(100)," +
                " PASSWORD TEXT(100)," +
                " DISPLAY_NAME TEXT(100)," +
                " FIRST_NAME TEXT(100)," +
                " LAST_NAME TEXT(100)," +
                " TEL TEXT(100)," +
                " LOGIN_TYPE TEXT(100)," +
                " ID_CARD TEXT(100)," +
                " LICENSE_EXP TEXT(100)," +
                " USER_PICTURE TEXT(100)," +
                " ADDRESS TEXT(100)," +
                " District_ID TEXT(100)," +
                " District_Name TEXT(100)," +
                " Amphur_ID TEXT(100)," +
                " Amphur_Name TEXT(100)," +
                " Province_ID TEXT(100)," +
                " Province_Name TEXT(100)," +
                " POSTCODE TEXT(100)," +
                " LATITUDE_ADDRESS TEXT(100)," +
                " LONGITUDE_ADDRESS TEXT(100));");

        Log.d("CREATE TABLE","Create Table Successfully.");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Insert Data
    public long InsertAccount(String strUSER_ID, String strPERMISSION_ID, String strEMAIL,
                              String strPASSWORD, String strDISPLAY_NAME, String strFIRST_NAME,
                              String strLAST_NAME, String strTEL, String strLOGIN_TYPE,
                              String strID_CARD, String strLICENSE_EXP, String strUSER_PICTURE,
                              String strADDRESS, String strDistrict_ID, String strDistrict_Name,
                              String strAmphur_ID, String strAmphur_Name, String strProvince_ID,
                              String strProvince_Name, String strPOSTCODE,
                              String strLATITUDE_ADDRESS, String strLONGITUDE_ADDRESS) {
        // TODO Auto-generated method stub

        try {
            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            SQLiteStatement insertCmd;
            String strSQL = "INSERT INTO " + TABLE_ACCOUNT
                    + " (USER_ID, PERMISSION_ID, EMAIL, PASSWORD, DISPLAY_NAME, FIRST_NAME, LAST_NAME, TEL, LOGIN_TYPE, ID_CARD, LICENSE_EXP, USER_PICTURE, ADDRESS, District_ID, District_Name, Amphur_ID, Amphur_Name, Province_ID, Province_Name, POSTCODE, LATITUDE_ADDRESS, LONGITUDE_ADDRESS) "
                    + "  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, strUSER_ID);
            insertCmd.bindString(2, strPERMISSION_ID);
            insertCmd.bindString(3, strEMAIL);
            insertCmd.bindString(4, strPASSWORD);
            insertCmd.bindString(5, strDISPLAY_NAME);
            insertCmd.bindString(6, strFIRST_NAME);
            insertCmd.bindString(7, strLAST_NAME);
            insertCmd.bindString(8, strTEL);
            insertCmd.bindString(9, strLOGIN_TYPE);
            insertCmd.bindString(10, strID_CARD);
            insertCmd.bindString(11, strLICENSE_EXP);
            insertCmd.bindString(12, strUSER_PICTURE);
            insertCmd.bindString(13, strADDRESS);
            insertCmd.bindString(14, strDistrict_ID);
            insertCmd.bindString(15, strDistrict_Name);
            insertCmd.bindString(16, strAmphur_ID);
            insertCmd.bindString(17, strAmphur_Name);
            insertCmd.bindString(18, strProvince_ID);
            insertCmd.bindString(19, strProvince_Name);
            insertCmd.bindString(20, strPOSTCODE);
            insertCmd.bindString(21, strLATITUDE_ADDRESS);
            insertCmd.bindString(22, strLONGITUDE_ADDRESS);
            return insertCmd.executeInsert();

        } catch (Exception e) {
            return -1;
        }
    }

    // Select All Data Array 2 dimention
    public String[][] SelectAllAccount() {
        // TODO Auto-generated method stub

        try {
            String arrData[][] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_ACCOUNT/* + " Where BlockId = '" + strBlockId + "'"*/;
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
    public long DeleteAccount(/*String strMemberID*/) {
        // TODO Auto-generated method stub

        try {

            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            // for API 11 and above
            SQLiteStatement insertCmd;
            String strSQL = "DELETE FROM " + TABLE_ACCOUNT/* + " WHERE MemberID = ? "*/;

            insertCmd = db.compileStatement(strSQL);
            /*insertCmd.bindString(1, strMemberID);*/

            return insertCmd.executeUpdateDelete();

        } catch (Exception e) {
            return -1;
        }

    }
}
