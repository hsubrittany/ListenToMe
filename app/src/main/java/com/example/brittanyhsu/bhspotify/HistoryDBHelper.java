package com.example.brittanyhsu.bhspotify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by brittanyhsu on 9/4/17.
 */

public class HistoryDBHelper extends SQLiteOpenHelper {
    String TAG = "HistoryDBHelper";

    // if search is success, insert data

    public static final String DATABASE_NAME = "history.db";
    public static final String TABLE_NAME = "history_table";
    public static final String COL_1 = "ID";

    public static final String COL_2 = "TITLE";
    public static final String COL_3 = "ARTIST";
    // add in album art later



    public HistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "  (ID INTEGER PRIMARY KEY AUTOINCREMENT,TITLE TEXT,ARTIST TEXT) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String title, String artist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,title);
        contentValues.put(COL_3,artist);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if(result == -1) return false;

        Log.d(TAG, "Inserted " + title + " " + artist);
        return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return res;
    }
}
