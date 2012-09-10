package com.hotmail.wolfiemario.utils;

import java.util.HashMap;
import net.minecraft.server.Block;
import net.minecraft.server.Item;

/**
 * This utility class allows item and block IDs to be retrieved using the names specified for them in Minecraft's source code,
 * and also allows custom names to be associated with IDs.
 * @author Gerrard Lukacs
 */
public class ItemIDGetter
{
	private static HashMap<String, Integer> idMap;
	
	/**
	 * Gets the item or block ID for the given item or block name.
	 * @param name - the name of the desired item or block
	 * @return The int value of the ID of this item or block, or -1 if no such item or block exists.
	 */
	public static int getID(String name)
	{
		Integer id = idMap.get(name);
		
		if(id == null)
			id = -1;
		
		//Check if the string is itself an ID number.
		try
		{
			id = Integer.parseInt(name);
		}
		catch(NumberFormatException e)
		{
			//Squelch exception because it simply means name wasn't a numeric ID.
		}
		
		return id;
	}
	
	/**
	 * Registers a custom item or block name to the given ID. This can also overwrite a previously registered name.
	 * @param name - the desired item or block name
	 * @param id - the id this name is meant to represent
	 */
	public static void registerName(String name, int id)
	{
		idMap.put(name, id);
	}
	
	//Initializes the map of item/block names and ids
	static
	{
		try
		{
			idMap = new HashMap<String, Integer>();
			
			//load block names
			for(int i = 0; i < 4096; i++)
			{
				Block block = Block.byId[i];
				
				if(block != null && block.a() != null)
				{
					String name = block.a();
					String pureName = name.substring(5);
					
					idMap.put(name, i);
					idMap.put(pureName, i);
				}
			}
			
			//load item names
			for(int i = 0; i < 32000; i++)
			{
				Item item = Item.byId[i];
				
				if(item != null && item.getName() != null)
				{
					String name = item.getName();
					String pureName = name.substring(5);
					
					idMap.put(name, i);
					idMap.put(pureName, i);
				}
			}
		}
		catch(RuntimeException e)
		{
			//This is so we have an idea of what goes wrong if something does go wrong.
			//The alternative, ExceptionInInitializerError, is not particularly helpful.
			e.printStackTrace();
			throw e; //Not that you'd see it, since this is an initializer.
		}
		
	}
}
