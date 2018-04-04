package com.alexander7161.closingtime;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.joda.time.DateTime;

/*
Restaurant Model object
 */

public class Restaurant
{
    private long id = -1;    //If the id == -1, the Restaurant object has never entered the database yet
    private String name;
    private String address;


    /*
    Constructor - creating a Restaurant object from scratch
     */
    public Restaurant(String title, String notes)
    {
        this.name = title;
        this.address = notes;
    }

    /*
    Constructor - creating a Restaurant object from an entry in the database
     */
    public Restaurant(Cursor input)
    {
        id = input.getLong(input.getColumnIndex("ID"));
        name = input.getString(input.getColumnIndex("NAME"));
        address = input.getString(input.getColumnIndex("ADDRESS"));
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

    @Override
    public String toString() {
        return getName() + " " +  getAddress();
    }
}
