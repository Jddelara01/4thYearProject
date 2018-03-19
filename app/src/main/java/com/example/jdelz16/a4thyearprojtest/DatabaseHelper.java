package com.example.jdelz16.a4thyearprojtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

/**
 * Created by jdelz16 on 08/11/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "Test.db", factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE MAPS (ID INTEGER PRIMARY KEY AUTOINCREMENT, Latitude Text, Longtitude Text)";
        String createUIDTable = "CREATE TABLE ITEMS (ID INTEGER PRIMARY KEY AUTOINCREMENT, Item Text)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS MAPS;");
        db.execSQL("DROP TABLE IF EXISTS ITEMS;");
        onCreate(db);
    }

    /*public void insert_location(String lat, String lng) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("Latitude", lat);
        contentValues.put("Longtitude", lng);

        this.getWritableDatabase().insert("MAPS", null, contentValues);
    }*/

    public void insert_UserID(String uID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("Item", uID);

        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM ITEMS", null);
        if(cursor.getCount() == 0) {
            this.getWritableDatabase().insert("ITEMS", null, contentValues);
        } else {
            this.getWritableDatabase().execSQL("UPDATE ITEMS SET Item='" + uID + "'");
        }
    }

    public String getItem() {
        String result = "";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ITEMS", null);

        if(cursor.getCount() == 0) {
            result     = "No Data Found";
        }else {
            while(cursor.moveToNext()) {
                result = cursor.getString(1);
            }
        }
        return result;
    }

    public void insert_location(String lat, String lng) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("Latitude", lat);
        contentValues.put("Longtitude", lng);

        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM MAPS", null);

        if(cursor.getCount() == 0) {
            this.getWritableDatabase().insert("MAPS", null, contentValues);
        } else {
            this.getWritableDatabase().execSQL("UPDATE MAPS SET Latitude='" + lat + "', Longtitude= '" + lng + "'");
        }
    }

    public String list_locations() {
        String result = "";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM MAPS", null);

        if(cursor.getCount() == 0) {
            result     = "No Data Found";
        }else {
            while(cursor.moveToNext()) {
                result = cursor.getString(1)+ ", " +cursor.getString(2)+ ", " +cursor.getString(0);
            }
        }
        return result;
    }

    public String listsGPS() {
        String result = " ";
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM MAPS", null);

        //textView.setText("");
        while (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        return result;
    }
}
