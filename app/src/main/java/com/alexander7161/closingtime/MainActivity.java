package com.alexander7161.closingtime;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //view components
    private ListView listView;

    //list view state
    private ArrayList<Restaurant> restaurants = new ArrayList<>();
    private RestaurantListAdapter listAdapter;

    private FirebaseAuth mAuth;

    SwipeRefreshLayout swipe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        swipe = findViewById(R.id.swipe_refresh);
        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        swipe.setOnRefreshListener(
                () -> {
                    Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
                    //insertWasabiRestaurants();
                    refreshRestaurants();
                }
        );

        //find views
        listView = findViewById(R.id.restaurantListView);
        //set up adapter
        listAdapter = new RestaurantListAdapter(this, this,restaurants);
        listView.setAdapter(listAdapter);


        refreshRestaurants();
        EditText searchFilter = (EditText) findViewById(R.id.search_filter);
        searchFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                listAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipe.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //refreshRestaurants();
    }


    private void refreshRestaurants() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Restaurants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Restaurant> restaurants = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Restaurant restaurant = new Restaurant(document.getString("Name"), document.getString("Address"), document.getString("LongAddress"), (ArrayList<String>) document.get("ClosingTimes"), (ArrayList<Long>) document.get("PercentOffs"), (ArrayList<String>) document.get("DiscountPeriods"));
                            restaurants.add(restaurant);
                        }
                        // Sort by currently discounted first.
                        restaurants.sort((a, b) -> Boolean.compare(b.getCurrentDiscountBoolean(), a.getCurrentDiscountBoolean()));
                        listAdapter.setRestaurants(restaurants);
                        listAdapter.notifyDataSetInvalidated();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        if (swipe.isRefreshing()) {
            swipe.setRefreshing(false);
        }
    }

    public void insertRestaurants() {
        new InsertWasabiRestaurantsTask().execute("http://motyar.info/webscrapemaster/api/?url=https://wasabi.uk.com/50-30-minutes-closing&xpath=//div[@id=node-1656]/div/div/div/div/p[4]/a");
        new InsertItsuRestaurantsTask(getApplicationContext()).execute();
    }

}

class InsertWasabiRestaurantsTask extends AsyncTask<String, Void, JSONArray> {

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
            for (int n = 0; n < json.length() - 1; n++) {
                Map<String, Object> restaurant = new HashMap<>();
                JSONObject object = json.getJSONObject(n);
                String text = (String) object.get("text");
                if (text.contains("&#39;")) {
                    text = text.replace("&#39;", "'");
                }
                String id = Integer.toString(n + 1);
                restaurant.put("Name", "Wasabi");
                restaurant.put("Address", text);
                restaurant.put("LongAddress", "Wasabi " + text);
                if (object.get("href").equals("https://wasabi.uk.com/branches/1649")) {
                    new InsertWasabiDetailsTask(Integer.parseInt(id), restaurant).execute("http://motyar.info/webscrapemaster/api/?url=https://wasabi.uk.com/branches/1649&xpath=//div[@id=branches]/div[2]/div/div[2]/div[5]#vws");
                } else if(object.get("href").equals("https://wasabi.uk.com/branches/1303")) {
                    new InsertWasabiDetailsTask(Integer.parseInt(id), restaurant).execute("http://motyar.info/webscrapemaster/api/?url=https://wasabi.uk.com/branches/1303&xpath=//div[@id=branches]/div[2]/div/div[2]/div[7]#vws");
                } else {
                    new InsertWasabiDetailsTask(Integer.parseInt(id), restaurant).execute("http://motyar.info/webscrapemaster/api/?url=https://wasabi.uk.com" + object.get("href") + "&xpath=//div[@id=branches]/div[2]/div/div[2]/div[7]#vws");
                }
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}

class InsertWasabiDetailsTask extends AsyncTask<String, Void, JSONArray> {

    private static final String TAG = "restaurant";
    private int id;
    private Map<String, Object> restaurant;


    public InsertWasabiDetailsTask(int id, Map<String, Object> restaurant) {
        this.id = id;
        this.restaurant = restaurant;

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
            openingHours = openingHours.replace("\n", "");
            openingHours = openingHours.replace(" ", "");
            openingHours = openingHours.trim();
            openingHours = openingHours.replace("OpeningHours", "");



            if (openingHours.equals("MondaytoWednesday9amto10pmThursdaytoSaturday9amto11pmSunday10amto9pm")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "23:00", "23:00", "23:00", "21:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday11:00-21:00Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            } else if (openingHours.equals("Monday-Friday10:30-22:30Saturday11:00-22:30Sunday11:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:30", "22:30", "22:30", "22:30", "22:30", "22:30", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday11:30-21:00Saturday11:00-21:00Sunday11:00-17:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "17:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Saturday:10am-9.30pmSunday:11am-8pm")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:30", "21:30", "21:30", "21:30", "21:30", "21:30", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Saturday10:30-21:00Sunday10:30-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("MondaytoSaturday10:30amto9pmSunday11amto8pm")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30–21:00Saturday10:30-20:00Sunday11:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Wednesday09:00-21:30Thursday-Friday09:00-22:00Saturday-Sunday11:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:30", "21:30", "21:30", "22:00", "22:00", "20:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Saturday10:30–21:00Sunday11:00–20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30-22:00Saturday-Sunday11:00-21:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "22:00", "21:00", "21:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:00-21:00Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            } else if (openingHours.equals("Monday-Thursday10:30-20:00Friday10:30-19:30Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:00", "20:00", "20:00", "20:00", "19:30", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            } else if (openingHours.equals("Monday-Saturday09.00-21:00Sunday10:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Saturday10:00-22:00Sunday11:00-21:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "22:00", "22:00", "21:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Saturday10:00-21:00Sunday11:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30-20:30Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:30", "20:30", "20:30", "20:30", "20:30", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            } else if (openingHours.equals("Monday-Friday10:00-22:00Saturday9:00-21:00Sunday11:00-17:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "22:00", "21:00", "17:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Saturday10:00-20:00Sunday10:00-18:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:00", "20:00", "20:00", "20:00", "20:00", "20:00", "18:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("MondaytoSunday11:00-19:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:00", "20:00", "20:00", "20:00", "20:00", "20:00", "18:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Sunday-Wednesday10:00-21:00Thursday-Saturday9:00-22:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "22:00", "22:00", "22:00", "21:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:00-20:00Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:00", "20:00", "20:00", "20:00", "20:00", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            }

            else if (openingHours.equals("Monday-Wednesday10:30-23:00Thursday-Saturday10:30-24:00Sunday10:30-23:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("23:00", "23:00", "23:00", "00:00", "00:00", "00:00", "23:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            }


            else if (openingHours.equals("Monday-Friday11:00-18:00Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("18:00", "18:00", "18:00", "18:00", "18:00", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            } else if (openingHours.equals("Monday-Sunday10:30–22:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "22:00", "22:00", "22:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30-20:30Saturday-Sunday12:00-17:30")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:30", "20:30", "20:30", "20:30", "20:30", "17:30", "17:30"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30-20:00Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:00", "20:00", "20:00", "20:00", "20:00", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            } else if (openingHours.equals("Monday-Friday10:30-21:00Saturday-Sunday10:30-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "20:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Thursday11:00-20:00Friday11:00-19:30Saturday-SundayClosed")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:00", "20:00", "20:00", "20:00", "20:00", "", ""));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "", ""));
            } else if (openingHours.equals("Monday-Saturday10:30-22:30Sunday11:00-20:30")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:30", "22:30", "22:30", "22:30", "22:30", "22:30", "20:30"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            }

            else if (openingHours.equals("Monday-Wednesday11:00-23:00Thursday-Saturday11:00-00:00Sunday11:00-23:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("23:00", "23:00", "23:00", "00:00", "00:00", "00:00", "23:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            }

            else if (openingHours.equals("Monday-Saturday10:30-22:00Sunday10:30-21:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "22:00", "22:00", "21:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Sunday12:00-21:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "21:00", "21:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30-20:30Saturday-Sunday11:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("20:30", "20:30", "20:30", "20:30", "20:30", "20:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Wednesday11:00-21:00Thursday-Saturday11:00-22:00Sunday11:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "22:00", "22:00", "22:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30-22:00Saturday-Sunday11:00-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "22:00", "20:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday9:30-22:00Saturady10:30-22:00Sunday10:30-20:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "22:00", "22:00", "20:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Thursday10:30-22:00Friday-Saturday10:30-23:00Sunday10:30-21:00")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:00", "22:00", "22:00", "22:00", "23:00", "23:00", "21:00"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Friday10:30-21:00Saturday-Sunday12:00-17:30")) {
                restaurant.put("ClosingTimes", Arrays.asList("21:00", "21:00", "21:00", "21:00", "21:00", "17:30", "17:30"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            } else if (openingHours.equals("Monday-Saturday11:00-22:30Sunday11:00-20:30")) {
                restaurant.put("ClosingTimes", Arrays.asList("22:30", "22:30", "22:30", "22:30", "22:30", "22:30", "20:30"));
                restaurant.put("PercentOffs", Arrays.asList(50, 50, 50, 50, 50, 50, 50));
                restaurant.put("DiscountPeriods", Arrays.asList("00:30", "00:30", "00:30", "00:30", "00:30", "00:30", "00:30"));
            }

            else {
                restaurant.put("ClosingTimes", Arrays.asList(1, 1));
                restaurant.put("PercentOffs", Arrays.asList(1, 1));
                restaurant.put("DiscountPeriods", Arrays.asList(1, 1));
                Log.d("not present", openingHours);
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Restaurants").document("wasabi"+restaurant.get("Address").toString().replace(" ",""))
                    .set(restaurant)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            Log.d("Json", Integer.toString(id) + restaurant.get("Address"));
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}

class InsertItsuRestaurantsTask extends AsyncTask<Void, Void, JSONArray> {

    private Context context;

    public InsertItsuRestaurantsTask(Context context) {
        this.context = context;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getResources().openRawResource(R.raw.itsu);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    @Override
    protected JSONArray doInBackground(Void... voids) {
        JSONArray object = null;
        try {
            object = (JSONArray) new JSONArray(loadJSONFromAsset());
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
        return object;
    }

    @Override
    protected void onPostExecute(JSONArray json) {
        super.onPostExecute(json);
        try {
            for (int n = 0; n < json.length() - 1; n++) {
                Map<String, Object> restaurant = new HashMap<>();
                JSONObject object = json.getJSONObject(n);
                JSONArray array1 = object.getJSONArray("children");
                JSONObject obj1 = array1.optJSONObject(1);                 //URL here
                String url = obj1.getJSONArray("attributes").getJSONObject(0).getString("value");
                JSONArray array2 = obj1.getJSONArray("children");
                String address = array2.getJSONObject(1).getJSONArray("children").getJSONObject(0).getString("content");
                address = address.substring(address.indexOf(".")+1,address.length()).trim();
                JSONObject obj2 = array2.getJSONObject(3);
                JSONArray array3 = obj2.getJSONArray("children");
                String longAddress = array3.getJSONObject(0).getString("content");

                String id = Integer.toString(n + 1);
                restaurant.put("Name", "Itsu");
                restaurant.put("Address", address);
                restaurant.put("LongAddress", "Itsu " + longAddress);
                Log.d("itsu" + id, address);
                new InsertItsuDetailsTask(Integer.parseInt(id), restaurant).execute("http://motyar.info/webscrapemaster/api/?url="+ url +"&xpath=//ul[@id=hours]/li");
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}

class InsertItsuDetailsTask extends AsyncTask<String, Void, JSONArray> {

    private static final String TAG = "restaurant";
    private int id;
    private Map<String, Object> restaurant;


    public InsertItsuDetailsTask(int id, Map<String, Object> restaurant) {
        this.id = id;
        this.restaurant = restaurant;

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
        List<String> closingTimes = new ArrayList<>();
        List<Integer> percentOffs = new ArrayList<>();
        List<String> discountPeriods = new ArrayList<>();

        try {
            if(json.getJSONObject(0).getString("text").contains("flight")) {
                // Airport Itsu, 30 minutes before last flight.
                Log.d("Json", Integer.toString(id) + restaurant.get("Address") + "AIRPORTNOTADDED");
                return;
            }
            if(json.getJSONObject(0).getString("text").contains("Bicester")) {
                // Bicester doesn't offer 50% off.
                Log.d("Json", Integer.toString(id) + restaurant.get("Address") + "BICESTERNOTADDED");
                return;
            }
            if(json.getJSONObject(7).getString("text").contains("half")) {
                for(int i = 0; i<7;i++) {
                    String s = json.getJSONObject(i).getString("text");
                    s = s.replace(".", ":");
                    s = s.replace("(breakfast until 10am)", "");
                    if(s.contains("closed")) {
                        closingTimes.add("");
                        percentOffs.add(null);
                        discountPeriods.add("");
                    } else {
                        String time = s.split("-")[1].trim();
                        if(time.contains("pm")) {
                            if(time.contains("30")) {
                                int closingTime = Integer.parseInt(time.replace("pm", "").replace(":30", ""));
                                if(closingTime<=12) {
                                    closingTime += 12;
                                }
                                closingTimes.add(closingTime+":30");
                                percentOffs.add(50);
                                discountPeriods.add("00:30");

                            } else {
                                int closingTime = Integer.parseInt(time.replace("pm", "").replace(":00", ""));
                                if(closingTime<=12) {
                                    closingTime += 12;
                                }
                                closingTimes.add(closingTime + ":00");
                                percentOffs.add(50);
                                discountPeriods.add("00:30");
                            }
                        }
                    }
                }
            } else if (json.getJSONObject(5).getString("text").contains("half")) {
                for(int i = 0; i<5;i++) {
                    String s = json.getJSONObject(i).getString("text");
                    if (s.contains("closed")) {
                        closingTimes.add("");
                        percentOffs.add(null);
                        discountPeriods.add("");
                    } else {
                        String time = s.split("-")[1].trim();
                        if (time.contains("pm")) {
                            if (time.contains("30")) {
                                int closingTime = Integer.parseInt(time.replace("pm", "").replace(":30", ""));
                                if(closingTime<=12) {
                                    closingTime += 12;
                                }
                                closingTimes.add(closingTime + ":30");
                                percentOffs.add(50);
                                discountPeriods.add("00:30");

                            } else {
                                int closingTime = Integer.parseInt(time.replace("pm", ""));
                                if(closingTime<=12) {
                                    closingTime += 12;
                                }
                                closingTimes.add(closingTime + ":00");
                                percentOffs.add(50);
                                discountPeriods.add("00:30");
                            }
                        }
                    }
                }
            } else if (json.getJSONObject(0).getString("text").contains("Cambridge")) {
                for(int i = 1; i<8;i++) {
                    String s = json.getJSONObject(i).getString("text");
                    if (s.contains("closed")) {
                        closingTimes.add("");
                        percentOffs.add(null);
                        discountPeriods.add("");
                    } else {
                        String time = s.split("-")[1].trim();
                        if (time.contains("pm")) {
                            if (time.contains("30")) {
                                int closingTime = Integer.parseInt(time.replace("pm", "").replace(":30", ""));
                                closingTime += 12;
                                closingTimes.add(closingTime + ":30");
                                percentOffs.add(50);
                                discountPeriods.add("00:30");

                            } else {
                                int closingTime = Integer.parseInt(time.replace("pm", ""));
                                closingTime += 12;
                                closingTimes.add(closingTime + ":00");
                                percentOffs.add(50);
                                discountPeriods.add("00:30");
                            }
                        }
                    }
                }
            } else {
                // No half price sale.
                Log.d("Json", Integer.toString(id) + restaurant.get("Address") + "NOHALFPRICENOTADDED");
                return;
            }
                restaurant.put("ClosingTimes", closingTimes);
                restaurant.put("PercentOffs", percentOffs);
                restaurant.put("DiscountPeriods", discountPeriods);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Restaurants").document("itsu" + restaurant.get("Address").toString().replace(" ", ""))
                    .set(restaurant)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            Log.d("Json", Integer.toString(id) + restaurant.get("Address"));
        } catch (Exception e) {
            Log.e("json", Integer.toString(id) + restaurant.get("Address") + e.toString());
        }
    }
}