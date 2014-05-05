package com.example.comsyslabAssignment2.Native;

public class MensaJNILib {

    static {
        System.loadLibrary("comsyslab_assignment2");
    }
    

    public static native String  handleRequest(String hostname, String request);
}
