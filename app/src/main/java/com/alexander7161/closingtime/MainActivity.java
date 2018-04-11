package com.alexander7161.closingtime;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //view components
    private ListView listView;

    //list view state
    private ArrayList<Restaurant> restaurants = new ArrayList<>();
    private RestaurantListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find views
        listView = findViewById(R.id.restaurantListView);
        //set up adapter
        listAdapter = new RestaurantListAdapter(this, this,restaurants);
        listView.setAdapter(listAdapter);

        //initial refresh
        refreshTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void refreshTasks()
    {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        restaurants = dbHelper.getAllRestaurants(dbHelper);
        listAdapter.setTasks(restaurants);
        listAdapter.notifyDataSetInvalidated();
        dbHelper.close();
    }

}
