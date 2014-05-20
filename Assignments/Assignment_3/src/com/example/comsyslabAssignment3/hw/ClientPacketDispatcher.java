package com.example.comsyslabAssignment3.hw;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.example.comsyslabAssignment3.Native.ClientJNILib;
import com.example.comsyslabAssignment3.constants.Constants;

import android.util.Log;

//public class ClientNetworkDispatcher implements Runnable {
public class ClientPacketDispatcher {
	private String serverIpAddress;
	private String serverPort;
	private DatagramSocket socket;
	private InetAddress address;
	private Boolean shakePending;
	private Boolean pendingRegistration;
	private Boolean pendingDeregistration;
	
	public ClientPacketDispatcher(String serverIpAddress, String serverPort) {
		this.shakePending = false;
		this.pendingRegistration = false;
		this.pendingDeregistration = false;
		this.serverIpAddress = serverIpAddress;
		this.serverPort = serverPort;
		try {
			this.address = InetAddress.getByName(serverIpAddress);
		} catch (UnknownHostException e) {
			Log.e(Constants.ClientPacketDispatcherTag, "Not able to parse IP address", e);
		}
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			Log.e(Constants.ClientPacketDispatcherTag, "Error while trying to create socket.", e);
		}
	}
	
	public void registerWithServer()
	{
		Log.i(Constants.ClientPacketDispatcherTag, "About to register");
		//this.pendingRegistration = true;
		sendRegisterPacket();
	}
	
	public void deregisterWithServer()
	{
		Log.i(Constants.ClientPacketDispatcherTag, "About to unregister");
		sendDeregisterPacket();
		//this.pendingDeregistration = true;
	}
	
	public void sendShakeNotification()
	{
		Log.i(Constants.ClientPacketDispatcherTag, "Sending shake message");
		sendShakePacket();
		//this.shakePending = true;
	}
	
	public void sendKeepAlive()
	{
		Log.i(Constants.ClientPacketDispatcherTag, "Sending keep alive message.");
		ClientJNILib.keepAlive(this.serverIpAddress);
	}
	
	private void sendRegisterPacket()
	{
		ClientJNILib.register(this.serverIpAddress, "Hello");
		//this.pendingRegistration = false;
	}
	
	private void sendShakePacket()
	{
		ClientJNILib.sendEvent(this.serverIpAddress);
		//this.shakePending = false;
	}
	
	private void sendDeregisterPacket()
	{
		ClientJNILib.unregister(this.serverIpAddress);
	}
	/*
	@Override
	public void run() {
		if(this.pendingRegistration)
			sendRegisterPacket();
		
		if(this.shakePending)
			sendShakePacket();
		
		if(this.pendingDeregistration)
			sendDeregisterPacket();
		
	}
	*/
	
	
}
