package com.alenaruprecht.jogdj.music;

import java.util.Stack;
import com.alenaruprecht.jogdj.database.JogDJDataSource;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

// This service is responsible for finding music stored on the phone. When a music file is found the service uses the EchonestAPI
// to find the tempo of the song and stores the result in the application database.
public class MusicFinderService extends Service
{
	// Message send when songs have been added to the database.
	public static String SONGS_ADDED = "SONGS_ADDED";	
	
	private Stack<String> mSongsToProcess = new Stack<String>();	
	
	private IBinder mBinder = new MusicFinderBinder();
    public class MusicFinderBinder extends Binder 
    {
    	public MusicFinderService getService() 
    	{
            return MusicFinderService.this;
        }
    } 
    	
	public void start()
	{
		String[] proj = { MediaStore.Audio.Media._ID,
				 		  MediaStore.Audio.Media.ARTIST,
				 		  MediaStore.Audio.Media.TITLE,
				 		  MediaStore.Audio.Media.DATA};

		// List songs stored on the device.
		Cursor musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
		while(musicCursor.moveToNext())
		{
			String path = musicCursor.getString(3);
			if (path.equals("") == false)
			{
				// Make sure the song is not already in the database.
				Record record = JogDJDataSource.getInstance().getRecordTable().getSong(path);
				
				if(record == null || record.getTempo() == 0.0)
				{
					mSongsToProcess.push(path);
				}
			}
		}
		
		if(mSongsToProcess.size() != 0)
		{
			if(isNetworkAvailable() == false)
			{
				Toast.makeText(getApplicationContext(), "No Internet connection, unable to calculate songs tempo.", Toast.LENGTH_LONG).show();
			}
			else
			{
				CharSequence text = "Found " + mSongsToProcess.size() + " songs. Calculating tempo ...";
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
				
				// Calculate tempo for songs.
				Intent intent = new Intent(this, CalculateSongsTempoService.class);
				intent.putExtra("songs", mSongsToProcess);
				startService(intent);
			}
		}
	}
	
	// Check whether the device is connected to the Internet.
	private boolean isNetworkAvailable()
	{
	    return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}
}
