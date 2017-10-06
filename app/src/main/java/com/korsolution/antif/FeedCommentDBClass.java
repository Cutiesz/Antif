package com.korsolution.antif;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Kontin58 on 7/3/2560.
 */

public class FeedCommentDBClass extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "feedcommentdb";

    // Table Name
    private static final String TABLE_FEED_COMMENT = "feedcomment";

    public FeedCommentDBClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_FEED_COMMENT +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " FEED_HEADER_ID TEXT(100)," +
                " FEED_COMMENT_ID TEXT(100)," +
                " COMMENT TEXT(100)," +
                " DISPLAY_NAME TEXT(100)," +
                " USER_PICTURE TEXT(100)," +
                " CREATE_DATE TEXT(100));");

        Log.d("CREATE TABLE","Create Table Successfully.");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Insert Data
    public long Insert(String strFEED_HEADER_ID, String strFEED_COMMENT_ID, String strCOMMENT,
                       String strDISPLAY_NAME, String strUSER_PICTURE, String strCREATE_DATE) {
        // TODO Auto-generated method stub

        try {
            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            SQLiteStatement insertCmd;
            String strSQL = "INSERT INTO " + TABLE_FEED_COMMENT
                    + "(FEED_HEADER_ID, FEED_COMMENT_ID, COMMENT, DISPLAY_NAME, USER_PICTURE, CREATE_DATE) VALUES (?, ?, ?, ?, ?, ?)";
            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, strFEED_HEADER_ID);
            insertCmd.bindString(2, strFEED_COMMENT_ID);
            insertCmd.bindString(3, strCOMMENT);
            insertCmd.bindString(4, strDISPLAY_NAME);
            insertCmd.bindString(5, strUSER_PICTURE);
            insertCmd.bindString(6, strCREATE_DATE);
            return insertCmd.executeInsert();

        } catch (Exception e) {
            return -1;
        }
    }

    // Select All Data Array 2 dimention
    public String[][] SelectAll(String strFEED_HEADER_ID) {
        // TODO Auto-generated method stub

        try {
            String arrData[][] = null;
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // Read Data

            String strSQL = "SELECT  * FROM " + TABLE_FEED_COMMENT + " Where FEED_HEADER_ID = '" + strFEED_HEADER_ID + "'";
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
    public long Delete(String strFEED_HEADER_ID) {
        // TODO Auto-generated method stub

        try {

            SQLiteDatabase db;
            db = this.getWritableDatabase(); // Write Data

            // for API 11 and above
            SQLiteStatement insertCmd;
            String strSQL = "DELETE FROM " + TABLE_FEED_COMMENT + " WHERE FEED_HEADER_ID = ? ";

            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1, strFEED_HEADER_ID);

            return insertCmd.executeUpdateDelete();

        } catch (Exception e) {
            return -1;
        }

    }
}
