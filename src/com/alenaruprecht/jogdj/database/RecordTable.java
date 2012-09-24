package com.alenaruprecht.jogdj.database;

import java.util.ArrayList;
import java.util.List;

import com.alenaruprecht.jogdj.music.Record;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Helper class for managing the record table.
public class RecordTable 
{
	public static final String TABLE_SONGS = "songs";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ARTIST = "artist";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TEMPO = "tempo";
	public static final String COLUMN_PATH = "key";
	
	public static final String TABLE_CREATE = "create table "
			+ TABLE_SONGS + "( " 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_ARTIST+ " text not null, "
			+ COLUMN_TITLE+ " text not null, "
			+ COLUMN_TEMPO+ " double, "
			+ COLUMN_PATH  + " text not null unique);";
	
	public static String[] ALL_COLUMNS = { COLUMN_ID,
										   COLUMN_ARTIST,
										   COLUMN_TITLE,
										   COLUMN_TEMPO,
										   COLUMN_PATH};
	
	private SQLiteDatabase database;
	
	public RecordTable(SQLiteDatabase database)
	{
		this.database = database;
	}
	
	// Add a new song to the database.
	public void addSong(Record record)
	{
		ContentValues values = new ContentValues();
		values.put(RecordTable.COLUMN_ARTIST, record.getArtist());
		values.put(RecordTable.COLUMN_TITLE, record.getTitle());
		values.put(RecordTable.COLUMN_TEMPO, record.getTempo());
		values.put(RecordTable.COLUMN_PATH, record.getPath());
		
		// Escape ' character.
		String path = record.getPath().replaceAll("'","''");
		Cursor cursor = database.query(RecordTable.TABLE_SONGS, RecordTable.ALL_COLUMNS, RecordTable.COLUMN_PATH + " = '" + path + "'", null, null, null, null);
		
		if(cursor.getCount() == 0)
		{
			database.insert(RecordTable.TABLE_SONGS, null, values);
		}
		cursor.close();
	}	
	
	public Cursor getAllSongsCursor()
	{
		return database.query(RecordTable.TABLE_SONGS, RecordTable.ALL_COLUMNS, null, null, null, null, null);
	}

	// Get song at specific path.
	public Record getSong(String path)
	{
		Record song = null;

		// Escape ' character.
		path = path.replaceAll("'","''");
		Cursor cursor = database.query(RecordTable.TABLE_SONGS, RecordTable.ALL_COLUMNS, RecordTable.COLUMN_PATH + "=\"" + path + "\"", null, null, null, null);

		if(cursor.getCount() != 0)
		{
			cursor.moveToFirst();
			song = cursorToRecord(cursor);
		}

		cursor.close();
		return song;
	}
	
	// Get all songs in the database.
	public List<Record> getAllSongs()
	{
		List<Record> records = new ArrayList<Record>();

		Cursor cursor = database.query(RecordTable.TABLE_SONGS, RecordTable.ALL_COLUMNS, RecordTable.COLUMN_TEMPO + "!=\"" + 0 + "\"", null, null, null, RecordTable.COLUMN_TEMPO);

		cursor.moveToFirst();
		for(int i=0;i < cursor.getCount(); i++)
		{
			records.add(cursorToRecord(cursor));
			cursor.moveToNext();
		}

		cursor.close();		
		return records;
	}

	// Convert record table cursor to a Record.
	static public Record cursorToRecord(Cursor cursor)
	{
		Record song = new Record();
		song.setId(cursor.getLong(0));
		song.setArtist(cursor.getString(1));
		song.setTitle(cursor.getString(2));
		song.setTempo(cursor.getDouble(3));
		song.setPath(cursor.getString(4));
		return song;
	}
}
