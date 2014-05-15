package com.example.comsyslab_assignment3.model;


public final class Accelerometer extends Sensor {
	private static int _type = android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
	
	@Override
	public int getType()
	{
		return _type;
	}
}
