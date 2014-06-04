package com.example.comsyslabAssignment4;

import java.util.ArrayList;

import com.example.comsyslabAssignment4.model.PlugInfo;
import com.example.comsyslabAssignment4.net.ClientPacketDispatcher;
import com.example.comsyslabAssignment4.net.ClientPacketDispatcher.OnPlugListReceivedListener;
import com.example.comsyslabAssignment4.constants.Constants;
import com.example.comsyslab_assignment4.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PlugControlActivity extends Activity {

	// Network-related variables
	private String mServerIpAddress;
	private String mServPort;
	private short mLocalPort = 3535;
	private ArrayList<PlugInfo> plugs;
	private ClientPacketDispatcher mNetDispatcher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plug_control);
		plugs = new ArrayList<PlugInfo>();
		
		getServerInfo();
		
		mNetDispatcher = new ClientPacketDispatcher(this,mServerIpAddress,mLocalPort,mServPort);
		mNetDispatcher.setOnPlugListReceivedListener(new OnPlugListReceivedListener () {
			
			@Override
			public void onPlugListReceived(PlugInfo plug) {
				plugs.add(plug);
			}
			
		});
		new Thread(mNetDispatcher).start();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.plug_control, menu);
		return true;
	}

}
