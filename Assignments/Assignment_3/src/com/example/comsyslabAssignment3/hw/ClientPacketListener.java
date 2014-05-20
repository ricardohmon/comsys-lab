package com.example.comsyslabAssignment3.hw;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

import com.example.comsyslabAssignment3.constants.Constants;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ClientPacketListener implements Runnable {

	private Context activityContext;
	private int port;
	private DatagramSocket serverSocket;
	
	public ClientPacketListener(Context context, int localPort)
	{
		this.activityContext = context;
		this.port = localPort;
	}
	
	@Override
	public void run() {
		try {
			Log.i(Constants.ClientPacketListenerTag,"Creating new Datagram Socket");
			serverSocket = new DatagramSocket(this.port);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		while(!Thread.currentThread().isInterrupted()) {
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf,buf.length);
			
			try {
				Log.i(Constants.ClientPacketListenerTag,"Listening for new packets");
				serverSocket.receive(packet);
				decodePacket(packet.getData());
				
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void decodePacket(byte[] data)
	{
		if(data.length > 0 && data[0] == Constants.TYPE_SHAKE) {
			String name = ByteBuffer.wrap(data, 10, data[9]).toString();
			String date = DateFormat.getInstance().format((new Date((long)ByteBuffer.wrap(data,1,8).getLong() * 1000)));
			Toast.makeText(this.activityContext, String.format("Shake detected by %s at %s", name,date), Toast.LENGTH_SHORT).show();
		}
	}

}
