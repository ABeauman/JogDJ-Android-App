package com.alenaruprecht.jogdj.music;

import com.alenaruprecht.jogdj.R;
import com.alenaruprecht.jogdj.database.RecordTable;
import com.alenaruprecht.jogdj.database.JogDJDataSource;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

// This activity is used to display songs all the songs stored on the device.
public class SongExplorerActivity extends Activity 
{
	// Reference to the list view used to display the songs.
	ListView mSongListView = null;
	
	// A reference to the music player service.
	private MusicPlayerService mMusicPlayerService;
	
	// This connection is used to bind to the music player service.
	private ServiceConnection mMusicPlayerConnection = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			mMusicPlayerService = ((MusicPlayerService.MusicPlayerBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) 
		{
			mMusicPlayerService = null;
		}
	};
	
	// This receiver is used to update the song list when a new song is found on the device.
	private BroadcastReceiver mSongAddedReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Update song list.
			Cursor cursor = JogDJDataSource.getInstance().getRecordTable().getAllSongsCursor();
			startManagingCursor(cursor);		
			
			((SimpleCursorAdapter) mSongListView.getAdapter()).changeCursor(cursor);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.song_explorer);

		mSongListView = (ListView) findViewById(R.id.list);

		// Get records cursor.
		Cursor mCursor = JogDJDataSource.getInstance().getRecordTable().getAllSongsCursor();
		startManagingCursor(mCursor);

		bindService(new Intent(SongExplorerActivity.this, MusicPlayerService.class), mMusicPlayerConnection, Context.BIND_AUTO_CREATE);

		ListAdapter adapter = new SimpleCursorAdapter(this,
													  android.R.layout.two_line_list_item, 
													  mCursor, 
													  new String[] { RecordTable.COLUMN_TITLE, RecordTable.COLUMN_TEMPO },
													  new int[] {android.R.id.text1, android.R.id.text2 });

		mSongListView.setAdapter(adapter);

		mSongListView.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				// Play song
				Cursor cursor = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();
				cursor.moveToPosition(position);
				mMusicPlayerService.play(RecordTable.cursorToRecord(cursor));
			}
		});

	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		registerReceiver(mSongAddedReceiver, new IntentFilter(MusicFinderService.SONGS_ADDED));
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		mMusicPlayerService.reset();
		
		unregisterReceiver(mSongAddedReceiver);
		unbindService(mMusicPlayerConnection);
	}
}
