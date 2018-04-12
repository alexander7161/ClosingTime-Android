package com.alexander7161.closingtime;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                        "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "NAME TEXT NOT NULL," +
                        "ADDRESS TEXT NOT NULL," +
                        "LONGADDRESS TEXT NOT NULL" +
                        ");");
                sqLiteDatabase.execSQL("CREATE TABLE RESTAURANTSHALFOFF (" +
                        "ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "IDRESTAURANT INTEGER NOT NULL," +
                        "DAYSOFWEEK INTEGER NOT NULL," +
                        "PERCENTOFF INTEGER NOT NULL," +
                        "CLOSINGTIME TIME NOT NULL," +
                        "HALFOFFPERIOD TIME NOT NULL," +
                        "FOREIGN KEY (IDRESTAURANT) REFERENCES RESTAURANTS(ID)" +
                        ");");
                //insertWasabiRestaurants(sqLiteDatabase);
                sqLiteDatabase.execSQL("INSERT INTO RESTAURANTS (NAME, ADDRESS, LONGADDRESS) VALUES" +
                        "('Wasabi', 'Kingsway', 'Wasabi 19 kingsway, Holborn, WC2B 6UN')," +
                        "('Wasabi', 'Borough', 'wasabi 59-61 Borough High St, London SE1 1NE');");
                sqLiteDatabase.execSQL("INSERT INTO RESTAURANTSHALFOFF (IDRESTAURANT, DAYSOFWEEK, PERCENTOFF, CLOSINGTIME, HALFOFFPERIOD) VALUES " +
                        "(1, 12345, 50, '20:30', '00:30')," +
                        "(2, 123456, 50, '21:30', '00:30')," +
                        "(2, 7, 50, '20:00', '00:30')" +
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
                object = (JSONArray) JsonReader.readJsonFromUrl(strings[0]);
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
                String id = Integer.toString(n + 1);
                insert = insert + "("+ id + ", 'Wasabi', '"+ text + "', '" + text + "'), ";
                new InsertWasabiDetailsTask(sqLiteDatabase, Integer.parseInt(id)).execute("http://motyar.info/webscrapemaster/api/?url="+ object.get("href") +"&xpath=//div[@id=branches]/div[2]/div/div[2]/div[7]#vws");
            }
            JSONObject object = json.getJSONObject(json.length()-1);
            String text = (String) object.get("text");
            String id = Integer.toString(json.length());
            insert = insert + "("+ id + ", 'Wasabi', '"+ text + "', '" + text + "');";
            new InsertWasabiDetailsTask(sqLiteDatabase, Integer.parseInt(id)).execute("http://motyar.info/webscrapemaster/api/?url="+ object.get("href") +"&xpath=//div[@id=branches]/div[2]/div/div[2]/div[7]#vws");
            insert = "INSERT INTO RESTAURANTS VALUES" + insert;
            sqLiteDatabase.execSQL(insert);
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}

class InsertWasabiDetailsTask extends AsyncTask<String, Void, JSONArray> {

    private SQLiteDatabase sqLiteDatabase;
    private int id;

    public InsertWasabiDetailsTask(SQLiteDatabase sqLiteDatabase, int id) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.id = id;
    }

    protected JSONArray doInBackground(String... strings) {
        JSONArray object = null;
        try {
            object = (JSONArray) JsonReader.readJsonFromUrl(strings[0]);
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
        return object;
    }

    @Override
    protected void onPostExecute(JSONArray json) {
        super.onPostExecute(json);
        try {
            JSONObject object = json.getJSONObject(0);
            String openingHours = object.getString("text");
            //openingHours = openingHours.replace("\n", "");
            String ss[] = openingHours.split("\n");
            List<String> list = new ArrayList<String>(Arrays.asList(ss));
            list = list.stream().filter(e -> e.trim().length() > 0).map(String::trim).collect(Collectors.toList());
            String insert = "";
            if (list.get(1).contains("Monday - Saturday:10am - 9.30pm")) {
                insert = insert + "(" + id + " , 1234567, 50, '21:30', '00:30')";
            } else if(list.get(1).contains("Monday - Friday")) {
                String[] week = list.get(1).split("-");
                if(week[week.length-1].contains("–")) {
                    week = week[week.length-1].split("–");
                }
                insert = insert + "("+ id + " , 12345, 50, '"+ week[week.length-1].trim()+ "', '00:30')";
            } else if (list.get(1).contains("Monday - Thursday")) {
                String[] week = list.get(1).split("-");
                if(week[week.length-1].contains("–")) {
                    week = week[week.length-1].split("–");
                }
                insert = insert + "(" + id + " , 1234, 50, '" + week[week.length - 1].trim() + "', '00:30')";
            } else if (list.get(1).contains("Monday - Wednesday")) {
                String[] week = list.get(1).split("-");
                if(week[week.length-1].contains("–")) {
                    week = week[week.length-1].split("–");
                }
                insert = insert + "(" + id + " , 123, 50, '" + week[week.length - 1].trim() + "', '00:30')";
            } else if (list.get(1).contains("Monday - Saturday")) {
                String[] week = list.get(1).split("-");
                if(week[week.length-1].contains("–")) {
                    week = week[week.length-1].split("–");
                } else if(week[week.length-1].contains("–")) {
                    week = week[week.length-1].split("–");
                }
                insert = insert + "(" + id + " , 123456, 50, '" + week[week.length - 1].trim() + "', '00:30')";
            } else if (list.get(1).contains("Monday - Sunday")) {
                String[] week = list.get(1).split("-");
                if(week[week.length-1].contains("–")) {
                    week = week[week.length-1].split("–");
                }
                insert = insert + "(" + id + " , 1234567, 50, '" + week[week.length - 1].trim() + "', '00:30')";
            } else if (list.get(1).contains("Monday to Sunday11:00 - 19:00")) {
                insert = insert + "(" + id + " , 1234567, 50, '19:00', '00:30')";
            } else if (list.get(1).contains("Monday to Wednesday9am to 10pm")) {
                insert = insert + "(" + id + " , 1234567, 50, '22:00', '00:30')";
            }
            insert = insert + ";";
            insert = "INSERT INTO RESTAURANTSHALFOFF (IDRESTAURANT, DAYSOFWEEK, PERCENTOFF, CLOSINGTIME, HALFOFFPERIOD) VALUES " + insert;
            sqLiteDatabase.execSQL(insert);
            Log.d("Json", Integer.toString(id) + list);
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}
