package com.alenaruprecht.jogdj.music;

import java.util.List;

import android.util.Log;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.SongParams;

// Helper class for querying song information through EchoNest.
public class EchoNest
{
	// Key to access EchoNest API.
	static private final String key = "XXRR4WTLMK2WEVL9M";
	static private final EchoNestAPI echoNestApi = new EchoNestAPI(key);
	
	// Get song tempo.
	static public Double getSongTempo(String artist, String title)
	{
		SongParams params = new SongParams();
		params.setArtist(artist);
		params.setTitle(title);
		
		Double tempo = 0.0;
		try
		{
			// Query song tempo.
			List<com.echonest.api.v4.Song> songs = echoNestApi.searchSongs(params);
			if(songs.size() > 0)
			{
				tempo = songs.get(0).getTempo();
			}
		} 
		catch (EchoNestException e)
		{
			Log.e("EchoNest", e.toString());
		}
		
		return tempo;
	}
}
