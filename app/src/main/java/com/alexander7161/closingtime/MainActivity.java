package com.alexander7161.closingtime;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
        listAdapter = new RestaurantListAdapter(this,restaurants);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                taskClicked((Restaurant)listAdapter.getItem(i));
            }
        });

        //initial refresh
        refreshTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        switch (item.getItemId()){
//            case R.id.createTask:
//                Intent createTaskIntent = new Intent(TaskListActivity.this,EditTaskActivity.class);
//                startActivity(createTaskIntent);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
        return false;
    }


    private void refreshTasks()
    {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        restaurants = dbHelper.getIncompleteTasks();
        listAdapter.setTasks(restaurants);
        listAdapter.notifyDataSetInvalidated();
    }


    private void taskClicked(final Restaurant restaurant)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);


        //If the task has a message, display it in the dialog box
        if(restaurant.getAddress() != null && restaurant.getAddress().length() > 0)
        {
            dialogBuilder.setMessage(restaurant.getAddress());
        }
        dialogBuilder.setCancelable(true);
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
