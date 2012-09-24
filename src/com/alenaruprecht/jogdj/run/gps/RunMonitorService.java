package com.alenaruprecht.jogdj.run.gps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

// This service monitors continuously record parameters about the run.
public class RunMonitorService extends Service
{  
	// This is the signal sent whenever one of the run parameters is updated.
	static public String RUN_MONITOR_UPDATE = "RUN_MONITOR_UPDATE";
	
	// The current speed
	private float mSpeed = 0.0f;
	
	// Distance.
	private float mTotalDistance = 0.0f;
	
	// Wifi/GPS locations.
	private List<Location> mLocations = new ArrayList<Location>();
	private boolean mStarted = false;
	
	private List<Location> mTempLocations = new  ArrayList<Location>();
	
	
	// Run monitor binder.
	private IBinder mRunMonitorServiceBinder = new RunMonitorBinder();	
    public class RunMonitorBinder extends Binder 
    {
    	public RunMonitorService getService() 
    	{
            return RunMonitorService.this;
        }
    }  
	
	LocationService mLocationService;	
	private ServiceConnection mLocationServiceConnection = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			mLocationService = ((LocationService.LocationServiceBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) 
		{
			mLocationService = null;
		}
	};	
    
    @Override
    public void onCreate()
    {
    	 bindService(new Intent(RunMonitorService.this, LocationService.class), mLocationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) 
    {
        return 0;
    }

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mRunMonitorServiceBinder;
	}
	
    @Override
    public void onDestroy() 
    {
    	unregisterReceiver(mLocationUpdateReceiver);
    }
   
    private BroadcastReceiver mLocationUpdateReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Location newLocation = (Location)intent.getExtras().get("location");
			
			mTempLocations.add(newLocation);
			
			if(mTempLocations.size() == 10)
			{
				float averageSpeed = 0.0f;
				float elapsedTime = 0.0f;
				
				for(int i=1 ; i< mTempLocations.size(); i++)			
				{
					Location previousLocation = mTempLocations.get(i-1);
					Location currentLocation = mTempLocations.get(i);
					
					// Calculate distance in km.
					float distance = currentLocation.distanceTo(previousLocation) / 1000.0f;
					
					// Calculate elapsed time in hours.
					elapsedTime = (float)((currentLocation.getTime() - previousLocation.getTime()) * (2.77777778  * 1E-7));
					
					//Calculate speed in km/h.
					averageSpeed += distance / elapsedTime;
				}
				
				mSpeed = averageSpeed /  mTempLocations.size();
				
				mTotalDistance += mSpeed * elapsedTime;
				
				mTempLocations.remove(0);
				getLocations().add(newLocation);
				
				Intent newIntent = new Intent(RUN_MONITOR_UPDATE);
				newIntent.putExtra("location", newLocation);
				newIntent.putExtra("totalDistance", getTotalDistance());
				newIntent.putExtra("speed", getSpeed());
			    context.sendBroadcast(newIntent);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Calculating speed...", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
    public void startRun()
    {
    	// Reset parameters
    	mTotalDistance = 0.0f;
    	mSpeed = 0.0f;
    	mLocations.clear();
    	
    	// Check if location services are enabled.
    	if(mLocationService.isGPSLocationEnabled() == true || 
    	   mLocationService.isNetworkLocationEnabled() == true)
    	{
    		mLocationService.startListening();
    		registerReceiver(mLocationUpdateReceiver, new IntentFilter(LocationService.LOCATION_UPDATED));
	    	mStarted = true;
    	}
    	else
    	{
    		Toast.makeText(getApplicationContext(), "GPS disabled", Toast.LENGTH_LONG).show();
    	}
    }
    
    public void stopRun()
    {
    	if(mStarted == true)
    	{
    		unregisterReceiver(mLocationUpdateReceiver);
    		mStarted = false;
    	}
    }

    public float getSpeed()
	{
		return mSpeed;
	}

	public float getTotalDistance() 
	{
		return mTotalDistance;
	}

	public List<Location> getLocations() 
	{
		return mLocations;
	}
	
	public boolean isStarted() 
	{
		return mStarted;
	}
}