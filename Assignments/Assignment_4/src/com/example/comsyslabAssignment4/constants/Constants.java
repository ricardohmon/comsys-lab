package com.example.comsyslabAssignment4.constants;


public final class Constants {
	public final static String PlugControlActivityTag = "PlugControlActivity";
	
	public final static String ClientPacketDispatcherTag = "ClientPacketDispatcher";
	
	public final static String ADDRESS_INPUT_EXTRA = "address_input";
	
	public final static String PORT_INPUT_EXTRA = "port_input";
	
	/*
	 * 	Message Types
	 */
	public final static int TYPE_DISCOVER  = 1;
	public final static int TYPE_PLUG_LIST = 2;
	public final static int TYPE_SWITCH_ON = 3;
	public final static int TYPE_SWITCH_OFF = 4;
	public final static int TYPE_STATUS_CHANGED = 5;
	public final static int TYPE_STATUS_ON = 6;
	public final static int TYPE_STATUS_OFF = 7;
	
	public enum Status { ON, OFF};
}
