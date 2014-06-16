package com.example.comsyslabAssignment4.custom;

import java.util.List;

import com.example.comsyslabAssignment4.constants.Constants.Status;
import com.example.comsyslabAssignment4.model.PlugInfo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.comsyslab_assignment4.R;

public class PlugListAdapter extends ArrayAdapter<PlugInfo> {

	private OnPlugToggledListener mPlugToggledListener;
	
	private static class ViewHolder {
		TextView plugName;
		ToggleButton plugStatus;
	}
	
	public PlugListAdapter(Context context, List<PlugInfo> objects) {
		super(context, R.layout.item_plug, objects);
	}
	
	/*
	 * 		Interfaces
	 */
	public interface OnPlugToggledListener {
		public void onPlugToggled(int plugId, Status plugNewStatus);
	}
	
	/*
	 * 		Listener setters
	 */
	public void setOnPlugToggledListener(OnPlugToggledListener listener) {
		this.mPlugToggledListener = listener;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		final PlugInfo plug = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		ViewHolder viewHolder; // view lookup cache stored in tag
		if(convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.item_plug, parent,false);
			viewHolder.plugName = (TextView) convertView.findViewById(R.id.plugNameTextView);
			viewHolder.plugStatus = (ToggleButton) convertView.findViewById(R.id.plugSwitchButton);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// Populate the data into the template view using the data object
	    viewHolder.plugName.setText(plug.getName());
	    viewHolder.plugStatus.setChecked(plug.getStatus() == Status.ON);
	    viewHolder.plugStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				mPlugToggledListener.onPlugToggled(plug.getPlugId(), isChecked? Status.ON : Status.OFF);
			}
			
	    });
	    // Return the completed view to render on screen
	    return convertView;
	}

}
