/*
 * 		Custom ArrayAdapter Guideline obtained from:
 * 		https://github.com/thecodepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 * 
 */

package com.example.comsyslabAssignment2.CustomAdapters;

import java.util.List;

import com.example.comsyslabAssignment2.Model.SideDishItem;
import com.example.comsyslab_assignment2.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SideDishAdapter extends ArrayAdapter<SideDishItem> {

	private static class ViewHolder {
		TextView dishName;
		TextView dishDescription;
		TextView dishNV;
	}
	
	public SideDishAdapter(Context context, List<SideDishItem> objects) {
		super(context, com.example.comsyslab_assignment2.R.layout.sidedish_item_menu, objects);
		Log.d(com.example.comsyslabAssignment2.Model.Constants.SIDE_DISH_ADAPTER_TAG, String.format("Side dishes: %d", objects.size()));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		// Get the data item for this position
		Log.d(com.example.comsyslabAssignment2.Model.Constants.SIDE_DISH_ADAPTER_TAG, String.format("Object called at position: %d", position));
		SideDishItem sideDish = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		ViewHolder viewHolder; // view lookup cache stored in tag
		if(convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(com.example.comsyslab_assignment2.R.layout.sidedish_item_menu, parent,false);
			viewHolder.dishName = (TextView) convertView.findViewById(R.id.dishName);
			viewHolder.dishDescription = (TextView) convertView.findViewById(R.id.dishDescription);
			viewHolder.dishNV = (TextView) convertView.findViewById(R.id.dishNV);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// Populate the data into the template view using the data object
	    viewHolder.dishName.setText(sideDish.getDishName());
	    viewHolder.dishDescription.setText(sideDish.getDescription());
	    viewHolder.dishNV.setText(sideDish.getNutritionalValue());
	    // Return the completed view to render on screen
	    return convertView;
	}

}
