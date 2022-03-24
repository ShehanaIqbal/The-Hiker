package com.example.hiker.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DataBaseHandler  extends SQLiteOpenHelper {
    public Context context;
    public static final String DATABASE_NAME = "hiker";

    public static final int DATABASE_VERSION = 1;
    public static final String USER_TABLE_NAME = "user";
    public static final String TRAIL_TABLE_NAME = "trail";
    public static final String TRAIL_BLOB_TABLE_NAME = "trail_blob";
    public static final String TRAIL_COMMENTS_TABLE_NAME = "trail_comments";


    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        Log.d("sqlLite","database created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(CREATE_TABLE); //TODO : implement create table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);//TODO : implement drop table
        onCreate(db);
    }

}
