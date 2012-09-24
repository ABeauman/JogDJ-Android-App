package com.alenaruprecht.jogdj.run.gps;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

// This class is used to display information on the map. At the moment it is only
// used to show the user's route on the map.
class RunMapOverlay extends Overlay
{
	private int mCurrentLatitude;
	private int mCurrentLongitude;
	
	private int mPreviousLatitude;
	private int mPreviousLongitude;
	
	private Projection mProjection;  
	
    public RunMapOverlay(Location currentLocation, Location previousLocation, Projection projection)
    {
    	// Convert latitude and longitude to degrees.
    	this.mCurrentLatitude = (int)(currentLocation.getLatitude() * 1E6);
    	this.mCurrentLongitude = (int)(currentLocation.getLongitude() * 1E6);
    	
    	if(previousLocation != null) 
    	{
    		this.mPreviousLatitude = (int)(previousLocation.getLatitude() * 1E6);
    		this.mPreviousLongitude = (int)(previousLocation.getLongitude() * 1E6);
    	}
    	else
    	{
    		this.mPreviousLatitude = this.mCurrentLatitude;
    		this.mPreviousLongitude = this.mCurrentLongitude;
    	}
    	
    	this.mProjection = projection;
    }   

    @Override
    public void draw(Canvas canvas, MapView mapv, boolean shadow)
    {
    	// Draw path between previous and current location.
        super.draw(canvas, mapv, shadow);

        Paint mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);

        GeoPoint gP1 = new GeoPoint(mPreviousLatitude, mPreviousLongitude);
        GeoPoint gP2 = new GeoPoint(mCurrentLatitude, mCurrentLongitude);

        Point p1 = new Point();
        Point p2 = new Point();
        Path path = new Path();

        mProjection.toPixels(gP1, p1);
        mProjection.toPixels(gP2, p2);

        path.moveTo(p2.x, p2.y);
        path.lineTo(p1.x,p1.y);

        canvas.drawPath(path, mPaint);
    }
}
