package com.alexander7161.closingtime;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTableLockedException;

import java.util.ArrayList;


/*
Custom Database Helper to store Restaurant object
 */
public class DbHelper extends SQLiteOpenHelper
{
    //Name and current Version of database

    public static final String DB_NAME = "DB";
    public static final int DB_VERSION = 1;

    /*
    DBHelper Constructor
     */
    public DbHelper(Context context)
    {
        super(context,DB_NAME,null,DB_VERSION);
    }

    /*
    Called when database is first created.
    We will consider this as upgrading from version O to version 1.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        onUpgrade(sqLiteDatabase, 0, DB_VERSION);
    }

    /*
    Called when updating the database from an oldVersion to a newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        switch(oldVersion)
        {
            case 0:
                //create intiial database using SQL
                sqLiteDatabase.execSQL("CREATE TABLE RESTAURANTS (" +
                        "ID INTEGER PRIMARY KEY," +
                        "NAME TEXT," +
                        "ADDRESS TEXT" +
                        ");");
                sqLiteDatabase.execSQL("CREATE TABLE RESTAURANTSHALFOFF (" +
                        "IDRESTAURANT INTEGER NOT NULL," +
                        "DAYSOFWEEK INTEGER NOT NULL," +
                        "PERCENTOFF INTEGER NOT NULL," +
                        "CLOSINGTIME TIME NOT NULL," +
                        "HALFOFFPERIOD TIME NOT NULL," +
                        "PRIMARY KEY (IDRESTAURANT)," +
                        "FOREIGN KEY (IDRESTAURANT) REFERENCES RESTAURANT(IDRESTAURANT)\n" +
                        ");");
                sqLiteDatabase.execSQL("INSERT INTO RESTAURANTS VALUES" +
                        "(1, 'Wasabi', 'Kingsway');");
                sqLiteDatabase.execSQL("INSERT INTO RESTAURANTSHALFOFF VALUES" +
                        "(1, 12345, 50, '20:30', '00:30');");
            case 1:

            case 2:
                break;
            default:
                throw new IllegalArgumentException();

        }
    }

    /*
    Query method to retrieve the list of incomplete Tasks in the database an an ArrayList
     */

    public ArrayList<Restaurant> getIncompleteTasks()
    {
        ArrayList<Restaurant> output = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        if(db == null) return output;

        //Store result of SQL query in Cursor object
        Cursor rawTasks = db.rawQuery("SELECT * FROM RESTAURANTS;",null);

        //Iterate over all tasks in Cursor, adding them to ArrayList<Restaurant> output
        if(rawTasks.moveToFirst())
        {
            do {
                output.add(new Restaurant(rawTasks)); //Construct a new Restaurant using the cursor, and add it to the ArrayList
            } while (rawTasks.moveToNext());
        }

        rawTasks.close();   //Close cursor (finished with it)

        return output;
    }

    /**
     * Get a single task by it's task id. Used for loading a task to the edit task activity.
     */
    public Restaurant getTask(long taskId)
    {
        SQLiteDatabase db = getReadableDatabase();
        if(db == null) return null;

        //Make an SQL query
        Cursor result = db.query(
                "RESTAURANTS",                    // To the table "Tasks"
                null,                    // All columns
                "ID = ?",               // Where id = ? (a parameter)
                new String[]{taskId + ""},      // set the parameter to taskId
                null,
                null,
                null
        );

        //Construct new Restaurant object from result of Cursor and return it.
        if(result.moveToFirst())
        {
            return new Restaurant(result);
        }else{
            return null;
        }
    }
}
