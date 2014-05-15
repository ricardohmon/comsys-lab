package com.example.comsyslab_assignment3.hw;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class SensorController<T extends com.example.comsyslab_assignment3.model.Sensor> implements SensorEventListener {

	private static final int UPDATE_THRESHOLD = 500;
	private T t;
	private SensorManager sensorManager;
	private Sensor sensor;
	private long lastUpdate;
	private float[] values;
	private double mCurrAcc;
	private Context mContext;
	private OnNewDataListener mNewDataListener;
	
	public SensorController(T sensorType,Context context)
	{
		this.t = sensorType;
		mContext = context;
		// Get a reference to the Sensor service and request the desired sensor.
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if(null == (sensor = sensorManager.getDefaultSensor(t.getType())))
			((Activity)context).finish();
		mCurrAcc = 0;
	}
	
	public void registerSensor()
	{
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
		lastUpdate = System.currentTimeMillis();
	}
	
	public void unregisterSensor()
	{
		sensorManager.unregisterListener(this);
	}
	
	public void setOnNewDataListener(OnNewDataListener listener) {
        this.mNewDataListener = listener;
    }

    public interface OnNewDataListener {
        public void onNewData(float[] values);
    }
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// N/A
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float xAxis;
		float yAxis;
		float zAxis;
		
		if(event.sensor.getType() == t.getType() ) {
			long actualTime = System.currentTimeMillis();
			
			if(actualTime - lastUpdate > UPDATE_THRESHOLD) {
				lastUpdate = actualTime;
				values = event.values;
				
				xAxis = values[0];
				yAxis = values[1];
				zAxis = values[2];
				mNewDataListener.onNewData(values);
				// Obtain the absolute value of three dimensions and check if it passes the threshold
				mCurrAcc = Math.sqrt(xAxis*xAxis + yAxis*yAxis + zAxis*zAxis);
				if(mCurrAcc > 2.7f)
				{	// Display a message
					Toast.makeText(mContext, "Shake detected", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
	}
	
}
