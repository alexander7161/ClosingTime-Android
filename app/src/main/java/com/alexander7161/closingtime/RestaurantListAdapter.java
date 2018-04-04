package com.alexander7161.closingtime;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class RestaurantListAdapter extends BaseAdapter
{
    //Data
    private Activity activity;
    private ArrayList<Restaurant> restaurants = null;

    public RestaurantListAdapter(Activity activity, ArrayList<Restaurant> restaurants)
    {
        this.activity = activity;
        this.restaurants = restaurants;
    }

    public void setTasks(ArrayList<Restaurant> restaurants)
    {
        this.restaurants = restaurants;
    }



    //Adapter components
    @Override
    public int getCount() {
        return restaurants == null ? 0 : restaurants.size();
    }

    @Override
    public Object getItem(int i) {
        return restaurants.get(i);
    }

    @Override
    public long getItemId(int i) {
        Restaurant r = (Restaurant) getItem(i);
        return r == null ? -1 : r.getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if(v == null)
        {
            v = activity.getLayoutInflater().inflate(R.layout.restaurant_list_item,viewGroup,false);
        }

        TextView title = v.findViewById(R.id.title_view);
        TextView dueDate = v.findViewById(R.id.due_date);
        Restaurant restaurant = (Restaurant) getItem(i);

        title.setText(restaurant.getName());
        dueDate.setText(restaurant.getAddress());

        return v;
    }
}
