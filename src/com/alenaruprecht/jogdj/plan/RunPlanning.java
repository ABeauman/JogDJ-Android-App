package com.alenaruprecht.jogdj.plan;

public class RunPlanning 
{
	public String mName = "";
	public Integer mTime = 0;
	public Integer mDistance = 0;
	public String mMessage = "";
	
	public RunPlanning(String name, Integer time, Integer distance, String message)
	{
		this.mName = name;
		this.mTime = time;
		this.mDistance = distance;
		this.mMessage = message;
	}
}
