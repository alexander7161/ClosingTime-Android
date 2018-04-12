package com.alexander7161.closingtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;


public class RestaurantListAdapter extends BaseAdapter {

    private Context context;
    //Data
    private Activity activity;
    private ArrayList<Restaurant> restaurants = null;

    public RestaurantListAdapter(Context context, Activity activity, ArrayList<Restaurant> restaurants)
    {
        this.context = context;
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = view;
        if(v == null)
        {
            v = activity.getLayoutInflater().inflate(R.layout.restaurant_list_item,viewGroup,false);
        }

        TextView title = v.findViewById(R.id.title_view);
        TextView dueDate = v.findViewById(R.id.due_date);
        TextView todayTime = v.findViewById(R.id.today_time);
        final Restaurant restaurant = (Restaurant) getItem(i);

        title.setText(restaurant.getName());
        dueDate.setText(restaurant.getAddress());
        //todayTime.setText(restaurant.getCurrentDiscount());

        Button navigateButton = v.findViewById(R.id.navigate_button);

        navigateButton.setOnClickListener(v1 -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + restaurant.getLongAddress());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        });

        v.setOnClickListener(v12 -> taskClicked((Restaurant) getItem(i)));

        return v;
    }

    private void taskClicked(final Restaurant restaurant)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);


        //If the task has a message, display it in the dialog box
        if(restaurant.getAddress() != null && restaurant.getAddress().length() > 0)
        {
            dialogBuilder.setMessage(restaurant.getAddress() +
                    " M: " + restaurant.getClosingTime(0) +
                    " T: " + restaurant.getClosingTime(1) +
                    " W: " + restaurant.getClosingTime(2) +
                    " T: " + restaurant.getClosingTime(3) +
                    " F: " + restaurant.getClosingTime(4) +
                    " S: " + restaurant.getClosingTime(5) +
                    " S: " + restaurant.getClosingTime(6));
        }
        dialogBuilder.setCancelable(true);
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
