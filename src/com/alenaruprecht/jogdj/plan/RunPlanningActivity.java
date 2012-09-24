package com.alenaruprecht.jogdj.plan;

import com.alenaruprecht.jogdj.Preferences;
import com.alenaruprecht.jogdj.R;
import com.alenaruprecht.jogdj.database.JogDJDataSource;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;


public class RunPlanningActivity extends Activity 
{
	private interface OnNumberSetListener
	{
		public void setValue(NumberPicker view, int value);
	}
	
	private Button warmUpTimeButton;
	private Button mainRunTimeButton;
	private Button coolDownTimeButton;
	private Button warmUpDistanceButton;
	private Button mainRunDistanceButton;
	private Button coolDownDistanceButton;
	
	private TextView distanceTitle;
	private int maxDistance;
	private int maxTime;
	
	private final int WARM_UP_TIME_PICKER = 1000;
	private final int MAIN_RUN_TIME_PICKER = 1001;
	private final int COOL_DOWN_TIME_PICKER = 1002;
	
	private final int WARM_UP_DISTANCE_PICKER = 1003;
	private final int MAIN_RUN_DISTANCE_PICKER = 1004;
	private final int COOL_DOWN_DISTANCE_PICKER = 1005;
	
	private OnNumberSetListener warmUpTimeListener;
	private OnNumberSetListener mainRunTimeListener;
	private OnNumberSetListener coolDownTimeListener;
	
	private OnNumberSetListener warmUpDistanceListener;
	private OnNumberSetListener mainRunDistanceListener;
	private OnNumberSetListener coolDownDistanceListener;
	
	private RunPlanning warmUpRunPlanning;
	private RunPlanning mainRunPlanning;
	private RunPlanning coolDownPlanning;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.run_planning);
		
		warmUpRunPlanning = JogDJDataSource.getInstance().getRunPlanningTable().getRunPlanning("warm up");
		mainRunPlanning = JogDJDataSource.getInstance().getRunPlanningTable().getRunPlanning("run");
		coolDownPlanning = JogDJDataSource.getInstance().getRunPlanningTable().getRunPlanning("cool down");

		distanceTitle = (TextView) findViewById(R.id.distance_text_view);
		maxDistance = 200;
		maxTime = 24 * 60;
		
		warmUpTimeButton = (Button) findViewById(R.id.warmUpButtonTime);
		mainRunTimeButton = (Button) findViewById(R.id.mainRunButtonTime);
		coolDownTimeButton = (Button) findViewById(R.id.coolDownButtonTime);
		
		warmUpDistanceButton = (Button) findViewById(R.id.warmUpButtonDistance);
		mainRunDistanceButton = (Button) findViewById(R.id.mainRunButtonDistance);
		coolDownDistanceButton = (Button) findViewById(R.id.coolDownButtonDistance);
		
		warmUpTimeButton.setText(warmUpRunPlanning.mTime.toString());
		mainRunTimeButton.setText(mainRunPlanning.mTime.toString());
		coolDownTimeButton.setText(coolDownPlanning.mTime.toString());
		
		warmUpDistanceButton.setText(warmUpRunPlanning.mDistance.toString());
		mainRunDistanceButton.setText(mainRunPlanning.mDistance.toString());
		coolDownDistanceButton.setText(coolDownPlanning.mDistance.toString());
		
		setButtonListeners();		
		setNumberPickerDialogListeners();
		
		if(Preferences.getDistanceUnit(getApplicationContext()) == Preferences.DistanceUnit.Miles)
		{
			distanceTitle.setText("Distance (m)");
		}
		else
		{
			distanceTitle.setText("Distance (km)");
		}
	}


	private void setNumberPickerDialogListeners() 
	{
		warmUpTimeListener = new OnNumberSetListener() 
		{
			public void setValue(NumberPicker view, int value) 
			{
				warmUpTimeButton.setText(String.valueOf(value));
				warmUpRunPlanning.mTime = value;				
				
				warmUpDistanceButton.setText("0");
				warmUpRunPlanning.mDistance = 0;
				
				JogDJDataSource.getInstance().getRunPlanningTable().updateRunPlanning(warmUpRunPlanning);
			}
		};
		
		mainRunTimeListener = new OnNumberSetListener() 
		{
			public void setValue(NumberPicker view, int value) 
			{
				mainRunTimeButton.setText(String.valueOf(value));
				mainRunPlanning.mTime = value;
				
				mainRunDistanceButton.setText("0");
				mainRunPlanning.mDistance = 0;
				
				JogDJDataSource.getInstance().getRunPlanningTable().updateRunPlanning(mainRunPlanning);
			}
		};
		
		coolDownTimeListener = new OnNumberSetListener() 
		{
			public void setValue(NumberPicker view, int value) 
			{
				coolDownTimeButton.setText(String.valueOf(value));
				coolDownPlanning.mTime = value;
				
				coolDownDistanceButton.setText("0");
				coolDownPlanning.mDistance = 0;
				
				JogDJDataSource.getInstance().getRunPlanningTable().updateRunPlanning(coolDownPlanning);
			}
		};
		
		warmUpDistanceListener = new OnNumberSetListener() 
		{
			public void setValue(NumberPicker view, int value) 
			{
				warmUpDistanceButton.setText(String.valueOf(value));
				warmUpRunPlanning.mDistance = value;
				
				warmUpTimeButton.setText("0");
				warmUpRunPlanning.mTime = 0;			
				
				JogDJDataSource.getInstance().getRunPlanningTable().updateRunPlanning(warmUpRunPlanning);
			}
		};
		
		mainRunDistanceListener = new OnNumberSetListener() 
		{
			public void setValue(NumberPicker view, int value) 
			{
				mainRunDistanceButton.setText(String.valueOf(value));
				mainRunPlanning.mDistance = value;
				
				mainRunTimeButton.setText("0");
				mainRunPlanning.mTime = 0;
				
				JogDJDataSource.getInstance().getRunPlanningTable().updateRunPlanning(mainRunPlanning);
			}
		};
		
		coolDownDistanceListener = new OnNumberSetListener() 
		{
			public void setValue(NumberPicker view, int value) 
			{
				coolDownDistanceButton.setText(String.valueOf(value));
				coolDownPlanning.mDistance = value;
				
				coolDownTimeButton.setText("0");
				coolDownPlanning.mTime = 0;
				
				JogDJDataSource.getInstance().getRunPlanningTable().updateRunPlanning(coolDownPlanning);
			}
		};
	}


	private void setButtonListeners() 
	{
		warmUpTimeButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				showDialog(WARM_UP_TIME_PICKER);
			}
		});
		
		mainRunTimeButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				showDialog(MAIN_RUN_TIME_PICKER);
			}
		});
		
		coolDownTimeButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				showDialog(COOL_DOWN_TIME_PICKER);
			}
		});
		
		warmUpDistanceButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				showDialog(WARM_UP_DISTANCE_PICKER);
			}
		});
		
		mainRunDistanceButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				showDialog(MAIN_RUN_DISTANCE_PICKER);
			}
		});
		
		coolDownDistanceButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				showDialog(COOL_DOWN_DISTANCE_PICKER);
			}
		});
	}

	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
		switch (id) 
		{
			case WARM_UP_TIME_PICKER:
				return getNumberPickerDialog("Set time in minutes", warmUpTimeListener, warmUpRunPlanning.mTime, maxTime);
				
			case MAIN_RUN_TIME_PICKER:
				return getNumberPickerDialog("Set time in minutes", mainRunTimeListener, mainRunPlanning.mTime, maxTime);
				
			case COOL_DOWN_TIME_PICKER:
				return getNumberPickerDialog("Set time in minutes", coolDownTimeListener, coolDownPlanning.mTime, maxTime);
				
			case WARM_UP_DISTANCE_PICKER:
				return getNumberPickerDialog("Set distance", warmUpDistanceListener, warmUpRunPlanning.mDistance, maxDistance);
				
			case MAIN_RUN_DISTANCE_PICKER:
				return getNumberPickerDialog("Set distance", mainRunDistanceListener, mainRunPlanning.mDistance, maxDistance);
				
			case COOL_DOWN_DISTANCE_PICKER:
				return getNumberPickerDialog("Set distance", coolDownDistanceListener, coolDownPlanning.mDistance, maxDistance);
 
		}
		return null;
	}
	
	private Dialog getNumberPickerDialog(String title, final OnNumberSetListener listener, int value, int maxValue)
	{
		// Create number picker dialog.
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.number_picker);
		dialog.setTitle(title);
		
		// Setup number picker.
		final NumberPicker numberPickerView = (NumberPicker)dialog.findViewById(R.id.picker);		
		
		numberPickerView.setMinValue(0);
		numberPickerView.setMaxValue(maxValue);
		numberPickerView.setValue(value);
		numberPickerView.setWrapSelectorWheel(true);
		
		Button okButton = (Button)dialog.findViewById(R.id.ok_button);
		okButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				listener.setValue(numberPickerView, numberPickerView.getValue());
				dialog.dismiss();
			}
		});
		
		Button cancelButton = (Button)dialog.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				dialog.dismiss();
			}
		});
		
		return dialog;
	}
}
