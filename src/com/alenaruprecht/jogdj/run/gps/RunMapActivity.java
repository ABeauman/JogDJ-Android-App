package com.alenaruprecht.jogdj.run.gps;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import com.alenaruprecht.jogdj.Preferences;
import com.alenaruprecht.jogdj.R;
import com.alenaruprecht.jogdj.database.JogDJDataSource;
import com.alenaruprecht.jogdj.music.MusicChooser;
import com.alenaruprecht.jogdj.music.MusicFinderService;
import com.alenaruprecht.jogdj.music.MusicPlayerService;
import com.alenaruprecht.jogdj.music.Record;
import com.alenaruprecht.jogdj.plan.RunPlanning;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class RunMapActivity extends MapActivity  implements OnInitListener
{
	// Google map.
	private List<Overlay> mMapOverlays = null;
	private Projection mProjection;  
	private MapView mMapView;
	private MapController mMapController;
	
	// Views.
	private Chronometer mChronometerView;
	private TextView distanceTextView;
	private TextView speedTextView;
	private TextView songTextView;
	
	private Button pauseButton;
	private Button nextSongButton;
	private Button previousSongButton;
	
	// Services.
	private RunMonitorService runMonitorService;
	private MusicPlayerService mMusicPlayerService;
	
	private int timeElaspedSinceLastAudioCue = 0;
	private float distanceAtLastAudioCue = 0.0f;
	
	// Run planning
	List<RunPlanning> runPlannings;
	private TextToSpeech mTextToSpeech;
	int index = 0;
	
	private Stack<Record> mRecordsHistory = new Stack<Record>();
	
	private float speed = 0.0f;
	private float distance = 0.0f;
	
	private Location previousLocation;
	
	private MusicChooser musicChooser;
	private RunPlanning currentRunPlanning = null;
	
	private ServiceConnection mRunMonitorServiceConnection = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			runMonitorService = ((RunMonitorService.RunMonitorBinder) service).getService();
			runMonitorService.startRun();
		}

		public void onServiceDisconnected(ComponentName className) 
		{
			runMonitorService = null;
		}
	};	
	
	private ServiceConnection mMusicPlayerConnection = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			mMusicPlayerService = ((MusicPlayerService.MusicPlayerBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) 
		{
			mMusicPlayerService = null;
		}
	};
	
	// This receiver is used to update the song list when a new song is found on the device.
	private BroadcastReceiver mSongAddedReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Update song list.
			List<Record> records = JogDJDataSource.getInstance().getRecordTable().getAllSongs();
			musicChooser.setRecords(records);
			
			Log.d("RunMapActivity", "New songs added. " + records.size() + " songs in total.");
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);
	    
	    mMapView = (MapView) findViewById(R.id.mapview);
	    mMapView.setBuiltInZoomControls(true);
	    
	    mMapOverlays = mMapView.getOverlays();        
	    mProjection = mMapView.getProjection();
	    mMapController = mMapView.getController();
	    mMapController.setZoom(16);
	    
	    mChronometerView = (Chronometer)findViewById(R.id.timer_text_view);
	    distanceTextView = (TextView)findViewById(R.id.distance_text_view);
	    speedTextView = (TextView)findViewById(R.id.speed_text_view);
	    songTextView = (TextView)findViewById(R.id.song_text_view);
	    
	    pauseButton = (Button)findViewById(R.id.pause_play);
	    nextSongButton = (Button)findViewById(R.id.forward);
	    previousSongButton = (Button)findViewById(R.id.previous);
	    
	    setButtonListeners();
	    
	    bindServices();
	    runPlannings = JogDJDataSource.getInstance().getRunPlanningTable().getRunPlannings();
        
        //check for TTS data
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, 0);
	    	    
	    musicChooser = new MusicChooser();
		// Update song list.
		List<Record> records = JogDJDataSource.getInstance().getRecordTable().getAllSongs();
		musicChooser.setRecords(records);		
	    
	    mChronometerView.start();
	    
	    setupAudioCues();
	}

	private void setupAudioCues() 
	{
		new Timer().schedule(new TimerTask() 
		{
			@Override
			public void run() 
			{
				int timeTrigger = Preferences.getTimeTrigger(getApplicationContext()) * 60;
				
				if(timeTrigger != 0)
				{
					timeElaspedSinceLastAudioCue += 1;
					if(timeElaspedSinceLastAudioCue > timeTrigger)
					{						
						mTextToSpeech.speak(getTriggerSpeech(), TextToSpeech.QUEUE_FLUSH, null);
						timeElaspedSinceLastAudioCue = 0;
					}
				}
				
				float distanceTrigger = Preferences.getDistanceTrigger(getApplicationContext());
				
				if(distanceTrigger != 0)
				{
					float diff = distance - distanceAtLastAudioCue;
					if(diff > distanceTrigger)
					{					
						mTextToSpeech.speak(getTriggerSpeech(), TextToSpeech.QUEUE_FLUSH, null);
						distanceAtLastAudioCue = distance;
					}
				}
			}

		}, 0, 1000);
	}
		
    //act on result of TTS data check
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	    if (requestCode == 0) 
	    {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
	        {
	            //the user has the necessary data - create the TTS
	        	mTextToSpeech = new TextToSpeech(this, this);

	        }
	        else {
	                //no data - install it now
	            Intent installTTSIntent = new Intent();
	            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installTTSIntent);
	        }
	    }
	}
	
	private void startNextRunningPlanPhase()
	{
		boolean startRunPlanning = false;
		for(RunPlanning runPlanning:runPlannings)
		{
			if(runPlanning.mDistance != 0.0f || runPlanning.mTime != 0)
			{
				startRunPlanning = true;
				break;
			}
		}
		
		if(startRunPlanning == false)
			return;
		
		new Timer().schedule(new TimerTask() 
		{
			private float startDistance = 0.0f;
			private long startTime = 0;
			private RunPlanning currentRunPlanning = null;
			private int index = 0;
					
			@Override
			public void run() 
			{
				if(currentRunPlanning == null)
				{
					if(index < runPlannings.size())
					{
						currentRunPlanning =  runPlannings.get(index);						
						
						if(currentRunPlanning.mDistance != 0)
						{
							String unit = "kilometers";
							if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
								unit = "miles";
							
							mTextToSpeech.speak(currentRunPlanning.mMessage + " for " + String.valueOf(currentRunPlanning.mDistance) + unit , TextToSpeech.QUEUE_FLUSH, null);
						}
						else if(currentRunPlanning.mTime != 0)
						{
							mTextToSpeech.speak(currentRunPlanning.mMessage + " for " + String.valueOf(currentRunPlanning.mTime) + "minutes" , TextToSpeech.QUEUE_FLUSH, null);
						}							
						
						startDistance = distance;
						startTime = scheduledExecutionTime();
						index++;
					}
					else
					{
						mTextToSpeech.speak("run finished", TextToSpeech.QUEUE_FLUSH, null);
						cancel();
					}
				}
				
				if(currentRunPlanning != null)
				{
					if(currentRunPlanning.mDistance != 0)
					{
						if(distance - startDistance > currentRunPlanning.mDistance)
						{
							currentRunPlanning = null;
						}
					}
					else if(currentRunPlanning.mTime != 0)
					{
						if(scheduledExecutionTime() - startTime > currentRunPlanning.mTime * 60 * 1000)
						{						
							currentRunPlanning = null;
						}
					}
					else
					{
						currentRunPlanning = null;
					}
				}
			}

		}, 0, 1000);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	private void setButtonListeners() 
	{
		pauseButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	if(mMusicPlayerService != null)
            	{
            		if(mMusicPlayerService.isPlaying())
            			mMusicPlayerService.pause();
            		else
            			mMusicPlayerService.start();
            	}
            }  
        });
	    
	    nextSongButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	Record record = musicChooser.getSong(speed, true);
            	playSong(record);
            	
            	/*
            	speed += 0.1;
            	displaySpeed(speed);
            	playSong(musicChooser.getSong(speed, false));
            	*/
            }  
        });
	    
	    previousSongButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	/*
            	speed -= 0.1;
            	displaySpeed(speed);         	
            	playSong(musicChooser.getSong(speed, false));            	
            	*/
            	
            	if(mRecordsHistory.isEmpty() == false)
            	{
            		Record song = mRecordsHistory.pop();
            		playSong(song);
            		
            		if(mRecordsHistory.isEmpty() == false)
            		{
	            		if(mRecordsHistory.peek() == song)
	            			mRecordsHistory.pop();
            		}
            	}
            }  
        });
	}

	private void bindServices() 
	{
		bindService(new Intent(RunMapActivity.this, RunMonitorService.class), mRunMonitorServiceConnection, Context.BIND_AUTO_CREATE);
	    bindService(new Intent(RunMapActivity.this, MusicPlayerService.class), mMusicPlayerConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void unbindServices() 
	{
		unbindService(mRunMonitorServiceConnection);
		unbindService(mMusicPlayerConnection);
	}
	
	private void registerReceiver() 
	{		
		registerReceiver(locationUpdateReceiver, new IntentFilter(RunMonitorService.RUN_MONITOR_UPDATE));
		registerReceiver(songPlayingReceiver, new IntentFilter(MusicPlayerService.MUSIC_PLAYING));
		registerReceiver(songFinishedReciever, new IntentFilter(MusicPlayerService.MUSIC_FINISHED));
		registerReceiver(mSongAddedReceiver, new IntentFilter(MusicFinderService.SONGS_ADDED));
	}
	
	private void unregisterReceiver() 
	{
		unregisterReceiver(locationUpdateReceiver);
		unregisterReceiver(songPlayingReceiver);
		unregisterReceiver(songFinishedReciever);
		unregisterReceiver(mSongAddedReceiver);
	}
	
	private void playSong(Record record )
	{
		if(record != null)
		{
			if(mMusicPlayerService.isPause() == false && (mRecordsHistory.isEmpty() ||  record != mRecordsHistory.peek()))
			{
				mMusicPlayerService.play(record);
				mRecordsHistory.push(record);
			}
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() 
	{
	    return false;
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();		
		registerReceiver();
	}

	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to exit Jog DJ?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
		       {
		           public void onClick(DialogInterface dialog, int id) 
		           {
		        	   test();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() 
		       {
		           public void onClick(DialogInterface dialog, int id) 
		           {
		                dialog.cancel();
		           }
		       });
		
		AlertDialog alert = builder.create();
		
		alert.show();
	}
	
	private void test()
	{
		mTextToSpeech.shutdown();
		
		runMonitorService.stopRun();
		
		mMusicPlayerService.reset();
		
		unregisterReceiver();
		
		unbindServices();
		
		finish();
	}

	private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Location location = (Location)intent.getExtras().get("location");
			
			int latitude = (int)(location.getLatitude() * 1E6);
	    	int longitude = (int)(location.getLongitude() * 1E6);
			mMapController.setCenter(new GeoPoint(latitude, longitude));

			distance = getDistance(intent.getExtras().getFloat("totalDistance"));
			speed = getSpeed(intent.getExtras().getFloat("speed"));
			
			displaySpeed(speed);
			displayDistance(distance);
			
			
			mMapOverlays.add(new RunMapOverlay(location, previousLocation, mProjection)); 

			previousLocation = location;
			mMapView.postInvalidate();
			
			Record record = musicChooser.getSong(speed, false);
			playSong(record);
		}
	};
	
	private BroadcastReceiver songPlayingReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String title = intent.getExtras().getString("title");	
			double tempo = intent.getExtras().getDouble("tempo");	
			songTextView.setText(title + " " + String.valueOf(tempo));
		}
	};
	
	private BroadcastReceiver songFinishedReciever = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Play next song.
			playSong(musicChooser.getSong(speed, true));
		}
	};
	
	@Override
	public void onInit(int status) 
	{
	    if (status == TextToSpeech.SUCCESS)
	    {
	    	if(mTextToSpeech.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE)
			{
	    		mTextToSpeech.setLanguage(Locale.ENGLISH);
	        	startNextRunningPlanPhase();
			}
	    }
	}
	
	private float getDistance(float distance)
	{
		if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
		{
			return distance * 0.621371192f;
		}
		
		return distance;
	}
	
	private float getSpeed(float speed)
	{
		if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
		{
			return speed * 0.621371192f;
		}
		
		return speed;
	}
	
	private void displayDistance(float distance)
	{
		if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
		{
			distanceTextView.setText(" " + new DecimalFormat("#.##").format(distance) + "m");
		}
		else
		{
			distanceTextView.setText(" " + new DecimalFormat("#.##").format(distance) + "km");
		}
	}
	
	private void displaySpeed(float speed)
	{

		if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
		{
			speedTextView.setText(" " + new DecimalFormat("#.##").format(speed) + "m/h");
		}
		else
		{
			speedTextView.setText(" " + new DecimalFormat("#.##").format(speed) + "km/h");
		}
	}
	
	private String getTriggerSpeech()
	{
		String speedSpeech = "";
		String distanceSpeech = "";
		
		if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
		{
			speedSpeech = "Pace " + new DecimalFormat("#.##").format(getSpeed(speed)) + " miles per hour.";
		}
		else
		{
			speedSpeech = "Pace " + new DecimalFormat("#.##").format(getSpeed(speed)) + " kilometers per hour.";
		}
		
		if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
		{
			distanceSpeech = "Distance " + new DecimalFormat("#.##").format(getSpeed(speed)) + " miles.";
		}
		else
		{
			distanceSpeech = "Distance " + new DecimalFormat("#.##").format(getSpeed(speed)) + " kilometers.";
		}
		
		return speedSpeech + distanceSpeech;
	}
}