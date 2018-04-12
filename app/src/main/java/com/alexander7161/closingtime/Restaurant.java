package com.alexander7161.closingtime;

import android.content.ContentValues;
import android.database.Cursor;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

/**
Restaurant Model object
 */
public class Restaurant
{
    private long id = -1;    //If the id == -1, the Restaurant object has never entered the database yet
    private String name;
    private String address;
    private String longAddress;
    private ArrayList<String> closingTimes = new ArrayList<>();
    private ArrayList<String> halfOffPeriods = new ArrayList<>();
    private ArrayList<Integer> percentOffs = new ArrayList<>();


    /*
    Constructor - creating a Restaurant object from scratch
     */
    public Restaurant(String name, String address, String longAddress)
    {
        this.name = name;
        this.address = address;
        this.longAddress = longAddress;
    }

    /**
    Constructor - creating a Restaurant object from an entry in the database
     */
    public Restaurant(Cursor input, DbHelper dbHelper)
    {
        id = input.getLong(input.getColumnIndex("ID"));
        name = input.getString(input.getColumnIndex("NAME"));
        address = input.getString(input.getColumnIndex("ADDRESS"));
        longAddress = input.getString(input.getColumnIndex("LONGADDRESS"));
        Cursor details = dbHelper.getRestaurantDetails(this);
        if(details.moveToFirst()) {
            do {
                int daysOfWeekInt = details.getInt(details.getColumnIndex("DAYSOFWEEK"));
                ArrayList<Integer> daysOfWeek = new ArrayList<>();
                collectDigits(daysOfWeekInt, daysOfWeek);
                for (Integer i : daysOfWeek) {
                    closingTimes.add(i - 1, details.getString(details.getColumnIndex("CLOSINGTIME")));
                    halfOffPeriods.add(i -1, details.getString(details.getColumnIndex("HALFOFFPERIOD")));
                    percentOffs.add(i-1, details.getInt(details.getColumnIndex("PERCENTOFF")));

                }
            } while (details.moveToNext());
        }
        details.close();
    }

    private static void collectDigits(int num, List<Integer> digits) {
        if(num / 10 > 0) {
            collectDigits(num / 10, digits);
        }
        digits.add(num % 10);
    }

    /**
    Represent our Restaurant data as a ContentValues object - for easy insertion into the database
     */
    public ContentValues getContentValues()
    {
        ContentValues output = new ContentValues();

        //If the Restaurant has been in the database before, use it's id.
        //Otherwise, the database will assign a new ID to it when it is added.
        if(id > 0) output.put("id",id);

        //Add the rest of the Restaurant data to the content values object
        output.put("NAME",name);
        output.put("ADDRESS",address);

        return output;
    }

    public long getId() {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLongAddress() {return longAddress;}

    public String getClosingTime(int index) {
        if(index>closingTimes.size()-1) {
            return "Closed";
        }
        return LocalTime.parse(closingTimes.get(index)).toString("HH:mm");
    }

//    public String getCurrentDiscount() {
//        LocalTime localTime = new LocalTime();
//        DateTime dateTime = new DateTime();
//        int index = dateTime.getDayOfWeek() - 1;
//        if(percentOffs.size()-1<index) {
//            return "No Discount Today";
//        }
//        int percentOff = percentOffs.get(index);
//        LocalTime closingTime = LocalTime.parse(closingTimes.get(index));
//        PeriodFormatter formatter = new PeriodFormatterBuilder()
//                .appendHours().appendSuffix(":")
//                .appendMinutes()
//                .toFormatter();
//        Period dur = Period.parse(halfOffPeriods.get(index), formatter);
//        LocalTime startHalfOff = closingTime.minus(dur);
//
//        if(localTime.isAfter(startHalfOff) && localTime.isBefore(closingTime)) {
//            return percentOff + "% off from " + startHalfOff.toString("HH:mm") + " to " + closingTime.toString("HH:mm");
//        } else if (localTime.isBefore(startHalfOff)) {
//            return percentOff + "% off starts at " + startHalfOff.toString("HH:mm");
//        } else if (localTime.isAfter(closingTime)) {
//            return percentOff + "% ended at " + closingTime.toString("HH:mm");
//        } else {
//            return "";
//        }
//    }

    @Override
    public String toString() {
        return getName() + " " +  getAddress() + " " + " Monday: " + getClosingTime(1);
    }
}
