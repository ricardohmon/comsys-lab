/*
 * 		Operational Mode Selection
 * 
 */

package com.example.comsyslabAssignment3;

import com.example.comsyslab_assignment3.R;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ModeSelectionActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Call the Cafeterias names resource and use it to populate the list on the activity.
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				getResources().getStringArray(R.array.operation_modes)));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		// Create a listener for each of the items, which will create an intent and pass it to the second activity with the number of the cafeteria chosen as an argument.
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent modeSelectedIntent;
				
				switch (position) {
				case 0 : 
					modeSelectedIntent = new Intent(ModeSelectionActivity.this,ServerMainActivity.class);
					break;
				default :
					modeSelectedIntent = new Intent(ModeSelectionActivity.this,ServerInfoActivity.class);
					break;
				}
				modeSelectedIntent.putExtra(com.example.comsyslabAssignment3.constants.Constants.CHOSEN_MODE_EXTRA, position);
				ModeSelectionActivity.this.startActivity(modeSelectedIntent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
