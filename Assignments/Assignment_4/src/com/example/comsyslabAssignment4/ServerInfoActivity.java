package com.example.comsyslabAssignment4;

import com.example.comsyslab_assignment4.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class ServerInfoActivity extends Activity {
	
	private EditText mIpAddressInput;
	private EditText mPortNumberInput;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_info);
		// Show the Up button in the action bar.
		setupActionBar();
		mIpAddressInput = (EditText) findViewById(R.id.ipaddressinput);
		mPortNumberInput = (EditText) findViewById(R.id.portinput);
	}

	public void onButton1Click(View sender) {
		String ipAddress = mIpAddressInput.getText().toString();
		String portNumber = mPortNumberInput.getText().toString();
		Intent clientActivityintent = new Intent(ServerInfoActivity.this,PlugControlActivity.class);
		clientActivityintent.putExtra(com.example.comsyslabAssignment4.constants.Constants.ADDRESS_INPUT_EXTRA, ipAddress);
		clientActivityintent.putExtra(com.example.comsyslabAssignment4.constants.Constants.PORT_INPUT_EXTRA, portNumber);
		ServerInfoActivity.this.startActivity(clientActivityintent);
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


}
