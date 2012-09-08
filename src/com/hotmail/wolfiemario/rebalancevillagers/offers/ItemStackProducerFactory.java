package com.hotmail.wolfiemario.rebalancevillagers.offers;

import java.util.Random;

import net.minecraft.server.EnchantmentManager;
import net.minecraft.server.ItemStack;

import org.bukkit.craftbukkit.inventory.CraftItemStack;


public class ItemStackProducerFactory
{
	private static Random random = new Random();
	
	public static ItemStackProducer createItemStackProducer(int id, int min, int max, int damage)
	{
		return new DefaultStackProducer(id, min, max, damage);
	}
	
	public static ItemStackProducer createItemStackProducer(int id, int min, int max)
	{
		return createItemStackProducer(id, min, max, 0);
	}
	
	public static ItemStackProducer createItemStackProducer(int id, int min, int max, int damage, int enchMin, int enchMax)
	{
		return new EnchantedStackProducer(id, min, max, damage, enchMin, enchMax);
	}
	
	public static ItemStackProducer createItemStackProducer(ItemStack stack)
	{
		return new FixedStackProducer(stack);
	}
	
	public static ItemStackProducer createItemStackProducer(org.bukkit.inventory.ItemStack stack)
	{
		return createItemStackProducer( CraftItemStack.createNMSItemStack(stack) );
	}
	
	
	private static class DefaultStackProducer implements ItemStackProducer
	{
		protected int itemId;
		protected int minimum;
		protected int maximum;
		protected int damage;
		
		public DefaultStackProducer(int id, int min, int max, int dmg)
		{
			itemId = id;
			minimum = min;
			maximum = max;
			damage = dmg;
		}
		
		@Override
		public ItemStack getItemStack()
		{
			if(maximum > minimum)
				return new ItemStack(itemId, minimum + random.nextInt(maximum - minimum + 1), damage);
			else
				return new ItemStack(itemId, minimum, damage);
		}
		
	}
	
	private static class EnchantedStackProducer extends DefaultStackProducer implements ItemStackProducer
	{
		protected int minimumLevel;
		protected int maximumLevel;
		
		public EnchantedStackProducer(int id, int min, int max, int dmg, int enchMin, int enchMax)
		{
			super(id, min, max, dmg);
			minimumLevel = enchMin;
			maximumLevel = enchMax;
		}
		
		@Override
		public ItemStack getItemStack()
		{
			if(maximumLevel > minimumLevel)
				return EnchantmentManager.a(random, super.getItemStack(), minimumLevel + random.nextInt(maximumLevel - minimumLevel + 1));
			else
				return EnchantmentManager.a(random, super.getItemStack(), minimumLevel);
		}
		
	}
	
	private static class FixedStackProducer implements ItemStackProducer
	{
		protected ItemStack stack;
		
		public FixedStackProducer(ItemStack s)
		{
			stack = s;
		}
		
		@Override
		public ItemStack getItemStack()
		{
			return stack;
		}
		
	}
}
