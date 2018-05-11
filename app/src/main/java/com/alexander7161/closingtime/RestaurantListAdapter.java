package com.alexander7161.closingtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;


public class RestaurantListAdapter extends BaseAdapter implements Filterable {

    private Context context;
    //Data
    private Activity activity;
    private ArrayList<Restaurant> restaurants;

    private ValueFilter valueFilter;
    private ArrayList<Restaurant> mStringFilterList;


    public RestaurantListAdapter(Context context, Activity activity, ArrayList<Restaurant> restaurants)
    {
        this.context = context;
        this.activity = activity;
        this.restaurants = restaurants;
        mStringFilterList =  restaurants;
        getFilter();
    }

    public void setRestaurants(ArrayList<Restaurant> restaurants)
    {
        this.restaurants = restaurants;
        mStringFilterList = restaurants;
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
        return i;
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


        if(restaurant.getCurrentDiscountBoolean()) {
            v.setBackgroundColor(Color.parseColor("#008000"));
        } else {
            v.setBackgroundColor(Color.WHITE);
        }
        title.setText(restaurant.getName());
        dueDate.setText(restaurant.getAddress());
        todayTime.setText(restaurant.getCurrentDiscount());

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

    @Override
    public Filter getFilter() {
        if(valueFilter==null) {

            valueFilter=new ValueFilter();
        }

        return valueFilter;
    }
    private class ValueFilter extends Filter {

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results=new FilterResults();
            if(constraint!=null && constraint.length()>0){
                ArrayList<Restaurant> filterList=new ArrayList<Restaurant>();
                for(int i=0;i<mStringFilterList.size();i++){
                    if((mStringFilterList.get(i).getName().toUpperCase() + " " + mStringFilterList.get(i).getAddress().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {
                        filterList.add(mStringFilterList.get(i));
                    }
                }
                results.count=filterList.size();
                results.values=filterList;
            }else{
                results.count=mStringFilterList.size();
                results.values=mStringFilterList;
            }
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            restaurants = (ArrayList<Restaurant>) results.values;
            notifyDataSetChanged();
        }
    }
    }
