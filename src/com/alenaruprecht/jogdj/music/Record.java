package com.alenaruprecht.jogdj.music;

// Class representing a song.
public class Record
{
	private long mId;
	private String mTitle;
	private String mArtist;
	private Double mTempo;
	private String mPath;

	public long getId()
	{
		return mId;
	}

	public void setId(long id)
	{
		this.mId = id;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public void setTitle(String title)
	{
		this.mTitle = title;
	}

	public String getArtist()
	{
		return mArtist;
	}

	public void setArtist(String artist)
	{
		this.mArtist = artist;
	}

	public Double getTempo()
	{
		return mTempo;
	}

	public void setTempo(Double tempo)
	{
		this.mTempo = tempo;
	}

	public String getPath()
	{
		return mPath;
	}

	public void setPath(String path)
	{
		this.mPath = path;
	}
	
	public String toString()
	{
		return mTitle + " " + mTempo;
	}
}
