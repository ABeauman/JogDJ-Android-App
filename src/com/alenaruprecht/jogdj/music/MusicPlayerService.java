package com.alenaruprecht.jogdj.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.MediaController.MediaPlayerControl;

// This class is used to play music.
public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener
{
	public static String MUSIC_PLAYING = "MUSIC_PLAYING";
	public static String MUSIC_FINISHED = "MUSIC_FINISHED";
	
    private MediaPlayer mMediaPlayer = null;
    private Record mRecord; 
    
    private boolean mPause = false;
    
    private IBinder mBinder = new MusicPlayerBinder();
    public class MusicPlayerBinder extends Binder 
    {
    	public MusicPlayerService getService() 
    	{
            return MusicPlayerService.this;
        }
    } 
    
    @Override
    public void onCreate()
    {
    	mMediaPlayer = new MediaPlayer(); 
    	mMediaPlayer.setOnPreparedListener(this);
    	mMediaPlayer.setOnCompletionListener(this);
    	mMediaPlayer.setOnErrorListener(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) 
    {
        return 0;
    }
    
    public void play(Record record)
    {
		mMediaPlayer.reset();
    	
    	try 
    	{
    		mMediaPlayer.setDataSource(record.getPath());
    		mMediaPlayer.prepareAsync();
    		
    		mPause = false;
		} 
    	catch (Exception e) 
    	{
			e.printStackTrace();
		} 
    	
    	mPause = false;
    	this.mRecord = record;
    }
    
    public void start()
    {
    	mMediaPlayer.start();
    }
    
    public void pause()
    {
    	if(isPlaying())
    	{
    		mMediaPlayer.pause();
    		mPause = true;
    	}
    }
    
    public void reset()
    {
    	mMediaPlayer.reset();
    }
    
    public boolean isPlaying()
    {
    	return mMediaPlayer.isPlaying();
    }
    
    public boolean isPause()
    {
    	return mPause;
    }
    
    public void playFadeOutFadeIn(Record record)
    {
    	
    }

	public void onPrepared(MediaPlayer player) 
	{
		player.start();

		Intent intent = new Intent(MUSIC_PLAYING);
		intent.putExtra("title", mRecord.getTitle());
		intent.putExtra("tempo", mRecord.getTempo());
		sendBroadcast(intent);
	}
	
	public void onCompletion(MediaPlayer player)
	{
		if(mRecord != null)
		{
			Intent intent = new Intent(MUSIC_FINISHED);
			intent.putExtra("title", mRecord.getTitle());
			sendBroadcast(intent);
		}
	}
	
	public boolean onError(MediaPlayer player, int what, int extra)
	{
		return true;
	}	

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}
	
    @Override
    public void onDestroy() 
    {
    	mMediaPlayer.release();
    }
    
    public void playSongFadeOut(Record record, float fadeOutDuration)
    {
    	/*
    	Timer timer = new Timer();
	    timer.schedule(new TimerTask() 
	    {
			@Override
			public void run() 
			{
				mMediaPlayer.setVolume(volume, volume);
			}

		}, 0, 5000);
	    
    	
        volume -= speed* deltaTime
		*/
    }
    public void FadeIn(float deltaTime)
    {
    	/*
    	mMediaPlayer.setVolume(volume, volume);
        volume += speed* deltaTime
		*/
    }
}