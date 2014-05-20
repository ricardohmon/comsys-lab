package com.example.comsyslabAssignment3.model;


public final class Accelerometer extends Sensor {
	private static int _type = android.hardware.Sensor.TYPE_ACCELEROMETER;
	
	@Override
	public int getType()
	{
		return _type;
	}
}
