package com.example.comsyslabAssignment3;

import com.example.comsyslabAssignment3.constants.Constants;
import com.example.comsyslabAssignment3.hw.ClientPacketDispatcher;
import com.example.comsyslabAssignment3.hw.SensorController;
import com.example.comsyslabAssignment3.hw.SensorController.OnNewDataListener;
import com.example.comsyslabAssignment3.hw.SensorController.OnShakeDetectedListener;
import com.example.comsyslabAssignment3.model.Accelerometer;
import com.example.comsyslab_assignment3.R;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ClientMainActivity extends Activity {
	private final long KEEPALIVE_INTERVAL = 15000;
	private SensorController<Accelerometer> accelerometerController;
	private LinearLayout mainView;
	private TextView xView;
	private TextView yView;
	private TextView zView;
	// Network-related variables
	private String mServerIpAddress;
	private String mServPort;
	private ClientPacketDispatcher mNetDispatcher;
	// Registration buttons
	private CheckBox registerCheckBx;
	private Boolean deviceIsRegistered;
	private Thread keepAliveThread;
	private Handler handler = new Handler(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.deviceIsRegistered = false;
		setContentView(R.layout.activity_main);
		mainView = (LinearLayout) findViewById(R.id.mainLayout); // Get a reference to the main layout
		xView = (TextView) findViewById(R.id.xValue);
		yView = (TextView) findViewById(R.id.yValue);
		zView = (TextView) findViewById(R.id.zValue);
		registerCheckBx = (CheckBox) findViewById(R.id.checkBox1);
		
		getServerInfo();
		
		mNetDispatcher = new ClientPacketDispatcher(mServerIpAddress,mServPort);
		keepAliveThread = new Thread(new KeepAliveTask());
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
		
		accelerometerController.setOnShakeDetectedListener(new OnShakeDetectedListener () {

			@Override
			public void onShakeDetected() {
				if(deviceIsRegistered)
					mNetDispatcher.sendShakeNotification();
			}
			
		});
		
		registerCheckBx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					mNetDispatcher.registerWithServer();
					keepAliveThread.run();
					ClientMainActivity.this.deviceIsRegistered = true;
				}
				else
				{
					mNetDispatcher.deregisterWithServer();
					ClientMainActivity.this.deviceIsRegistered = false;
				}
			}
		});
			
	}

	private void getServerInfo() {
		Bundle extrasBundle = getIntent().getExtras();
		mServerIpAddress = extrasBundle.getString(com.example.comsyslabAssignment3.constants.Constants.ADDRESS_INPUT_EXTRA);
		if(mServerIpAddress == null)
			mServerIpAddress = "";
		mServPort = extrasBundle.getString(com.example.comsyslabAssignment3.constants.Constants.PORT_INPUT_EXTRA);
		if(mServPort == null)
			mServPort = "";
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
	
	private class KeepAliveTask implements Runnable {
		@Override
		public void run() {
				Log.i(Constants.ClientMainActivityTag,"Placing a delayed message.");
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if(ClientMainActivity.this.deviceIsRegistered) {
							ClientMainActivity.this.mNetDispatcher.sendKeepAlive();
							KeepAliveTask.this.run();
						}
					}
				}, KEEPALIVE_INTERVAL);
		}
	}

}
