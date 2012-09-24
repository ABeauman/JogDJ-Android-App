package com.alenaruprecht.jogdj.music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import com.alenaruprecht.jogdj.database.JogDJDataSource;
import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

// This service is used to calculate the tempo of songs. This service runs on a separate thread to avoid blocking the UI thread.
public class CalculateSongsTempoService extends IntentService 
{	
	private long mStartTime = 0;
	private int mSongProcessed = 0;
	
	public CalculateSongsTempoService() 
	{
		super("CalculateSongsTempoService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		List<String> paths = intent.getExtras().getStringArrayList("songs");
		List<Record> records = new ArrayList<Record>();
		
		mStartTime = SystemClock.elapsedRealtime();

		for (String path : paths) 
		{
			// Retrieve song metadata.
			AudioFile f;
			try 
			{
				f = AudioFileIO.read(new File(path));
			}
			catch (Exception e) 
			{
				continue;
			}

			Tag tag = f.getTag();

			String artist = tag.getFirst(FieldKey.ARTIST);
			String title = tag.getFirst(FieldKey.TITLE);
			
			// Retrieve song tempo using EchoNest API.
			Double tempo = EchoNest.getSongTempo(artist, title);

			// Create new record.
			Record record = new Record();
			record.setArtist(artist);
			record.setTitle(title);
			record.setPath(path);
			record.setTempo(tempo);
			
			records.add(record);

			// Commit records to the database when ten records have been created.
			if(records.size() == 10)
			{
				for(Record currentRecord : records)
				{
					JogDJDataSource.getInstance().getRecordTable().addSong(currentRecord);
				}
				
				// Notify application that records have been added to the database.
				Intent myIntent = new Intent(MusicFinderService.SONGS_ADDED);
				sendBroadcast(myIntent);
				
				records.clear();
			}
			
			mSongProcessed++;
			
			// EchoNest doesn't allow us to process more than 100 songs per minute.
			if(mSongProcessed == 100)
			{
				mSongProcessed = 0;
				
				long timeElapsed = SystemClock.elapsedRealtime() - mStartTime;
				if(timeElapsed < 60*1000)
				{
					SystemClock.sleep(60*1000 - timeElapsed + 1000 * 10);
				}
			}
		}
		
		// Commit records to the database.
		for(Record currentRecord : records)
		{
			JogDJDataSource.getInstance().getRecordTable().addSong(currentRecord);
		}
		
		Intent myIntent = new Intent(MusicFinderService.SONGS_ADDED);
		sendBroadcast(myIntent);
		
		records.clear();
	}
}
