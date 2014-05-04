package com.example.comsyslab_assignment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import com.example.comsyslab_assignment2.Model.Cafeterias;
import com.example.comsyslab_assignment2.Model.MainDishItem;

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
			cafeteria = savedInstanceState.getInt(com.example.comsyslab_assignment2.Model.Constants.CHOSEN_CAFETERIA_KEY,0);
			savedLanguage = savedInstanceState.getString(com.example.comsyslab_assignment2.Model.Constants.CHOSEN_LANGUAGE_KEY);
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
			cafeteria = extrasBundle.getInt(com.example.comsyslab_assignment2.Model.Constants.CHOSEN_CAFETERIA_EXTRA, 0);	
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
		savedInstanceState.putInt(com.example.comsyslab_assignment2.Model.Constants.CHOSEN_CAFETERIA_KEY, cafeteria);
		savedInstanceState.putString(com.example.comsyslab_assignment2.Model.Constants.CHOSEN_LANGUAGE_KEY, chosenLocale.toString());
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
			Socket socket = null;
			String data = "";
			String[] cafeterias_paths = getResources().getStringArray(R.array.cafeterias_paths);
			this.requestSite = cafeterias_paths[cafeteria];
			
			// Build get request header
			HTTP_GET_COMMAND = "GET /mensa/"
					+ this.requestSite
					+ " HTTP/1.1"
					+ "\n"
					+ "Host: "
					+ com.example.comsyslab_assignment2.Model.Constants.HOST
					+ "\n"
					+ "Connection: close" + "\n\n";
			Log.i(com.example.comsyslab_assignment2.Model.Constants.HTTP_GET_TAG, HTTP_GET_COMMAND);
			try {
				socket = new Socket(com.example.comsyslab_assignment2.Model.Constants.HOST, 80);
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(
						socket.getOutputStream()), true);
				pw.println(HTTP_GET_COMMAND);

				data = readStream(socket.getInputStream());

			} catch (UnknownHostException exception) {
				exception.printStackTrace();
			} catch (IOException exception) {
				exception.printStackTrace();
			} finally {
				if (null != socket)
					try {
						socket.close();
					} catch (IOException e) {
						Log.e(com.example.comsyslab_assignment2.Model.Constants.HTTP_GET_TAG, "IOException",e);
					}
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			
			com.example.comsyslab_assignment2.Model.Menu menu = new com.example.comsyslab_assignment2.Model.Menu(result);
			
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
			//dataTextView.setText(parseHtml(result));
		}

		private String readStream(InputStream in) {
			BufferedReader reader = null;
			StringBuffer data = new StringBuffer();
			try {
				reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				while ((line = reader.readLine()) != null) {
					data.append(line);
				}
			} catch (IOException e) {
				Log.e(com.example.comsyslab_assignment2.Model.Constants.HTTP_GET_TAG, "IOException",e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						Log.e(com.example.comsyslab_assignment2.Model.Constants.HTTP_GET_TAG, "IOException",e);
					}
				}
			}
			return data.toString();
		}
	}

}
