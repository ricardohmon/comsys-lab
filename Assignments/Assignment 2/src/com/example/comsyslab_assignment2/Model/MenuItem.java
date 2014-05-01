package com.example.comsyslab_assignment2.Model;

public abstract class MenuItem implements Cloneable {

	private String _dishName;
	private String _description;
	private String _nutritionalValue;
	
	public MenuItem(String dishName, String description, String nutritionalValue)
	{
		this._dishName = dishName;
		this._description = description;
		this._nutritionalValue = nutritionalValue;
	}
	public String getDishName()
	{
		return this._dishName;
	}
	public String getDescription()
	{
		return this._description;
	}
	public String getNutritionalValue()
	{
		return this._nutritionalValue;
	}
	public MenuItem clone() throws CloneNotSupportedException
	{
		return (MenuItem) super.clone();
		
	}
	
}
