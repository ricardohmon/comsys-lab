package com.example.comsyslabAssignment3.Native;

/*
 * 		Native code's wrapper
 */
public class ClientJNILib {
	
	static {
		System.loadLibrary("comsyslab_assignment3");
	}
	
	public static native void register(String hostname, String request);
	public static native void sendEvent(String hostname);
	public static native void unregister(String hostname);
	public static native void keepAlive(String hostname);
}
