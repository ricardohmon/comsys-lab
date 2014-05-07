/*
 * 		MenuDisplayActivity
 * 		Support activity, whose purpose is to display the menu of the chosen cafeteria.
 * 
 */

package com.example.comsyslabAssignment2;



import java.util.ArrayList;
import java.util.Locale;

import com.example.comsyslabAssignment2.CustomAdapters.MainDishAdapter;
import com.example.comsyslabAssignment2.CustomAdapters.SideDishAdapter;
import com.example.comsyslabAssignment2.Model.MainDishItem;
import com.example.comsyslabAssignment2.Model.SideDishItem;
import com.example.comsyslabAssignment2.Native.MensaJNILib;
import com.example.comsyslab_assignment2.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class MenuDisplayActivity extends Activity {
	
	int cafeteria;
	ListView maindishesList;
	ListView sidedishesList;
	Locale chosenLocale;
	MainDishAdapter adapter;
	SideDishAdapter side_dishes_adapter;
	View maindishesHeader;
	View sidedishesHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // Set main layout to the activity
		// Get the reference to the Views, which will be used later
		TextView dataTextView = (TextView) findViewById(R.id.textView1);
		 // Get the reference of ListView
        maindishesList=(ListView)findViewById(R.id.listView1);
        sidedishesList=(ListView)findViewById(R.id.listView2);
        
        // Check if there is persisting state from a previous session(e.g. device rotation) in order to restore it.
		if(savedInstanceState != null)
		{
			String savedLanguage = "";
			cafeteria = savedInstanceState.getInt(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_CAFETERIA_KEY,0); // Key used to remember the cafeteria election.
			savedLanguage = savedInstanceState.getString(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_LANGUAGE_KEY); // Key used to remember the language election.
			if(savedLanguage != null)
			{
				// Restore previously selected language in the app's configuration
				Resources res = this.getResources();
			    // Change locale settings in the app.
			    DisplayMetrics dm = res.getDisplayMetrics();
			    android.content.res.Configuration conf = res.getConfiguration();
			    conf.locale = new Locale(savedLanguage);
			    res.updateConfiguration(conf, dm);	
			}
		}
		else
		{
			// If there was no saved state, extract the selection from the intent and assign default values to the variables.
			Bundle extrasBundle = getIntent().getExtras();
			cafeteria = extrasBundle.getInt(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_CAFETERIA_EXTRA, 0);
			chosenLocale = this.getResources().getConfiguration().locale;
		}
		// Set the name of the current cafeteria
		String[] cafeterias_names = getResources().getStringArray(R.array.cafeterias);
		dataTextView.setText(cafeterias_names[cafeteria]);
		//Trigger Asynchronous task to make the HTTP request.
		new HttpGetTask(this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_change_language:
	            changeLanguage();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/*
	 *  Method in charge of toggling the language between English and German.
	 */
	private void changeLanguage() {
		// Clear the Lists' content and headers before appending new ones.
		adapter.clear();
		maindishesList.removeHeaderView(maindishesHeader);
		sidedishesList.removeHeaderView(sidedishesHeader);
		side_dishes_adapter.clear();

		// Obtain language configuration
		Resources res = this.getResources();
	    // Change locale settings in the app.
	    DisplayMetrics dm = res.getDisplayMetrics();
	    android.content.res.Configuration conf = res.getConfiguration();
	    if (conf.locale.getLanguage().equalsIgnoreCase(Locale.ENGLISH.toString()))
	    {
	    	chosenLocale = Locale.GERMAN;
	    } else {
	    	chosenLocale = Locale.ENGLISH;
	    }
	    conf.locale = chosenLocale;
	    Locale.setDefault(conf.locale);
	    res.updateConfiguration(conf, dm);
	    // Trigger the Http request again.
	    new HttpGetTask(this).execute();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Store activity's current state
		savedInstanceState.putInt(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_CAFETERIA_KEY, cafeteria);
		savedInstanceState.putString(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_LANGUAGE_KEY, chosenLocale.toString());
	}
	

    /*
     * 		Asynchronous Task which handles the Http request
     */
	private class HttpGetTask extends AsyncTask<Void, Void, String> {

		private Context mContext;
		private String requestSite;
		private String HTTP_GET_COMMAND;
		
		/*
		 * 		Single Constructor that receives the activity's context as a parameter and initializes the custom adapters. 
		 */
	    public HttpGetTask (Context context){
	         mContext = context;
	         adapter = new MainDishAdapter(mContext, new ArrayList<MainDishItem>());
	         side_dishes_adapter = new SideDishAdapter(mContext, new ArrayList<SideDishItem>());
	    }
	    
	    /*
	     * Piece of code which creates the HTTP request using the proper page's URL and hostname.
	     * In addition, calls the JNI function wrapper and waits for the result.
	     */
		@Override
		protected String doInBackground(Void... params) {
			String data = "";
			String[] cafeterias_paths = getResources().getStringArray(R.array.cafeterias_paths);
			this.requestSite = cafeterias_paths[cafeteria];
			
			// Build get request header
			HTTP_GET_COMMAND = "GET /mensa/"
					+ this.requestSite
					+ " HTTP/1.1"
					+ "\n"
					+ "Host: "
					+ com.example.comsyslabAssignment2.Model.Constants.HOST
					+ "\n"
					+ "Connection: close" + "\n\n";
			Log.i(com.example.comsyslabAssignment2.Model.Constants.HTTP_GET_TAG, HTTP_GET_COMMAND);
			data = MensaJNILib.handleRequest(com.example.comsyslabAssignment2.Model.Constants.HOST,HTTP_GET_COMMAND);
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			com.example.comsyslabAssignment2.Model.Menu menu = new com.example.comsyslabAssignment2.Model.Menu(result);
			
			// Create the adapter to convert the array to views. Use the previously parsed menu items.
			// Populating Main dishes
			maindishesHeader = (View) getLayoutInflater().inflate(R.layout.maindishes_listview_header_row, null);
			maindishesList.addHeaderView(maindishesHeader);
			adapter.addAll(menu.getMainDishes());
			maindishesList.setAdapter(adapter);
			
			// Populating Side dishes
	        sidedishesHeader = (View) getLayoutInflater().inflate(R.layout.sidedishes_listview_header_row, null);
	        sidedishesList.addHeaderView(sidedishesHeader);
			side_dishes_adapter.addAll(menu.getSideDishes());
			sidedishesList.setAdapter(side_dishes_adapter);
			Log.i(com.example.comsyslabAssignment2.Model.Constants.HTTP_GET_TAG, "Done");
		}
	}

}
