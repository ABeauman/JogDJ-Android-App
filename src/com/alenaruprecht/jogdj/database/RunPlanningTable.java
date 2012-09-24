package com.alenaruprecht.jogdj.database;

import java.util.ArrayList;
import java.util.List;

import com.alenaruprecht.jogdj.plan.RunPlanning;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Helper class for managing the run planning table.
public class RunPlanningTable 
{
	public static final String TABLE_NAME = "runplanning";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PHASE = "phase";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_DISTANCE = "distance";
	public static final String COLUMN_MESSAGE = "message";
	
	public static final String TABLE_CREATE = "create table "
			+ TABLE_NAME + "( " 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PHASE+ " text not null, "
			+ COLUMN_TIME+ " integer, "
			+ COLUMN_DISTANCE + " integer, "
			+ COLUMN_MESSAGE+ " text not null"
			+ ");";
	
	public static String[] ALL_COLUMNS = { COLUMN_ID,
										   COLUMN_PHASE,
										   COLUMN_TIME,
										   COLUMN_DISTANCE,
										   COLUMN_MESSAGE};
	
	private SQLiteDatabase database;
	
	public RunPlanningTable(SQLiteDatabase database)
	{
		this.database = database;
	}
	
	// Add run planning to the database.
	public void addRunPlanning(RunPlanning runPlanning)
	{
		ContentValues values = RunPlanningTable.runPlanningToContentValues(runPlanning);

		Cursor cursor = database.query(TABLE_NAME, ALL_COLUMNS, COLUMN_PHASE + " = '" + runPlanning.mName + "'", null, null, null, null);
		
		if(cursor.getCount() == 0)
		{
			database.insert(TABLE_NAME, null, values);
		}
		
		cursor.close();
	}
	
	// Update run planning.
	public void updateRunPlanning(RunPlanning runPlanning)
	{
		ContentValues values = RunPlanningTable.runPlanningToContentValues(runPlanning);

		database.update(TABLE_NAME, values, COLUMN_PHASE + " = '" + runPlanning.mName + "'", null);
	}
	
	// Check whether run planning exists in the database.
	public Boolean planningExist(String name)
	{
		Cursor cursor = database.query(TABLE_NAME, ALL_COLUMNS, COLUMN_PHASE + " = '" + name + "'", null, null, null, null);
		
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return false;
		}
		
		cursor.close();
		return true;
	}
	
	// Get run planning with specific name. The function returns nulll if the run planning doesn't exist.
	public RunPlanning getRunPlanning(String name)
	{
		Cursor cursor = database.query(TABLE_NAME, ALL_COLUMNS, COLUMN_PHASE + " = '" + name + "'", null, null, null, null);
		
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return null;
		}
		
		cursor.moveToFirst();
		RunPlanning runPlanning = RunPlanningTable.cursorToRunPlanning(cursor);
		
		cursor.close();
		return runPlanning;
	}
	
	// Get all run plannings stored in the database.
	public List<RunPlanning> getRunPlannings()
	{
		List<RunPlanning> runPlannings = new ArrayList<RunPlanning>();
		Cursor cursor = database.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) 
		{
			runPlannings.add(RunPlanningTable.cursorToRunPlanning(cursor));
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return runPlannings;
	}
	
	// Convert run planning table cursor to a Record.
	static private RunPlanning cursorToRunPlanning(Cursor cursor)
	{
		return new RunPlanning(cursor.getString(1),
							   cursor.getInt(2),
							   cursor.getInt(3),
							   cursor.getString(4));
	}
	
	// Convert run planning to cursor.
	static private ContentValues runPlanningToContentValues(RunPlanning runPlanning)
	{
		ContentValues values = new ContentValues();
		values.put(COLUMN_PHASE, runPlanning.mName);
		values.put(COLUMN_TIME, runPlanning.mTime);
		values.put(COLUMN_DISTANCE, runPlanning.mDistance);
		values.put(COLUMN_MESSAGE, runPlanning.mMessage);
		
		return values;
	}
}
