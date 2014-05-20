package com.example.comsyslabAssignment3.hw;

import com.example.comsyslabAssignment3.constants.Constants;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class SensorController<T extends com.example.comsyslabAssignment3.model.Sensor> implements SensorEventListener {

	private static final int UPDATE_THRESHOLD = 500;
	private T t;
	private SensorManager sensorManager;
	private Sensor sensor;
	private long lastUpdate;
	private float[] linear_acceleration;
	private float[] mGravity;
	private double mCurrAcc;
	private Context mContext;
	private OnNewDataListener mNewDataListener;
	private OnShakeDetectedListener mShakeDetectedListener;
	
	/*
	 * 		Interfaces
	 */
    public interface OnNewDataListener {
        public void onNewData(float[] values);
    }
    
    public interface OnShakeDetectedListener {
    	public void onShakeDetected();
    }
	
	/*
	 * 		Listener setters
	 */
	public void setOnNewDataListener(OnNewDataListener listener) {
        this.mNewDataListener = listener;
    }
	
	public void setOnShakeDetectedListener(OnShakeDetectedListener listener) {
		this.mShakeDetectedListener = listener;
	}
    
    
	public SensorController(T sensorType,Context context)
	{
		this.t = sensorType;
		mContext = context;
		// Get a reference to the Sensor service and request the desired sensor.
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if(null == (sensor = sensorManager.getDefaultSensor(t.getType()))) {
			Log.e(Constants.SensorControllerTag, "Sensor not found");
			((Activity)context).finish();
		}
		mCurrAcc = 0;
		mGravity = new float[] {0,0,0};
		linear_acceleration = new float[] {0,0,0};
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
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// N/A
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final float alpha = 0.8f;
		float xAxis;
		float yAxis;
		float zAxis;
		
		if(event.sensor.getType() == t.getType() ) {
			long actualTime = System.currentTimeMillis();
			
			if(actualTime - lastUpdate > UPDATE_THRESHOLD) {
				lastUpdate = actualTime;
				
				mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
				mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
				mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
				
				linear_acceleration[0] = event.values[0] - mGravity[0];
		        linear_acceleration[1] = event.values[1] - mGravity[1];
		        linear_acceleration[2] = event.values[2] - mGravity[2];
				
				xAxis = linear_acceleration[0];
				yAxis = linear_acceleration[1];
				zAxis = linear_acceleration[2];
				mNewDataListener.onNewData(linear_acceleration);
				// Obtain the absolute value of three dimensions and check if it passes the threshold
				mCurrAcc = Math.sqrt(xAxis*xAxis + yAxis*yAxis + zAxis*zAxis);
				if(mCurrAcc > 2.7f)
				{	// Display a message
					Toast.makeText(mContext, "Shake detected", Toast.LENGTH_SHORT).show();
					this.mShakeDetectedListener.onShakeDetected();
				}
				
			}
		}
	}
	
}
