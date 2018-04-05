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
                        "ID INTEGER NOT NULL PRIMARY KEY," +
                        "IDRESTAURANT INTEGER NOT NULL," +
                        "DAYSOFWEEK INTEGER NOT NULL," +
                        "PERCENTOFF INTEGER NOT NULL," +
                        "CLOSINGTIME TIME NOT NULL," +
                        "HALFOFFPERIOD TIME NOT NULL," +
                        "FOREIGN KEY (IDRESTAURANT) REFERENCES RESTAURANTS(ID)" +
                        ");");
                sqLiteDatabase.execSQL("INSERT INTO RESTAURANTS VALUES" +
                        "(1, 'Wasabi', 'Kingsway')," +
                        "(2, 'Wasabi', 'Borough');");
                sqLiteDatabase.execSQL("INSERT INTO RESTAURANTSHALFOFF VALUES " +
                        "(1, 1, 12345, 50, '20:30', '00:30')," +
                        "(2, 2, 123456, 50, '21:30', '00:30')," +
                        "(3, 2, 7, 50, '20:00', '00:30')" +
                        ";");
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

    public ArrayList<Restaurant> getAllRestaurants(DbHelper dbHelper)
    {
        ArrayList<Restaurant> output = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        if(db == null) return output;

        //Store result of SQL query in Cursor object
        Cursor rawTasks = db.rawQuery("SELECT * " +
                "FROM RESTAURANTS"
                ,null);

        //Iterate over all tasks in Cursor, adding them to ArrayList<Restaurant> output
        if(rawTasks.moveToFirst())
        {
            do {
                output.add(new Restaurant(rawTasks, dbHelper)); //Construct a new Restaurant using the cursor, and add it to the ArrayList
            } while (rawTasks.moveToNext());
        }

        rawTasks.close();   //Close cursor (finished with it)

        return output;
    }

    public Cursor getRestaurantDetails(Restaurant restaurant)
    {
        Cursor rawDetails = null;

        SQLiteDatabase db = getReadableDatabase();
        if(db == null) return rawDetails;
        //Store result of SQL query in Cursor object
        rawDetails = db.rawQuery("SELECT R.ID, R.NAME, R.ADDRESS, H.PERCENTOFF, H.DAYSOFWEEK, H.CLOSINGTIME, H.HALFOFFPERIOD " +
                "FROM RESTAURANTS R " +
                "JOIN" +
                "(SELECT *" +
                " FROM RESTAURANTSHALFOFF" +
                ") H" +
                " ON R.ID = H.IDRESTAURANT " +
                "WHERE R.ID=" + restaurant.getId(),null);

        return rawDetails;
    }

}
