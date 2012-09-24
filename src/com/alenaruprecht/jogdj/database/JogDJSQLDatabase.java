package com.alenaruprecht.jogdj.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Helper class to help create and upgrade the application database.
public class JogDJSQLDatabase extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "jogDJDatabase.db";
	private static final int DATABASE_VERSION = 1;

	public JogDJSQLDatabase(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		database.execSQL(RecordTable.TABLE_CREATE);
		database.execSQL(RunPlanningTable.TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS " + RecordTable.TABLE_CREATE);
		db.execSQL("DROP TABLE IF EXISTS " + RunPlanningTable.TABLE_CREATE);
		onCreate(db);
	}
}