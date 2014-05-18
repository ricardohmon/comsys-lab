package com.example.comsyslab_assignment3;

import com.example.comsyslab_assignment3.constants.Constants;
import com.example.comsyslab_assignment3.hw.SensorController;
import com.example.comsyslab_assignment3.hw.SensorController.OnNewDataListener;
import com.example.comsyslab_assignment3.model.Accelerometer;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SensorController<Accelerometer> accelerometerController;
	private LinearLayout mainView;
	private TextView xView;
	private TextView yView;
	private TextView zView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mainView = (LinearLayout) findViewById(R.id.mainLayout); // Get a reference to the main layout
		xView = (TextView) findViewById(R.id.xValue);
		yView = (TextView) findViewById(R.id.yValue);
		zView = (TextView) findViewById(R.id.zValue);
		
		final GraphView sensorGraphVw = new GraphView(this); // Create an instance of the custom view
		mainView.addView(sensorGraphVw);					// add it to the layout
		
		accelerometerController = new SensorController<Accelerometer>(new Accelerometer(),this); // Create an instance of the SensorController class specifying the Accelerometer sensor
		accelerometerController.setOnNewDataListener(new OnNewDataListener() {
			// The sensor controller has a listener where we can specify the action to carry on whenever a new data is available.
			@Override
			public void onNewData(float[] values)
			{
				sensorGraphVw.addNewValues(values); // Add the new value to the graph
				xView.setText(String.format("%.3f", values[0]));
				yView.setText(String.format("%.3f", values[1]));
				zView.setText(String.format("%.3f", values[2]));
			}
		});
		Log.i(Constants.MainActivityTag, "Launching thread...");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected void onResume() {
		super.onResume();
		
		accelerometerController.registerSensor(); // Register sensor until this step so we don't waste energy
	}
	
	protected void onPause() {
		super.onPause();
		accelerometerController.unregisterSensor(); // Make sure to unregister when the activity is closed.
	}

}
