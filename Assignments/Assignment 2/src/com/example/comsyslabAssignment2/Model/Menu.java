package com.example.comsyslabAssignment2.Model;

import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.util.Log;

public class Menu {
	private ArrayList<MainDishItem> _mainDishes;
	private ArrayList<SideDishItem> _sideDishes;
	
	public Menu(String rawHTML) 
	{
		_mainDishes = new ArrayList<MainDishItem>();
		_sideDishes = new ArrayList<SideDishItem>();
		try {
			Document doc = Jsoup.parse(rawHTML);
			if(doc != null && doc.hasText())
			{
				Elements tables = doc.select("table");
				if(tables.size() > 0)
				{
					loadMainDishes(tables.get(0));
					if(tables.get(1) != null)
					{
						loadSideDishes(tables.get(1));
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.e(com.example.comsyslabAssignment2.Model.Constants.HTML_PARSING_TAG, "Error while parsing Menu",e);
		}
	}
	
	public ArrayList<MainDishItem> getMainDishes()
	{
		return new ArrayList<MainDishItem>(this._mainDishes);
	}
	public ArrayList<SideDishItem> getSideDishes()
	{
		return new ArrayList<SideDishItem>(this._sideDishes);
	}
	
	private void loadMainDishes(org.jsoup.nodes.Element tableElement)
	{
		try {
			for(Iterator<org.jsoup.nodes.Element> elem_it = tableElement.select("tr").iterator(); elem_it.hasNext();)
			{
				String dishName = "";
				String description = "";
				String nutritionalValue = "";
				String price = "";
				org.jsoup.nodes.Element dishInfoElem;
				// Extract every single piece of information from DOM
				dishInfoElem = elem_it.next();
				Elements tdElems = dishInfoElem.select("td");
				if(tdElems.size() > 0)
					dishName = tdElems.get(0).select("b").text();
				if(tdElems.size() > 1)
					description = tdElems.get(1).ownText();
				if(tdElems.size() > 2)
					price = tdElems.get(2).ownText();
				
				if(elem_it.hasNext())
				{
					nutritionalValue = elem_it.next().text();
				}
				_mainDishes.add(new MainDishItem(dishName,description,nutritionalValue,price));
				
			}
		}
		catch(Exception e)
		{
			Log.e(com.example.comsyslabAssignment2.Model.Constants.HTML_PARSING_TAG, "Error while parsing Main dishes",e);
		}
	}
	private void loadSideDishes(org.jsoup.nodes.Element tableElement)
	{
		try {
			for(Iterator<org.jsoup.nodes.Element> elem_it = tableElement.select("tr").iterator(); elem_it.hasNext();)
			{
				String dishName = "";
				String description = "";
				String nutritionalValue = "";
				org.jsoup.nodes.Element dishInfoElem;
				// Extract every single piece of information from DOM
				dishInfoElem = elem_it.next();
				Elements tdElems = dishInfoElem.select("td");
				if(tdElems.size() > 0)
					dishName = tdElems.get(0).select("b").text();
				if(tdElems.size() > 1)
					description = tdElems.get(1).ownText();
				
				if(elem_it.hasNext())
				{
					nutritionalValue = elem_it.next().text();
				}
				_sideDishes.add(new SideDishItem(dishName,description,nutritionalValue));
				
			}
		}
		catch(Exception e)
		{
			Log.e(com.example.comsyslabAssignment2.Model.Constants.HTML_PARSING_TAG, "Error while parsing Side dishes",e);
		}
	}
}
