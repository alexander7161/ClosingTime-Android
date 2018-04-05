package com.alexander7161.closingtime;

import android.content.ContentValues;
import android.database.Cursor;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

/*
Restaurant Model object
 */

public class Restaurant
{
    private long id = -1;    //If the id == -1, the Restaurant object has never entered the database yet
    private String name;
    private String address;
    private int percentOff;
    private ArrayList<String> closingTimes = new ArrayList<>();


    /*
    Constructor - creating a Restaurant object from scratch
     */
    public Restaurant(String title, String notes, int percentOff)
    {
        this.name = title;
        this.address = notes;
        this.percentOff = percentOff;
    }

    /*
    Constructor - creating a Restaurant object from an entry in the database
     */
    public Restaurant(Cursor input, DbHelper dbHelper)
    {
        id = input.getLong(input.getColumnIndex("ID"));
        name = input.getString(input.getColumnIndex("NAME"));
        address = input.getString(input.getColumnIndex("ADDRESS"));
        Cursor details = dbHelper.getRestaurantDetails(this);
        if(details.moveToFirst()) {
            percentOff = details.getInt(details.getColumnIndex("PERCENTOFF"));
            do {
                int daysOfWeekInt = details.getInt(details.getColumnIndex("DAYSOFWEEK"));
                ArrayList<Integer> daysOfWeek = new ArrayList<>();
                collectDigits(daysOfWeekInt, daysOfWeek);
                for (Integer i : daysOfWeek) {
                    closingTimes.add(i - 1, details.getString(details.getColumnIndex("CLOSINGTIME")));
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

    /*
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
        output.put("PERCENTOFF",percentOff);

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

    public int getPercentOff() {return percentOff;}

    public String getClosingTime(int index) {
        if(index>closingTimes.size()-1) {
            return "Closed";
        }
        return LocalTime.parse(closingTimes.get(index)).toString("HH:mm");
    }

    @Override
    public String toString() {
        return getName() + " " +  getAddress() + " " + getPercentOff() + " Monday: " + getClosingTime(1);
    }
}
