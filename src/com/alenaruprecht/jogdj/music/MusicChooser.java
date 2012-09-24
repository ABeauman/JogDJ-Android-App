package com.alenaruprecht.jogdj.music;

import java.util.ArrayList;
import java.util.List;
import android.os.SystemClock;
import android.util.Log;

// This class is responsible for choosing a song to play based
// on the current speed.
public class MusicChooser 
{
	// List of all songs available.
	private List<Record> mRecords = new ArrayList<Record>();
	
	// Song currently playing.
	private Record mCurrentRecord = null;
	
	// Last known speed runner speed.
	private float mSpeed = 0.0f;
	
	// Time of the last update.
	private long mLastUpdateTime = 0;
	private float mTargetSpeed = 0.0f;
	
	// Various constants.
	private float mSpeedToTempoMultiplier = 12.0f;
	private long mMinimumSongUpdateInterval = 30 * 1000; //30s
	private double mMaximumTempoDifference = 10;
	private double mMaximumTempoDifferenceWithCurrentSong = 10;
	
	public void setRecords(List<Record> records)
	{
		this.mRecords = records;
	}
	
	// Find song matching the current speed.
	private Record getRecordForSpeed(float speed)
	{
		if(mRecords.size() == 0)
			return null;
		
		// This is the tempo we are looking for.
		double tempo = speed * mSpeedToTempoMultiplier;
		Log.d("MusicChooser", "Finding song for tempo " + tempo);
		
		// Find the record with a tempo greater than the wanted tempo.
		int i = 0;
		for(i=0; i< mRecords.size(); i++)
		{
			if(mRecords.get(i).getTempo() >= tempo)
			{
				break;
			}
		}
		
		if(i == mRecords.size())
			i--;
			
		// Find 10 records that are within 10% of the desired tempo.
		List<Record> possibleRecords = new ArrayList<Record>() ;		
		possibleRecords.add(mRecords.get(i));
				 
		int j = i - 1;
		int count = 0;
		while(j >= 0 && count <= 11)
		{
			// Don't continue if tempo difference is more than 50.
			double diff = Math.abs(mRecords.get(i).getTempo() - mRecords.get(j).getTempo());
			if(diff > mMaximumTempoDifference)
			{
				break;
			}
			
			possibleRecords.add(mRecords.get(j));
			Log.d("MusicChooser", "Possible song " + mRecords.get(j));
			count++;
			j--;
		}
		
		j = i + 1;
		while(j < mRecords.size() && count <= 11)
		{
			// Don't continue if tempo difference is more than 10.
			double diff = Math.abs(mRecords.get(j).getTempo() - mRecords.get(i).getTempo() );
			if(diff > mMaximumTempoDifference)
			{
				break;
			}
			
			possibleRecords.add(mRecords.get(j));
			Log.d("MusicChooser", "Possible song " + mRecords.get(j));
			count++;
			j++;
		}
		
		 // Pick random record from list of possible records.
		 int index = (int)(Math.random() * possibleRecords.size());	
		 Record record = possibleRecords.get(index);
		 
		 if(mCurrentRecord != null)
		 {
			 double diff = Math.abs(record.getTempo() - mCurrentRecord.getTempo());
			 if(diff < mMaximumTempoDifferenceWithCurrentSong)
			 {
				 Log.d("MusicChooser", "Don't change song because tempo difference is less than " + mMaximumTempoDifferenceWithCurrentSong);
				 record = mCurrentRecord;
			 }
		 }

		 Log.d("MusicChooser", "Picked song " + record);
		 
		 return record;

	}
	
	// Calculate whether we can can change the song currently playing.
	private boolean shouldChangeSong(float newSpeed)
	{
		float diff = Math.abs(newSpeed - mTargetSpeed);
		
		// Only play a new song if the difference between new speed and old speed is more than 10% of the old speed.
		if(diff <= mTargetSpeed * 0.1f )
		{
			if(SystemClock.elapsedRealtime() - mLastUpdateTime >= mMinimumSongUpdateInterval)
			{
				mLastUpdateTime = SystemClock.elapsedRealtime();
				mTargetSpeed = newSpeed;
				return true;
			}
		}
		else
		{
			mLastUpdateTime = SystemClock.elapsedRealtime();
			mTargetSpeed = newSpeed;
		}
		
		return false;
	}
	
	public Record getSong(float newSpeed, boolean forceUpdate)
	{		
		Log.d("MusicChooser", "Get song for speed " + newSpeed);
		
		if(forceUpdate)
			mCurrentRecord = null;
		
		// Pick record with the lowest tempo if there is no previous record.
		if(mCurrentRecord == null)
		{
			Log.d("MusicChooser", "No previous song " + newSpeed);
			this.mSpeed = newSpeed;
			
			mCurrentRecord = getRecordForSpeed(mSpeed);
			mLastUpdateTime = SystemClock.elapsedRealtime();				
			
			return mCurrentRecord;
		}
		else
		{
			if(shouldChangeSong(newSpeed))
			{
				this.mSpeed = newSpeed;
				
				mCurrentRecord = getRecordForSpeed(mSpeed);
				mLastUpdateTime = SystemClock.elapsedRealtime();				
				
				return mCurrentRecord;
			}
			
			Log.d("MusicChooser", "Song should not change.");
		}
		
		return null;
	}
}
