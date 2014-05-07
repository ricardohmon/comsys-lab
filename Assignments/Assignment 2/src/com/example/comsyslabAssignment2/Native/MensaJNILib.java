package com.example.comsyslabAssignment2.Native;

/*
 * 		Native code's wrapper
 */
public class MensaJNILib {

    static {
        System.loadLibrary("comsyslab_assignment2");
    }
    

    public static native String  handleRequest(String hostname, String request);
}
