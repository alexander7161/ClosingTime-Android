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
}