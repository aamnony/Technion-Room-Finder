package com.gmail.aamnony.technionroomfinder.db;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.gmail.aamnony.technionroomfinder.db.BusyRoomsContract.Columns;

public class BusyRoomsContentProvider extends ContentProvider
{
    private BusyRoomsDbHelper mDbHelper;

    // used for the UriMatcher
    private static final int MULTIPLE_ROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static final int GROUP_BY = 3;

    private static final String AUTHORITY = BusyRoomsContentProvider.class.getName();

    private static final String BASE_PATH = "busy_rooms";
    private static final String GROUP_BY_SUBPATH = "group_by";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final Uri CONTENT_URI_GROUP_BY = Uri.withAppendedPath(CONTENT_URI, GROUP_BY_SUBPATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, MULTIPLE_ROWS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SINGLE_ROW);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + '/' + GROUP_BY_SUBPATH + "/*", GROUP_BY);
    }

    @Override
    public boolean onCreate ()
    {
        mDbHelper = new BusyRoomsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//        // check if the caller has requested a column which does not exists
//        checkColumns(projection);
        queryBuilder.setTables(Columns.TABLE_NAME);
        String groupBy = null;
        switch (sURIMatcher.match(uri))
        {
            case MULTIPLE_ROWS:
                break;
            case SINGLE_ROW:
                // adding the ID to the original query
                queryBuilder.appendWhere(Columns._ID + "=" + uri.getLastPathSegment());
                break;
            case GROUP_BY:
                groupBy = uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert (Uri uri, ContentValues values)
    {
        switch (sURIMatcher.match(uri))
        {
            case MULTIPLE_ROWS:
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long newRowId = db.insert(
                        Columns.TABLE_NAME,
                        null,
                        values);
                Uri withAppendedId = ContentUris.withAppendedId(CONTENT_URI, newRowId);
                getContext().getContentResolver().notifyChange(withAppendedId, null);
                return withAppendedId;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete (Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sURIMatcher.match(uri))
        {
            case MULTIPLE_ROWS:
                rowsDeleted = db.delete(
                        Columns.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case SINGLE_ROW:
                String id = uri.getLastPathSegment();
                rowsDeleted = db.delete(
                        Columns.TABLE_NAME,
                        makeWhereClause(selection, id),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update (Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (sURIMatcher.match(uri))
        {
            case MULTIPLE_ROWS:
                rowsUpdated = db.update(
                        Columns.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case SINGLE_ROW:
                String id = uri.getLastPathSegment();
                rowsUpdated = db.update(
                        Columns.TABLE_NAME,
                        values,
                        makeWhereClause(selection, id),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public String getType (Uri uri)
    {
//        switch (sURIMatcher.match(uri))
//        {
//            case MULTIPLE_ROWS:
//                return CONTENT_TYPE;
//            case SINGLE_ROW:
//                return CONTENT_ITEM_TYPE;
//            default:
//                throw new IllegalArgumentException("Unknown URI: " + uri);
//        }
        return null;
    }

    private String makeWhereClause (String selection, String id)
    {
        return Columns._ID + "=" + id + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ')');
    }
}