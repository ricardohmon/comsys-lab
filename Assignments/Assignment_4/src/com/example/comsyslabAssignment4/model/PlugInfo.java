package com.example.comsyslabAssignment4.model;

import com.example.comsyslabAssignment4.constants.Constants.Status;

public class PlugInfo {
	private Status mStatus;
	private int mPlugId;
	private String mName;
	
	public PlugInfo(int plugId, Status status, String name)
	{
		this.mStatus = status;
		this.mPlugId = plugId;
		this.mName = name;
	}
	
	public String getName()
	{
		return this.mName;
	}
	
	public int getPlugId()
	{
		return this.mPlugId;
	}
	
	public Status getStatus()
	{
		return this.mStatus;
	}
}
