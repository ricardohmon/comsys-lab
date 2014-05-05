package com.example.comsyslabAssignment2;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import com.example.comsyslabAssignment2.Model.MainDishItem;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MenuDisplayActivity extends Activity {
	
	TextView dataTextView;
	int cafeteria;
	ListView dishesList;
	Locale chosenLocale;
	

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dataTextView = (TextView) findViewById(R.id.dataTextView);
		 // Get the reference of ListView
        dishesList=(ListView)findViewById(R.id.listView1);
        
		if(savedInstanceState != null)
		{
			String savedLanguage = "";
			cafeteria = savedInstanceState.getInt(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_CAFETERIA_KEY,0);
			savedLanguage = savedInstanceState.getString(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_LANGUAGE_KEY);
			if(savedLanguage != null)
			{
				// Restore previously selected language
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
			Bundle extrasBundle = getIntent().getExtras();
			cafeteria = extrasBundle.getInt(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_CAFETERIA_EXTRA, 0);	
		}
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
	
	private void changeLanguage() {
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
	    new HttpGetTask(this).execute();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_CAFETERIA_KEY, cafeteria);
		savedInstanceState.putString(com.example.comsyslabAssignment2.Model.Constants.CHOSEN_LANGUAGE_KEY, chosenLocale.toString());
	}
	

    
	private class HttpGetTask extends AsyncTask<Void, Void, String> {

		private Context mContext;
		private String requestSite;
		private String HTTP_GET_COMMAND;
		
	    public HttpGetTask (Context context){
	         mContext = context;
	    }
	    
	    
		@Override
		protected String doInBackground(Void... params) {
			String data = "";
			//Log.i(com.example.comsyslabAssignment2.Model.Constants.HTTP_GET_TAG, MensaJNILib.stringFromJNI());
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
			//Log.i(com.example.comsyslabAssignment2.Model.Constants.HTTP_GET_TAG, result);
			com.example.comsyslabAssignment2.Model.Menu menu = new com.example.comsyslabAssignment2.Model.Menu(result);
			
			// Create The Adapter with passing ArrayList as 3rd parameter
			// TODO: Delete this temporary list
			ArrayList<String> dishes = new ArrayList<String>();
			for(Iterator<MainDishItem> it = menu.getMainDishes().iterator(); it.hasNext();)
			{
				dishes.add(it.next().getDishName());
			}
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1, dishes);
            // Set The Adapter
            dishesList.setAdapter(arrayAdapter); 
		}
	}

}
