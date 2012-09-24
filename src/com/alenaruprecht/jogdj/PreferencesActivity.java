package com.alenaruprecht.jogdj;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

// This activity lets the user sets preferences.
public class PreferencesActivity extends Activity 
{
	// Callback function for trigger dialog.
	private interface OnTriggerDialogListener
	{
		public void setValue(int id);
	}
	
	// Dialog ids.
	private final int TIME_TRIGGER_DIALOG = 1000;
	private final int DISTANCE_KM_TRIGGER_DIALOG = 1001;
	private final int DISTANCE_MILES_TRIGGER_DIALOG = 1002;
	
	// Views
	private Button mTimeTriggerButton;
	private Button mDistanceTriggerButton;
	
	private RadioButton mKMRadioButton;
	private RadioButton mMilesRadioButton;
	
	// Trigger dialog instances.
	private OnTriggerDialogListener mTimeTriggerListener;
	private OnTriggerDialogListener mDistanceTriggerListener;
	
	// Vibrator to give haptic feedback when a button is clicked.
	private Vibrator mVibrator;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.preferences);
	    
	    mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE); 
	    
	    // Retrieve view references.
	    mTimeTriggerButton = (Button)findViewById(R.id.timetrigger);
	    mDistanceTriggerButton = (Button)findViewById(R.id.distancetrigger);
	    
	    mKMRadioButton = (RadioButton)findViewById(R.id.km_radio);
	    mMilesRadioButton = (RadioButton)findViewById(R.id.m_radio);
	    
	    // Toggle distance unit radio buttons.
	    if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
	    	mMilesRadioButton.toggle();
    	else
    		mKMRadioButton.toggle();
	    
	    setButtonClickListeners();	    
	    setRadioButtonListeners(); 
	    setTriggerListeners();
	}

	private void setRadioButtonListeners() 
	{
		// Set preference when km radio button is pressed.
		mKMRadioButton.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() 
	    {
	        @Override
	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	        {
	        	if(isChecked)
	        	{
	        		mVibrator.vibrate(40);
	        		Preferences.setDistanceUnit(getApplicationContext(), Preferences.DistanceUnit.Km);
	        	}
	        }
	    });
	    
		// Set preference when miles radio button is pressed.
	    mMilesRadioButton.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() 
	    {
	        @Override
	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	        {
	        	if(isChecked)
	        	{
	        		mVibrator.vibrate(40);
	        		Preferences.setDistanceUnit(getApplicationContext(), Preferences.DistanceUnit.Miles);
	        	}
	        }
	    });
	}

	private void setTriggerListeners() 
	{
		mTimeTriggerListener = new OnTriggerDialogListener() 
		{
			public void setValue(int id) 
			{
				idToTimeTriggerPreference(id);
			}
		};
		
		mDistanceTriggerListener = new OnTriggerDialogListener() 
		{
			public void setValue(int id) 
			{
				idToDistanceTriggerPreference(id);
			}
		};
	}

	private void setButtonClickListeners() 
	{
		// Show time trigger dialog when time trigger button is clicked.
		mTimeTriggerButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	mVibrator.vibrate(40);
            	showDialog(TIME_TRIGGER_DIALOG);
            }  
        }); 
	    
		// Show distance trigger dialog when distance trigger button is clicked.
	    mDistanceTriggerButton.setOnClickListener(new View.OnClickListener()
        {   
            @Override  
            public void onClick(View view) 
            {  
            	mVibrator.vibrate(40);
            	
            	if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
            		showDialog(DISTANCE_MILES_TRIGGER_DIALOG);
            	else
            		showDialog(DISTANCE_KM_TRIGGER_DIALOG);
            }  
        });
	}
	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
		switch (id) 
		{
			case TIME_TRIGGER_DIALOG:
				return getDialog("Select time", mTimeTriggerListener, R.layout.time_trigger, timeTriggerPreferenceToId());
				
			case DISTANCE_KM_TRIGGER_DIALOG:
				return getDialog("Select distance", mDistanceTriggerListener, R.layout.km_trigger, distanceTriggerToId());
				
			case DISTANCE_MILES_TRIGGER_DIALOG:
				return getDialog("Select distance", mDistanceTriggerListener, R.layout.mile_trigger, distanceTriggerToId());

		}
		return null;
	}
	
	// Create a dialog box.
	private Dialog getDialog(String title, final OnTriggerDialogListener listener, int layout, int selected)
	{
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(layout);
		dialog.setTitle(title);		
				
		// Select appropriate radio button.
		final RadioButton selectedRadioButton = (RadioButton)dialog.findViewById(selected);
		selectedRadioButton.toggle();
		
		Button okButton = (Button)dialog.findViewById(R.id.ok_button);		
		okButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// Find radio button selected and pass it to the listener.
				RadioGroup radioGroup = (RadioGroup)dialog.findViewById(R.id.trigger_radio_group);
				int id = radioGroup.getCheckedRadioButtonId();
				
				listener.setValue(id);
				
				dialog.dismiss();
			}
		});
		
		Button cancelButton = (Button)dialog.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				selectedRadioButton.toggle();
				dialog.dismiss();
			}
		});
		
		return dialog;
	}

	// Convert a radio button id into a distance trigger. This function is used to decide a distance trigger based on the radio button selected.
	private void idToDistanceTriggerPreference(int id) 
	{
		switch(id)
		{
			case R.id.km0 : Preferences.setDistanceTrigger(getApplicationContext(), 0.0f); break;
			case R.id.km1 : Preferences.setDistanceTrigger(getApplicationContext(), 0.25f); break;
			case R.id.km2 : Preferences.setDistanceTrigger(getApplicationContext(), 0.5f); break;
			case R.id.km3 : Preferences.setDistanceTrigger(getApplicationContext(), 0.75f); break;
			case R.id.km4 : Preferences.setDistanceTrigger(getApplicationContext(), 1.0f); break;
			
			case R.id.mile0 : Preferences.setDistanceTrigger(getApplicationContext(), 0.0f); break;
			case R.id.mile1 : Preferences.setDistanceTrigger(getApplicationContext(), 0.25f); break;
			case R.id.mile2 : Preferences.setDistanceTrigger(getApplicationContext(), 0.5f); break;
			case R.id.mile3 : Preferences.setDistanceTrigger(getApplicationContext(), 0.75f); break;
			case R.id.mile4 : Preferences.setDistanceTrigger(getApplicationContext(), 1.0f); break;
			
			default: Preferences.setDistanceTrigger(getApplicationContext(), 0.0f);
		}
	}

	// Convert a radio button id into a time trigger. This function is used to decide a time trigger based on the radio button selected.
	private void idToTimeTriggerPreference(int id) 
	{
		switch(id)
		{
			case R.id.time0 : Preferences.setTimeTrigger(getApplicationContext(), 0); break;
			case R.id.time1 : Preferences.setTimeTrigger(getApplicationContext(), 1); break;
			case R.id.time2 : Preferences.setTimeTrigger(getApplicationContext(), 2); break;
			case R.id.time3 : Preferences.setTimeTrigger(getApplicationContext(), 3); break;
			case R.id.time4 : Preferences.setTimeTrigger(getApplicationContext(), 4); break;
			case R.id.time5 : Preferences.setTimeTrigger(getApplicationContext(), 5); break;
			default: break;
		}
	}
	
	// Convert a time into a view id. This function is used to decide which radio button should be selected based on a time trigger.
	private int timeTriggerPreferenceToId() 
	{
		switch(Preferences.getTimeTrigger(getApplicationContext()))
		{
			case 0 : return R.id.time0;
			case 1 : return R.id.time1;
			case 2 : return R.id.time2;
			case 3 : return R.id.time3;
			case 4 : return R.id.time4;
			case 5 : return R.id.time5;
			default: return R.id.time0;
		}
	}
	
	// Convert a distance into an android view id. This function is used to decide which radio button should be selected based on a distance trigger.
	private int distanceTriggerToId() 
	{
		float distanceTrigger = Preferences.getDistanceTrigger(getApplicationContext());
		Preferences.DistanceUnit unit = Preferences.getDistanceUnit(getApplicationContext());
		
		if(distanceTrigger == 0.0f)
		{
			return unit == Preferences.DistanceUnit.Km ? R.id.km0 : R.id.mile0;
		}
		else if(distanceTrigger == 0.25f)
		{
			return unit == Preferences.DistanceUnit.Km ? R.id.km1 : R.id.mile1;
		}
		else if(distanceTrigger == 0.5f)
		{
			return unit == Preferences.DistanceUnit.Km ? R.id.km2 : R.id.mile2;
		}
		else if(distanceTrigger == 0.75f)
		{
			return unit == Preferences.DistanceUnit.Km ? R.id.km3 : R.id.mile3;
		}
		else if(distanceTrigger == 1.0f)
		{
			return unit == Preferences.DistanceUnit.Km ? R.id.km4 : R.id.mile4;
		}
		else
		{
			return unit == Preferences.DistanceUnit.Km ? R.id.km0 : R.id.mile0;
		}
	}
}
