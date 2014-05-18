package com.example.comsyslab_assignment3;

import com.example.comsyslab_assignment3.constants.Constants;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GraphView extends SurfaceView implements SurfaceHolder.Callback {

	private final float SENSOR_MAX_VALUE = 9.81f; // Max value in the graph
	private final float SAMPLE_NUM = 50; // Number of samples to be shown in the graph before reseting it.
	private final int colors[] = {Color.GREEN, Color.MAGENTA, Color.RED}; 
	private final Context mContext; // Activity's context
	private final DisplayMetrics  mDisplay;
	private final SurfaceHolder mSurfaceHolder;
	private final Paint mPainter = new Paint();
	private Bitmap mBackgroundBMP;
	private Thread mDrawingThread;
	private int sampleCount;
	private Path paths[] = new Path[3];
	private float mYOrigin;
	private float mXspacing,mYspacing;
	
	
	
	public GraphView(Context context) {
		super(context);
		
		mContext = context;
		mDisplay = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(mDisplay);
		mBackgroundBMP = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.grid);
		// Assign styling parameters to the Paint object
		mPainter.setAntiAlias(true);
		mPainter.setStyle(Paint.Style.STROKE);
		mPainter.setStrokeWidth(8);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this); // Let the surface holder know this class will draw on the surface
		sampleCount = 0;
	}
	/*
	 * 		Add new set of values incoming from the sensor to the corresponding path. If the number of samples was reached, reset the graph.
	 */
	public void addNewValues(float[] values)
	{
		float xPixel, yPixel = 0f;
		Log.i(Constants.DrawViewTag, "Received new values from the sensor");
		if(sampleCount == SAMPLE_NUM) {
			sampleCount = 0;
			for(Path path:paths)
			{
				path.rewind();
			}
		}
		else
		{
			xPixel = mXspacing*sampleCount;
			for(int i = 0; i < paths.length; i++ )
			{
				yPixel = -values[i]*mYspacing + mYOrigin;
				paths[i].lineTo(xPixel, yPixel);
			}
			sampleCount++;
		}
			
	}
	// First erase the canvas with a white background and then draw the paths.
	private void drawGraph(Canvas canvas) {
		
		canvas.drawColor(mContext.getResources().getColor(R.color.graph_background_color));
		canvas.drawBitmap(mBackgroundBMP, 0, 0, mPainter);
		for(int i=0; i< paths.length;i++)
		{
			mPainter.setColor(colors[i]);
			canvas.drawPath(paths[i], mPainter);
		}
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * Method which notifies when the surface is ready to draw on it.
	 * Let a thread handle the operation. 
	 */
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		mYOrigin = this.getHeight() / 2f;
		mXspacing = this.getWidth() / SAMPLE_NUM;
		mYspacing = mYOrigin / SENSOR_MAX_VALUE;
		mBackgroundBMP = Bitmap.createScaledBitmap(mBackgroundBMP, this.getWidth(), this.getHeight(), true);

		// Initialize all paths that will contain the values received from the sensor.
		for(int i=0; i<paths.length;i++)
		{
			paths[i] = new Path();
			paths[i].moveTo(0f, mYOrigin);
		}
		
		mDrawingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Canvas canvas = null;
				while(!Thread.currentThread().isInterrupted())
				{
					canvas = mSurfaceHolder.lockCanvas();
					if(null != canvas) {
						drawGraph(canvas);
						mSurfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		});
		mDrawingThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (null != mDrawingThread)
			mDrawingThread.interrupt();
		
	}

}
