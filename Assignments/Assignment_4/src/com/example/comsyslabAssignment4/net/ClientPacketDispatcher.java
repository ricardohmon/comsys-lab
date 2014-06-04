package com.example.comsyslabAssignment4.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.example.comsyslabAssignment4.constants.Constants;
import com.example.comsyslabAssignment4.constants.Constants.Status;
import com.example.comsyslabAssignment4.model.PlugInfo;

import android.content.Context;
import android.util.Log;


public class ClientPacketDispatcher implements Runnable {
	private Context context;
	private String serverIpAddress;
	private short serverPort;
	private InetAddress serverAddress;
	private DatagramSocket socket;
	private short localPort;
	private DatagramPacket sendPacket;
	private boolean newPacket;
	private OnPlugListReceivedListener mPlugListReceivedListener;
	
	public ClientPacketDispatcher(Context context, String serverIpAddress, short localPort, String serverPort) {
		this.context = context;
		this.localPort = localPort;
		this.serverIpAddress = serverIpAddress;
		this.serverPort = Short.parseShort(serverPort);
		
		try {
			serverAddress = InetAddress.getByName(this.serverIpAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		try {
			socket = new DatagramSocket(this.localPort);
			socket.setSoTimeout(3000);
		} catch (SocketException e) {
			Log.e(Constants.ClientPacketDispatcherTag, "Error while trying to create socket.", e);
		}
	}
	
	/*
	 * 		Interfaces
	 */
    public interface OnPlugListReceivedListener {
        public void onPlugListReceived(PlugInfo plug);
    }
    
    /*
	 * 		Listener setters
	 */
	public void setOnPlugListReceivedListener(OnPlugListReceivedListener listener) {
        this.mPlugListReceivedListener = listener;
    }
	
	
	public void requestPlugList()
	{
		this.sendPacket(Constants.TYPE_DISCOVER,0);
	}
	
	public void turnOff(int plugId)
	{
		this.sendPacket(Constants.TYPE_SWITCH_OFF,plugId);
	}
	
	public void turnOn(int plugId)
	{
		this.sendPacket(Constants.TYPE_SWITCH_ON,plugId);
	}
	
	@Override
	public void run() {
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf,buf.length);
		
		while(!Thread.currentThread().isInterrupted())
		{
			try {
				this.socket.receive(packet);
				if(this.newPacket)
				{
					Log.i(Constants.ClientPacketDispatcherTag, "New packet available to send.");
					this.socket.send(sendPacket);
					this.newPacket = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			decodePacket(packet.getData());
			//((Activity)this.context).runOnUiThread(new showOnUIThread(buf));
		}
	}
	private void sendPacket(int type, int plugId)
	{
		sendPacket = this.createPacket(type, plugId);
		newPacket = true;
	}
	
	private DatagramPacket createPacket(int type, int plugId)
	{
		 ByteBuffer buff;
		 byte[] buffarray;
		 DatagramPacket packet;
		 
		 switch(type)
		 {
		 case Constants.TYPE_DISCOVER :
			 buff = ByteBuffer.allocate(1);
			 buff.put((byte)Constants.TYPE_DISCOVER);
			 break;
		 case Constants.TYPE_SWITCH_ON :
			 buff = ByteBuffer.allocate(2);
			 buff.put((byte)Constants.TYPE_SWITCH_ON);
			 buff.put((byte)plugId);
			 break;
		 case Constants.TYPE_SWITCH_OFF :
			 buff = ByteBuffer.allocate(2);
			 buff.put((byte)Constants.TYPE_SWITCH_OFF);
			 buff.put((byte)plugId);
			 break;
		  default:
			  buff = ByteBuffer.allocate(0);
			  break;
		 }
		 buffarray = buff.array();
		 packet = new DatagramPacket(buffarray,buffarray.length,this.serverAddress,(int)this.serverPort);
		 return packet;
	}
	
	private void decodePacket(byte[] data)
	{
		if(data.length > 0 && data[0] == Constants.TYPE_PLUG_LIST) {
			int totalPlugs = data[1];
			int idx = 2;
			for(int plugCount = 0; plugCount < totalPlugs; plugCount++)
			{
				int plugId = data[idx++];
				Status status = data[idx++] == Constants.TYPE_STATUS_ON ? Status.ON : Status.OFF;
				String name = ByteBuffer.wrap(data, idx + 1, data[idx]).toString();
				idx += data[idx] + 1;
				this.mPlugListReceivedListener.onPlugListReceived(new PlugInfo(plugId,status,name));
			}
		}
	}
	/*
	class showOnUIThread implements Runnable {
        private byte[] data;
        public showOnUIThread(byte[] data) {
            this.data = data;
        }
        @Override
        public void run() {
        	if(data.length > 0 && data[0] == Constants.TYPE_SHAKE) {
    			String name = ByteBuffer.wrap(data, 10, data[9]).toString();
    			String date = DateFormat.getInstance().format((new Date((long)ByteBuffer.wrap(data,1,8).getLong() * 1000)));
    			Toast.makeText(ClientPacketDispatcher.this.context, String.format("Shake detected by %s at %s", name,date), Toast.LENGTH_SHORT).show();
    		}
        }
    }
    */
	
}
