package com.gmail.aamnony.technionroomfinder.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.gmail.aamnony.technionroomfinder.R;
import com.gmail.aamnony.technionroomfinder.db.BusyRoomsContentProvider;
import com.gmail.aamnony.technionroomfinder.db.BusyRoomsContract;
import com.gmail.aamnony.technionroomfinder.pojos.BusyRoom;
import com.gmail.aamnony.technionroomfinder.util.RepUtils;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

public class RepActivity extends Activity
{
    private TextView text1;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep);
        text1 = (TextView) findViewById(android.R.id.text1);
        new FillDatabaseTask().execute(this);
    }

    private static final class FillDatabaseTask extends AsyncTask<RepActivity, String, Boolean>
    {
        private static final String TAG = FillDatabaseTask.class.getName();
        private RepActivity parent;

        @Override
        protected Boolean doInBackground (RepActivity... params)
        {
            parent = params[0];
            try
            {
                publishProgress("Downloading zip file");
                File zipFile = RepUtils.downloadZip(params[0]);

                publishProgress("Unzipping");
                RepUtils.unzip(zipFile);

                CharBuffer charBuffer = RepUtils.toUnicode(new File(zipFile.getParentFile(), "REPY"));
                //            String unicodeData = RepUtils.extractUnicodeData(zipFile);

                publishProgress("Parsing data");
                List<BusyRoom> busyRooms = RepUtils.parse(charBuffer.toString());

                publishProgress("Filling database");
                ContentValues[] contentValues = new ContentValues[busyRooms.size()];
                int i = 0;
                for (BusyRoom br : busyRooms)
                {
                    contentValues[i++] = BusyRoomsContract.newContentValues(br);
                }
                if (contentValues.length != params[0].getContentResolver().bulkInsert(BusyRoomsContentProvider.CONTENT_URI, contentValues))
                {
                    throw new Error("Failed to add all items to database");
                }
                // TODO: 29/10/2017 Insert rooms only to a different table (consult with Matan).

                publishProgress("Finalizing");
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute (Boolean success)
        {
            // TODO: 29/10/2017 Close dialog, refresh cursor adapter/loader.
            parent.setResult(0x5);
            parent.text1.append("\nDone");
            PreferenceManager.getDefaultSharedPreferences(parent).edit().putBoolean("db_exists", true).commit();
        }

        @Override
        protected void onProgressUpdate (String... values)
        {
            super.onProgressUpdate(values);
            //Log.i(TAG, Arrays.toString(values));
            parent.text1.append("\n");
            parent.text1.append(Arrays.toString(values));
        }

        @Override
        protected void onCancelled ()
        {
            super.onCancelled();
        }
    }
}
