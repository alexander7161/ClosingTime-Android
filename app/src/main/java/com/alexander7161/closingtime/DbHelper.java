package com.alexander7161.closingtime;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
Custom Database Helper to store Restaurant object
 */
public class DbHelper extends SQLiteOpenHelper
{
    //Name and current Version of database

    public static final String DB_NAME = "DB";
    public static final int DB_VERSION = 1;

    /**
    DBHelper Constructor
     */
    public DbHelper(Context context)
    {
        super(context,DB_NAME,null,DB_VERSION);
    }

    /**
    Called when database is first created.
    We will consider this as upgrading from version O to version 1.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        onUpgrade(sqLiteDatabase, 0, DB_VERSION);
    }

    /**
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
                        "ID INTEGER PRIMARY KEY NOT NULL," +
                        "NAME TEXT NOT NULL," +
                        "ADDRESS TEXT NOT NULL," +
                        "LONGADDRESS TEXT NOT NULL" +
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
                        "(1, 'Wasabi', 'Kingsway', 'Wasabi 19 kingsway, Holborn, WC2B 6UN')," +
                        "(2, 'Wasabi', 'Borough', 'wasabi 59-61 Borough High St, London SE1 1NE');");
                //insertWasabiRestaurants(sqLiteDatabase);
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

    /**
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

    public void insertWasabiRestaurants(SQLiteDatabase sqLiteDatabase) {
        new InsertWasabiRestaurantsTask(sqLiteDatabase).execute("http://motyar.info/webscrapemaster/api/?url=https://wasabi.uk.com/50-30-minutes-closing&xpath=//div[@id=node-1656]/div/div/div/div/p[4]/a");
    }

}
class InsertWasabiRestaurantsTask extends AsyncTask<String, Void, JSONArray> {

    private SQLiteDatabase sqLiteDatabase;

    public InsertWasabiRestaurantsTask(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    protected JSONArray doInBackground(String... strings) {
        JSONArray object = null;
            try {
                object = com.alexander7161.closingtime.JsonReader.readJsonFromUrl(strings[0]);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
        return object;
    }

    @Override
    protected void onPostExecute(JSONArray json) {
        super.onPostExecute(json);
        try {
            String insert = "";
            for(int n = 0; n < json.length()-1; n++)
            {
                JSONObject object = json.getJSONObject(n);
                String text = (String) object.get("text");
                insert = insert + "("+ n+3 + ", 'Wasabi', "+ text + ", " + text + "),";
            }
            JSONObject object = json.getJSONObject(json.length()-1);
            String text = (String) object.get("text");
            insert = insert + "("+ json.length()+3 + ", 'Wasabi', "+ text + ", " + text + ");";
            sqLiteDatabase.execSQL("INSERT INTO RESTAURANTS VALUES" +
                    insert);
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}
