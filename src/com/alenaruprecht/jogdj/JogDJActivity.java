package com.alenaruprecht.jogdj;

import com.alenaruprecht.jogdj.R;
import com.alenaruprecht.jogdj.database.JogDJDataSource;
import com.alenaruprecht.jogdj.music.MusicFinderService;
import com.alenaruprecht.jogdj.music.MusicPlayerService;
import com.alenaruprecht.jogdj.music.SongExplorerActivity;
import com.alenaruprecht.jogdj.plan.RunPlanning;
import com.alenaruprecht.jogdj.plan.RunPlanningActivity;
import com.alenaruprecht.jogdj.run.gps.LocationService;
import com.alenaruprecht.jogdj.run.gps.RunMapActivity;
import com.alenaruprecht.jogdj.run.gps.RunMonitorService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

// This is the main activity.
public class JogDJActivity extends Activity
{
	// Reference to the music finder service.
	private MusicFinderService mMusicFinderService;
	
	// Vibrator to give haptic feedback when a button is clicked.
	private Vibrator mVibrator;
	
	private ServiceConnection mMusicFinderConnection = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			mMusicFinderService = ((MusicFinderService.MusicFinderBinder)service).getService();
			
			// Start finding music on device.
			mMusicFinderService.start();
		}

		public void onServiceDisconnected(ComponentName className) 
		{
			mMusicFinderService = null;
		}
	};
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		unbindService(mMusicFinderConnection);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.main);      
        
        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE); 
        
        JogDJDataSource.initialise(this);
        startServices(); 
        initialiseRunPlanning();      
        setButtonClickListeners(); 
    }

	private void setButtonClickListeners() 
	{
		// Start run when the start button is clicked.
		Button startButton = (Button)findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	mVibrator.vibrate(40);
            	startActivity(new Intent(getApplicationContext(), RunMapActivity.class));
            }  
        }); 
        
        // Show training plan activity when training plan button is clicked.
        Button trainingPlanButton = (Button)findViewById(R.id.training_plan);
        trainingPlanButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	mVibrator.vibrate(40);
            	startActivity(new Intent(getApplicationContext(), RunPlanningActivity.class));
            }  
        }); 
       
        // Show song explorer activity when song explorer button is clicked.
        Button songExplorerButton = (Button)findViewById(R.id.song_explorer);
        songExplorerButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	mVibrator.vibrate(40);
            	startActivity(new Intent(getApplicationContext(), SongExplorerActivity.class));
            }  
        }); 
        
        // Show settings activity when settings button is clicked.
        Button settingsButton = (Button)findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	mVibrator.vibrate(40);
            	startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
            }  
        }); 
        
        // Show help activity when helper button is clicked.
        Button helpButton = (Button)findViewById(R.id.help);
        helpButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	mVibrator.vibrate(40);
            	startActivity(new Intent(getApplicationContext(), HelpActivity.class));
            }  
        });
	} 
 
	private void startServices() 
	{
		startService(new Intent(this, MusicFinderService.class));        
        startService(new Intent(this, LocationService.class));   
        startService(new Intent(this, MusicPlayerService.class));
        startService(new Intent(this, RunMonitorService.class));
        
        bindService(new Intent(JogDJActivity.this, MusicFinderService.class), mMusicFinderConnection, Context.BIND_AUTO_CREATE);
	}

	private void initialiseRunPlanning()
	{
		if(JogDJDataSource.getInstance().getRunPlanningTable().planningExist("warm up") == false)
        {
			JogDJDataSource.getInstance().getRunPlanningTable().addRunPlanning(new RunPlanning("warm up", 0, 0, "start warm up"));
        }
        
        if(JogDJDataSource.getInstance().getRunPlanningTable().planningExist("run") == false)
        {
        	JogDJDataSource.getInstance().getRunPlanningTable().addRunPlanning(new RunPlanning("run", 0, 0, "start main run"));
        }
        
        if(JogDJDataSource.getInstance().getRunPlanningTable().planningExist("cool down") == false)
        {
        	JogDJDataSource.getInstance().getRunPlanningTable().addRunPlanning(new RunPlanning("cool down", 0, 0, "start cool down"));
        }
	}
}