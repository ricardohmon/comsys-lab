/*
 * 		Custom ArrayAdapter Guideline obtained from:
 * 		https://github.com/thecodepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 * 
 */

package com.example.comsyslabAssignment2.CustomAdapters;

import java.util.List;

import com.example.comsyslabAssignment2.Model.MainDishItem;
import com.example.comsyslab_assignment2.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainDishAdapter extends ArrayAdapter<MainDishItem> {

	private static class ViewHolder {
		TextView dishName;
		TextView dishDescription;
		TextView dishNV;
		TextView dishPrice;
	}
	
	public MainDishAdapter(Context context, List<MainDishItem> objects) {
		super(context, com.example.comsyslab_assignment2.R.layout.item_menu, objects);
		Log.d(com.example.comsyslabAssignment2.Model.Constants.MAIN_DISH_ADAPTER_TAG, String.format("Mains dishes: %d", objects.size()));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		// Get the data item for this position
		Log.d(com.example.comsyslabAssignment2.Model.Constants.MAIN_DISH_ADAPTER_TAG, String.format("Object called at position: %d", position));
		MainDishItem mainDish = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		ViewHolder viewHolder; // view lookup cache stored in tag
		if(convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(com.example.comsyslab_assignment2.R.layout.item_menu, parent,false);
			viewHolder.dishName = (TextView) convertView.findViewById(R.id.dishName);
			viewHolder.dishDescription = (TextView) convertView.findViewById(R.id.dishDescription);
			viewHolder.dishNV = (TextView) convertView.findViewById(R.id.dishNV);
			viewHolder.dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// Populate the data into the template view using the data object
	    viewHolder.dishName.setText(mainDish.getDishName());
	    viewHolder.dishDescription.setText(mainDish.getDescription());
	    viewHolder.dishNV.setText(mainDish.getNutritionalValue());
	    viewHolder.dishPrice.setText(mainDish.getPrice());
	    // Return the completed view to render on screen
	    return convertView;
	}

}
