package com.example.comsyslabAssignment3.constants;


public final class Constants {
	public final static String ClientMainActivityTag = "ClientMainActivity";
	
	public final static String ServerMainActivityTag = "ServerMainActivity";
	
	public final static String DrawViewTag = "DrawView";
	
	public final static String SensorControllerTag = "SensorController";
	
	public final static String ClientPacketDispatcherTag = "ClientPacketDispatcher";
	
	public final static String ClientPacketListenerTag = "ClientPacketListener";
	
	public final static String CHOSEN_MODE_EXTRA = "chosen_mode";
	
	public final static String ADDRESS_INPUT_EXTRA = "address_input";
	
	public final static String PORT_INPUT_EXTRA = "port_input";
	
	/*
	 * 	Message Types
	 */
	public final static int TYPE_REGISTER  = 1;
	public final static int TYPE_UNREGISTER = 2;
	public final static int TYPE_KEEPALIVE = 3;
	public final static int TYPE_EVENT = 4;
	public final static int TYPE_SHAKE = 5;
}
