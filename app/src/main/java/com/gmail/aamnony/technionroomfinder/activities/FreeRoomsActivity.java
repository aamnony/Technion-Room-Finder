package com.gmail.aamnony.technionroomfinder.activities;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

import com.gmail.aamnony.technionroomfinder.R;
import com.gmail.aamnony.technionroomfinder.db.BusyRoomsContentProvider;
import com.gmail.aamnony.technionroomfinder.db.BusyRoomsContract;
import com.gmail.aamnony.technionroomfinder.db.BusyRoomsDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FreeRoomsActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int LOADER_ID = 0;
    public static final String LOADER_TIME = "LOADER_TIME";
    public static final String LOADER_DAY = "LOADER_DAY";
    private static final String[] DAYS = {"42", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    long t;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("db_exists", false))
        {
            loadRooms();
        }
        else
        {
            startActivityForResult(new Intent(this, RepActivity.class), 0);
        }
    }

    private void loadRooms ()
    {
        setListAdapter(new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                null,
                new String[]{BusyRoomsContract.Columns.BUILDING, BusyRoomsContract.Columns.ROOM},
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        ));
//        int c = 0;
//        t = System.nanoTime();
//        for (int i = 0; i < 5; i++)
//            c = g_1(selectionArgs);
        Calendar calendar = Calendar.getInstance();
        int d = calendar.get(Calendar.DAY_OF_WEEK);
        String day = String.valueOf(d);
        String time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(calendar.getTime());
        setTitle(DAYS[d] + " @ " + time);
        String[] selectionArgs = {day, time};
        getListAdapter().swapCursor(g_3(selectionArgs));
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_free_rooms, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_load_rep:
                startActivityForResult(new Intent(this, RepActivity.class), 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0x5)
        {
            loadRooms();
        }
    }

    Cursor g_1 (String[] args)
    {
        return new BusyRoomsDbHelper(this).getReadableDatabase().rawQuery(
                "SELECT _ID, `room` , `building`\n" +
                        "FROM `busy_rooms` as `r`\n" +
                        "WHERE NOT EXISTS (\n" +
                        "    SELECT *\n" +
                        "    FROM `busy_rooms` as `busy`\n" +
                        "    WHERE\n" +
                        "        `busy`.`building` = `r`.`building` AND `busy`.`room` = `r`.`room`\n" +
                        "        AND `day_of_week` = ? AND ? BETWEEN `start_time` AND `finish_time` \n" +
                        ")\n" +
                        "GROUP BY `building` , `room`",
                args
        );
    }


    Cursor g_2 (String[] args)
    {
        return new BusyRoomsDbHelper(this).getReadableDatabase().rawQuery(
                "SELECT _ID, `room` , `building`\n" +
                        "FROM (\n" +
                        "SELECT _ID, `room` , `building`\n" +
                        "    FROM `busy_rooms`\n" +
                        "    GROUP BY `building` , `room`\n" +
                        ") as `rooms`\n" +
                        "WHERE NOT EXISTS (\n" +
                        "    SELECT *\n" +
                        "    FROM `busy_rooms` as `temp_name`\n" +
                        "    WHERE   `temp_name`.`building` = `rooms`.`building`\n" +
                        "            AND `temp_name`.`room` = `rooms`.`room` \n" +
                        "            AND `day_of_week` = ? \n" +
                        "            AND ? BETWEEN `start_time` AND `finish_time`\n" +
                        ")\n" +
                        "GROUP BY `building` , `room`",
                args
        );
    }


    Cursor g_3 (String[] args)
    {
        return new BusyRoomsDbHelper(this).getReadableDatabase().rawQuery(
                "SELECT `_id` , `building` , `room`\n" +
                        "FROM (\n" +
                        "    SELECT `_id` , `building` , `room`\n" +
                        "    FROM `busy_rooms`\n" +
                        "    GROUP BY `building` , `room`\n" +
                        ") as `rooms`\n" +
                        "WHERE NOT EXISTS (\n" +
                        "    SELECT *\n" +
                        "    FROM `busy_rooms` as `temp_name`\n" +
                        "    WHERE   `temp_name`.`building` = `rooms`.`building`\n" +
                        "            AND `temp_name`.`room` = `rooms`.`room` \n" +
                        "            AND `day_of_week` = ? \n" +
                        "            AND ? BETWEEN `start_time` AND `finish_time`\n" +
                        ")",
                args
        );
    }


    void g_0 (Bundle bundle)
    {
        String[] projection = {BusyRoomsContract.Columns._ID, BusyRoomsContract.Columns.ROOM, BusyRoomsContract.Columns.BUILDING};

        String selection = String.format(
/**/            "NOT EXISTS (" +
/**/                    "SELECT * " +
/**/                    "FROM %1$s as %2$s " +
/**/                    "WHERE " +
/**/                        "%2$s.%3$s = %1$s.%3$s AND %2$s.%4$s = %1$s.%4$s " +
/**/                        "AND %5$s = ? " +
/**/                        "AND ? BETWEEN %6$s AND %7$s" +
/**/            ") ", // +
///**/            "GROUP BY %3$s , %4$s",

                BusyRoomsContract.Columns.TABLE_NAME,
                "temp_name",
                BusyRoomsContract.Columns.BUILDING,
                BusyRoomsContract.Columns.ROOM,
                BusyRoomsContract.Columns.DAY_OF_WEEK,
                BusyRoomsContract.Columns.START_TIME,
                BusyRoomsContract.Columns.FINISH_TIME
        );

        String[] selectionArgs = {bundle.getString(LOADER_DAY), bundle.getString(LOADER_TIME)};
        String sortOrder = null;

        String groupBy = BusyRoomsContract.Columns.BUILDING + " , " + BusyRoomsContract.Columns.ROOM;

        BusyRoomsDbHelper dbh = new BusyRoomsDbHelper(this);
        Cursor c = dbh.getReadableDatabase().query(BusyRoomsContract.Columns.TABLE_NAME, projection, selection, selectionArgs, groupBy, null, sortOrder);
        c.getCount();
        c.close();
//        getListAdapter().swapCursor(c);
    }

    @Override
    public Loader<Cursor> onCreateLoader (int id, Bundle bundle)
    {
        String[] projection = {BusyRoomsContract.Columns._ID, BusyRoomsContract.Columns.ROOM, BusyRoomsContract.Columns.BUILDING};

        String selection = String.format(
/**/            "NOT EXISTS (" +
/**/                    "SELECT * " +
/**/                    "FROM %1$s as %2$s " +
/**/                    "WHERE " +
/**/                        "%2$s.%3$s = %1$s.%3$s AND %2$s.%4$s = %1$s.%4$s " +
/**/                        "AND %5$s = ? " +
/**/                        "AND ? BETWEEN %6$s AND %7$s" +
/**/            ") ", // +
///**/            "GROUP BY %3$s , %4$s",

                BusyRoomsContract.Columns.TABLE_NAME,
                "temp_name",
                BusyRoomsContract.Columns.BUILDING,
                BusyRoomsContract.Columns.ROOM,
                BusyRoomsContract.Columns.DAY_OF_WEEK,
                BusyRoomsContract.Columns.START_TIME,
                BusyRoomsContract.Columns.FINISH_TIME
        );

        String[] selectionArgs = {bundle.getString(LOADER_DAY), bundle.getString(LOADER_TIME)};
        String sortOrder = null;

        String groupBy = BusyRoomsContract.Columns.BUILDING + " , " + BusyRoomsContract.Columns.ROOM;

        Uri uri = Uri.withAppendedPath(BusyRoomsContentProvider.CONTENT_URI_GROUP_BY, Uri.encode(groupBy));

        return new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished (Loader<Cursor> loader, Cursor c)
    {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        getListAdapter().swapCursor(c);
    }

    @Override
    public void onLoaderReset (Loader<Cursor> loader)
    {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        getListAdapter().swapCursor(null);
    }

    @Override
    public SimpleCursorAdapter getListAdapter ()
    {
        return (SimpleCursorAdapter) super.getListAdapter();
    }
}
