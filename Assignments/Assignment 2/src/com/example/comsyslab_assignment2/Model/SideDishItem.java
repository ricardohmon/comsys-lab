package com.example.comsyslab_assignment2.Model;

public class SideDishItem extends MenuItem implements Cloneable {

	public SideDishItem(String dishName, String description,
			String nutritionalValue) {
		super(dishName, description, nutritionalValue);
	}

	public SideDishItem clone() throws CloneNotSupportedException
	{
		return (SideDishItem) super.clone();
		
	}
	
}
