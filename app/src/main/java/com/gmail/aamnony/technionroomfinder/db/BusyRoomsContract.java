package com.gmail.aamnony.technionroomfinder.db;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.gmail.aamnony.technionroomfinder.pojos.BusyRoom;

public final class BusyRoomsContract
{
    /**
     * Prevents instantiation of this class.
     */
    private BusyRoomsContract ()
    {
    }

    public static ContentValues newContentValues(BusyRoom br)
    {
        ContentValues values = new ContentValues(5);
        values.put(Columns.BUILDING, br.getBuilding());
        values.put(Columns.ROOM, br.getRoom());
        values.put(Columns.START_TIME, br.getStartTime());
        values.put(Columns.FINISH_TIME, br.getFinishTime());
        values.put(Columns.DAY_OF_WEEK, br.getDayOfWeek());
        return values;
    }

    /**
     * Inner class that defines the table contents
     */
    public static class Columns implements BaseColumns
    {
        public static final String TABLE_NAME = "busy_rooms";
        public static final String BUILDING = "building";
        public static final String ROOM = "room";
        public static final String START_TIME = "start_time";
        public static final String FINISH_TIME = "finish_time";
        public static final String DAY_OF_WEEK = "day_of_week";

    }
}