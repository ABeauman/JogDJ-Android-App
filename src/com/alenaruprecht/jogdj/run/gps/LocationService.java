package com.alenaruprecht.jogdj.run.gps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

// This service provides location information based on GPS or 3G/Wifi antennas. The service broadcasts a message whenever a new location is recieved.
public class LocationService extends Service 
{
	public static String LOCATION_UPDATED = "LOCATION_UPDATED";
	private LocationManager mLocationManager;
	
	private IBinder mLocationServiceBinder = new LocationServiceBinder();
    public class LocationServiceBinder extends Binder 
    {
    	public LocationService getService() 
    	{
            return LocationService.this;
        }
    }
    
	@Override
	public void onCreate() 
	{
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	}

	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return mLocationServiceBinder;
	}
    
    // Define a listener that responds to location updates
    private LocationListener locationListener = new LocationListener() 
	{
		public void onLocationChanged(Location location)
		{
		   Intent intent = new Intent(LOCATION_UPDATED);
		   intent.putExtra("location", location);
		   sendBroadcast(intent);
		}

		public void onProviderEnabled(String provider){}
		public void onProviderDisabled(String provider){}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras){}
	};
    
    
    
    public void startListening()
    {
    	// Register the listener with the Location Manager to receive location updates
    	int minTime = 1000;
    	if(isGPSLocationEnabled())
    	{
    		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, locationListener);
    	}
    	else if(isNetworkLocationEnabled())
    	{
    		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, 0, locationListener);
    	}		
    }
    
    public void stopListening()
    {
		mLocationManager.removeUpdates(locationListener);
    }
    
    public Boolean isGPSLocationEnabled()
    {
    	return ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER) == true;
    }
    
    public Boolean isNetworkLocationEnabled()
    {
    	return ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true;
    }
}
