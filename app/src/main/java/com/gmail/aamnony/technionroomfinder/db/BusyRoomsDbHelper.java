package com.gmail.aamnony.technionroomfinder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gmail.aamnony.technionroomfinder.db.BusyRoomsContract.Columns;
import com.gmail.aamnony.technionroomfinder.pojos.BusyRoom;

import java.util.List;

/**
 * https://developer.android.com/training/basics/data-storage/databases.html
 */
public class BusyRoomsDbHelper extends SQLiteOpenHelper
{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BusyRooms.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String TINYINT_TYPE = " TINYINT";
    private static final String TEXT_TYPE = " TEXT";

    public static final String COMMA = ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + Columns.TABLE_NAME + " (" +
            Columns._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA +
            Columns.BUILDING + TEXT_TYPE + COMMA +
            Columns.ROOM + TEXT_TYPE + COMMA +
            Columns.START_TIME + TEXT_TYPE + COMMA +
            Columns.FINISH_TIME + TEXT_TYPE + COMMA +
            Columns.DAY_OF_WEEK + TINYINT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Columns.TABLE_NAME;

    public BusyRoomsDbHelper (Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insert (BusyRoom br)
    {
        return insert(br, getWritableDatabase());
    }

    public void insert (List<BusyRoom> brs)
    {
        SQLiteDatabase db = getWritableDatabase();
        for (BusyRoom br : brs)
        {
            insert(br, db);
        }
    }

    private long insert (BusyRoom br, SQLiteDatabase db)
    {
        ContentValues values = BusyRoomsContract.newContentValues(br);
        return db.insert(Columns.TABLE_NAME, null, values);
    }
}