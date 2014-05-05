package com.example.comsyslabAssignment2.Model;

public class MainDishItem extends MenuItem implements Cloneable {
	
	private String	_price;
	
	public MainDishItem(String dishName, String description, String nutritionalValue, String price)
	{
		super(dishName,description,nutritionalValue);
		this._price = price;
	}
	public String getPrice()
	{
		return this._price;
	}
	public MainDishItem clone() throws CloneNotSupportedException
	{
		return (MainDishItem) super.clone();
		
	}
	
}
