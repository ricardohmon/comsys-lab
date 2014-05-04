package com.example.comsyslab_assignment2;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MensaSelectionActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				getResources().getStringArray(R.array.cafeterias)));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent menuDisplayActivityintent = new Intent(MensaSelectionActivity.this,MenuDisplayActivity.class);
				menuDisplayActivityintent.putExtra(com.example.comsyslab_assignment2.Model.Constants.CHOSEN_CAFETERIA_EXTRA, position);
				MensaSelectionActivity.this.startActivity(menuDisplayActivityintent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mensa_selection, menu);
		return true;
	}

}
