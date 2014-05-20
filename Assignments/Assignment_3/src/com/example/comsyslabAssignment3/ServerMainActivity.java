package com.example.comsyslabAssignment3;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;

import com.example.comsyslabAssignment3.constants.Constants;
import com.example.comsyslab_assignment3.R;

import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

public class ServerMainActivity extends Activity {
	private TextView mTextView;
	private TextView mIpAddressView;
	private DatagramSocket serverSocket;
	public static final int SERVERPORT = 2345;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_main);
		// Show the Up button in the action bar.
		setupActionBar();
		mIpAddressView = (TextView) findViewById(R.id.serverIpAddressView);
		try {
			mIpAddressView.setText(getDeviceIpAddress());
		} catch (SocketException e) {
			Log.e(com.example.comsyslabAssignment3.constants.Constants.ServerMainActivityTag,"Error while looking for IP Address");
		}
		mTextView = (TextView) findViewById(R.id.textView1);
		Log.i(com.example.comsyslabAssignment3.constants.Constants.ServerMainActivityTag,"Launching new Thread");
		new Thread(new ConnectionThread(this)).start();
	}
	
	private String getDeviceIpAddress() throws SocketException {
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		    NetworkInterface intf = en.nextElement();
		    if (intf.getName().contains("wlan")) {
		        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		            InetAddress inetAddress = enumIpAddr.nextElement();
		            if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
		                return inetAddress.getHostAddress();
		            }
		        }
		    }
		}
		return "";
		
	}
	
	class ConnectionThread implements Runnable {
		Context context;
		
		public ConnectionThread(Context context)
		{
			this.context = context;
		}
		@Override
		public void run() {
			try {
				Log.i(com.example.comsyslabAssignment3.constants.Constants.ServerMainActivityTag,"Creating new Datagram Socket");
				serverSocket = new DatagramSocket(SERVERPORT);
			} catch(IOException e)
			{
				e.printStackTrace();
			}
			
			while(!Thread.currentThread().isInterrupted()) {
				byte[] buf = new byte[256];
				DatagramPacket packet = new DatagramPacket(buf,buf.length);
				
				try {
					Log.i(com.example.comsyslabAssignment3.constants.Constants.ServerMainActivityTag,"Listening for new packets");
					serverSocket.receive(packet);
					Log.i(com.example.comsyslabAssignment3.constants.Constants.ServerMainActivityTag,String.format("Packet received from address: ",packet.getAddress().getHostAddress()));
					((Activity)this.context).runOnUiThread(new updateUIThread(interpretMessage(packet.getData())));
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private String interpretMessage(byte[] data)
		{
			String message = "";
			if(data.length > 0)
			{
				switch(data[0])
				{
					case Constants.TYPE_REGISTER : message= String.format("New device registered: %s", new String(data,2,data[1])); break;
					case Constants.TYPE_UNREGISTER : message= String.format("Device unregistered."); break;
					case Constants.TYPE_KEEPALIVE : message = String.format("Keep alive message"); break;
					case Constants.TYPE_EVENT : message= String.format("Shake detected at %s", DateFormat.getInstance().format((new Date((long)ByteBuffer.wrap(data,1,8).getLong() * 1000)))); break;
					default : message= String.format("Some other message."); break;
				}
			}
			return message;
		}
	}
	
	class updateUIThread implements Runnable {
		        private String msg;
		        public updateUIThread(String str) {
		            this.msg = str;
		        }
		        @Override
		        public void run() {
		            mTextView.setText(msg);
		        }
		    }

	protected void onStop() {
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onStop();
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
