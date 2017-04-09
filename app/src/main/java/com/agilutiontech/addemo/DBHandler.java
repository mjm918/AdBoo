package com.agilutiontech.addemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.agilutiontech.addemo.common.Addvertise;

import java.util.ArrayList;
import java.util.List;


public class DBHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "Addvertisement";
    // Contacts table name
    private static final String TABLE_SHOPS = "Addvertise";
    // Shops Table Columns names
    private static final String KEY_INDEX = "ixid";
    private static final String KEY_TIME = "time";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SHOPS + "("
                + KEY_INDEX + " TEXT,"
                + KEY_TIME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPS);
// Creating tables again
        onCreate(db);
    }

    // Adding new Ads
    public void insertAdd(Addvertise shop) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INDEX, shop.getIndex()); // Addvertise Name
        values.put(KEY_TIME, shop.getTime()); // Addvertise Phone Number

// Inserting Row
        db.insert(TABLE_SHOPS, null, values);
        db.close(); // Closing database connection
    }



    // Getting All Ads
    public List<Addvertise> getAllAds() {
        List<Addvertise> AdList = new ArrayList<Addvertise>();
// Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_SHOPS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Addvertise Ad = new Addvertise();
                Ad.setIndex(cursor.getString(cursor.getColumnIndex(KEY_INDEX)));
                Ad.setTime(cursor.getString(cursor.getColumnIndex(KEY_TIME)));
// Adding Ad to list
                AdList.add(Ad);
            } while (cursor.moveToNext());
        }

// return contact list
        return AdList;
    }



    // Deleting a Ads
    public void removeAdsFromDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_SHOPS);
        db.close();
    }
}