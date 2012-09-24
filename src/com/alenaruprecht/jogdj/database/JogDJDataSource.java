package com.alenaruprecht.jogdj.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

// This class provides an interface to more easily interact with the application database. This class is a singleton which can be accessed through
// the getInstance method.
public class JogDJDataSource
{	
	private SQLiteDatabase mDatabase;
	private JogDJSQLDatabase mDbHelper;
	
	// Database table helpers.
	private RunPlanningTable mRunPlanningTable;
	private RecordTable mRecordTable;
	
	// JogDJDataSource singleton.
	static private JogDJDataSource mJogDJDataSource;
	
	public static void initialise(Context context)
	{
		mJogDJDataSource = new JogDJDataSource(context);
		mJogDJDataSource.open();
	}
	
	public static JogDJDataSource getInstance()
	{
		return mJogDJDataSource;
	}

	private JogDJDataSource(Context context)
	{
		mDbHelper = new JogDJSQLDatabase(context);
	}

	public void open() throws SQLException
	{
		mDatabase = mDbHelper.getWritableDatabase();
		
		mRunPlanningTable = new RunPlanningTable(mDatabase);
		mRecordTable = new RecordTable(mDatabase);
	}

	public void close()
	{
		mDbHelper.close();
	}
	
	public RecordTable getRecordTable()
	{
		return mRecordTable;
	}

	public RunPlanningTable getRunPlanningTable() 
	{
		return mRunPlanningTable;
	}
}