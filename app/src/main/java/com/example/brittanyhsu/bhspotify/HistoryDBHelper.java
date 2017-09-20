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
    public static final String COL_4 = "ALBUM";
    public static final String COL_5 = "URI";




    public HistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "  (ID INTEGER PRIMARY KEY AUTOINCREMENT,TITLE TEXT,ARTIST TEXT,ALBUM TEXT,URI TEXT) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String title, String artist, String albumArtUrl, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,title);
        contentValues.put(COL_3,artist);
        contentValues.put(COL_4,albumArtUrl);
        contentValues.put(COL_5,uri);

        long result = db.insert(TABLE_NAME,null,contentValues);
        if(result == -1) return false;

        Log.d(TAG, "Inserted to DB: " + title + " " + artist + "\n URL: " + albumArtUrl + "\n URI: " + uri);
        return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return res;
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public boolean existsInTable(String title, String artist) {
        Cursor res = getAllData();
        if(res.getCount() == 0) {
            return false;
        }

        while(res.moveToNext()) {
            if(res.getString(1).equals(title) && res.getString(2).equals(artist)) {
                Log.d(TAG,"FOUND IN DB : " + title + "  "+ artist);
                return true;
            }
        }
        res.close();
        return false;
    }


    public void deleteRow(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COL_2+ "='"+title+"'");

    }
}
