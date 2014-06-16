package com.example.comsyslabAssignment4;

import java.util.ArrayList;

import com.example.comsyslabAssignment4.model.PlugInfo;
import com.example.comsyslabAssignment4.net.ClientPacketDispatcher;
import com.example.comsyslabAssignment4.net.ClientPacketDispatcher.OnPlugListReceivedListener;
import com.example.comsyslabAssignment4.constants.Constants;
import com.example.comsyslabAssignment4.constants.Constants.Status;
import com.example.comsyslabAssignment4.custom.PlugListAdapter;
import com.example.comsyslabAssignment4.custom.PlugListAdapter.OnPlugToggledListener;
import com.example.comsyslab_assignment4.R;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class PlugControlActivity extends Activity {

	// Thread handles
	private Thread mNetThread;
	
	// Network-related variables
	private String mServerIpAddress;
	private String mServPort;
	private short mLocalPort = 3535;
	private ClientPacketDispatcher mNetDispatcher;
	
	// GUI related variables
	private PlugListAdapter mPlugsAdapter;
	private ListView mPlugsListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plug_control);
		mPlugsListView = (ListView) findViewById(R.id.listView1);
		
		
		getServerInfo();
		
		mPlugsAdapter = new PlugListAdapter(this, new ArrayList<PlugInfo>());
		mNetDispatcher = new ClientPacketDispatcher(this,mServerIpAddress,mLocalPort,mServPort);
		mPlugsAdapter.setOnPlugToggledListener(new OnPlugToggledListener() {
			// Listen for any change to the switches occurred in the UI.
			@Override
			public void onPlugToggled(int plugId, Status plugNewStatus) {
				if(plugNewStatus == Status.ON) {
					mNetDispatcher.turnOn(plugId);
				} else
				{
					mNetDispatcher.turnOff(plugId);
				}
			}
			
		});
		mPlugsListView.setAdapter(mPlugsAdapter);
		
		
		mNetDispatcher.setOnPlugListReceivedListener(new OnPlugListReceivedListener () {
			// Whenever a new plug information is received, this is added to the adapter list.
			@Override
			public void onPlugListReceived(PlugInfo plug) {
				mPlugsAdapter.add(plug);
				Log.d(Constants.PlugControlActivityTag, String.format("New plug added with ID: %d", plug.getPlugId()));
			}
			
		});
		mNetThread = new Thread(mNetDispatcher);
		mNetThread.start();
	}
	
	private void getServerInfo() {
		Bundle extrasBundle = getIntent().getExtras();
		mServerIpAddress = extrasBundle.getString(Constants.ADDRESS_INPUT_EXTRA);
		if(mServerIpAddress == null)
			mServerIpAddress = "";
		mServPort = extrasBundle.getString(Constants.PORT_INPUT_EXTRA);
		if(mServPort == null)
			mServPort = "";
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mPlugsAdapter.clear();
		mNetDispatcher.onResume();
		mNetDispatcher.requestPlugList();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mNetDispatcher.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mNetDispatcher.closeSocket();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.plug_control, menu);
		return true;
	}

}
