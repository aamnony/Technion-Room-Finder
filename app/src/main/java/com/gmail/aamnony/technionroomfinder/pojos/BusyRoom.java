package com.gmail.aamnony.technionroomfinder.pojos;

import java.util.regex.Matcher;

public class BusyRoom
{
    private String building;
    private String room; // TODO: convert to integer?
    private String startTime; // HH:mm format.  // TODO: 30/10/2017 add seconds?
    private String finishTime; // HH:mm format. // TODO: 30/10/2017 add seconds?
    private byte dayOfWeek; // Starting at 1 (Sunday = 1, Monday = 2, ... , Friday = 6).

    public BusyRoom (String building, String room, String startTime, String finishTime, String dayOfWeekHebrew)
    {
        this.building = building;
        this.room = room;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.dayOfWeek = toByte(dayOfWeekHebrew);
    }

    public BusyRoom (Matcher matcher)
    {
        this(reverse(matcher.group(1)), matcher.group(2), fixTime(matcher.group(3)), fixTime(matcher.group(4)),
                matcher.group(5));
    }

    @Override
    public String toString ()
    {
        return building + "-" + room + " @" + dayOfWeek + " " + startTime + "-" + finishTime;
    }

    public String getBuilding ()
    {
        return building;
    }

    public String getRoom ()
    {
        return room;
    }

    public String getStartTime ()
    {
        return startTime;
    }

    public String getFinishTime ()
    {
        return finishTime;
    }

    public int getDayOfWeek ()
    {
        return dayOfWeek;
    }

    private static byte toByte (String dayOfWeekHebrew)
    {
        switch (dayOfWeekHebrew)
        {
            default:
            case "\u05d0":
                return 1;
            case "\u05d1":
                return 2;
            case "\u05d2":
                return 3;
            case "\u05d3":
                return 4;
            case "\u05d4":
                return 5;
            case "\u05d5":
                return 6;
        }
    }

    private static String reverse (String s)
    {
        return new StringBuilder(s).reverse().toString();
    }

    private static String fixTime (String time)
    {
        char[] chars = time.toCharArray();
        chars[1 + (chars.length % 4)] = ':';
        return new String(chars);
    }
}
