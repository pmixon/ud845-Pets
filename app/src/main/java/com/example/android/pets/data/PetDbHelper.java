package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by phill on 1/17/2018.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    private static final String CREATE_DB = "CREATE TABLE pets (" +
            "_id INTEGER," +
            " name TEXT," +
            " breed TEXT," +
            " gender INTEGER," +
            " weight INTEGER)";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "shelter.db";


    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
