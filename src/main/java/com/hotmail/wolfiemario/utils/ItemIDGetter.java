package com.hotmail.wolfiemario.utils;

import java.util.HashMap;

import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.Item;

/**
 * This utility class allows item and block IDs to be retrieved using the names specified for them in Minecraft's source code,
 * and also allows custom names to be associated with IDs.
 * @author Gerrard Lukacs
 */
public class ItemIDGetter
{
	private static HashMap<String, Item> blockItemMap;
	
	/**
	 * Gets the item or block ID for the given item or block name.
	 * @param name - the name of the desired item or block
	 * @return The int value of the ID of this item or block, or -1 if no such item or block exists.
	 */
	public static Item getItemOrBlock(String name)
	{
	    Item item = blockItemMap.get(name);
		
		if (item == null) {
	        //Check if the string is itself an ID number.
	        try
	        {
	            item = Item.d(Integer.parseInt(name));
	        }
	        catch(NumberFormatException e)
	        {
	            //Squelch exception because it simply means name wasn't a numeric ID.
	        }		    
		}
		

		if (item == null) {
            //Check if the string is itself an ID number.
            try
            {
                item = Item.getItemOf(Block.e(Integer.parseInt(name)));
            }
            catch(NumberFormatException e)
            {
                //Squelch exception because it simply means name wasn't a numeric ID.
            }           
        }
		
		
		
		return item;
	}
	
	/**
	 * Registers a custom item or block name to the given ID. This can also overwrite a previously registered name.
	 * @param name - the desired item or block name
	 * @param id - the id this name is meant to represent
	 */
	public static void registerName(String name, Item itemOrBlock)
	{
	    blockItemMap.put(name, itemOrBlock);
	}
	
	//Initializes the map of item/block names and ids
	static
	{
		try
		{
		    blockItemMap = new HashMap<String, Item>();
			
			//load block names
			for(int i = 0; i < 4096; i++)
			{
				Item block = Item.getItemOf(Block.e(i));
				
				if(block != null && block.getName() != null)
				{
					String name = block.getName();
					String pureName = name.substring(5);
					
					blockItemMap.put(name, block);
					blockItemMap.put(pureName, block);
				}
			}
			
			//load item names
			for(int i = 0; i < 32000; i++)
			{
				Item item = Item.d(i);
				
				if(item != null && item.getName() != null)
				{
					String name = item.getName();
					String pureName = name.substring(5);
					
					blockItemMap.put(name, item);
					blockItemMap.put(pureName, item);
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
