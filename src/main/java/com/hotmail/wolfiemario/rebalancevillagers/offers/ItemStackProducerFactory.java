package com.hotmail.wolfiemario.rebalancevillagers.offers;

import java.util.Random;

import net.minecraft.server.v1_8_R1.EnchantmentManager;
import net.minecraft.server.v1_8_R1.Item;
import net.minecraft.server.v1_8_R1.ItemStack;

import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;


/**
 * This factory can create ItemStackProducers to suit a variety of needs.
 * @author Gerrard Lukacs
 */
public class ItemStackProducerFactory
{
	private static Random random = new Random();
	
	/**
	 * Creates an ItemStackProducer which produces ItemStacks of the given id and damage value,
	 * with a random count anywhere between min and max, inclusive.
	 * @param id - the item or block id for all ItemStacks produced by this producer.
	 * @param min - the minimum (inclusive) count (number of stacked item) for ItemStacks produced by this producer.
	 * @param max - the maximum (inclusive) count (number of stacked item) for ItemStacks produced by this producer.
	 * @param damage - the damage value for all ItemStacks produced by this producer.
	 * @return An ItemStackProducer based on the above parameters.
	 */
	public static ItemStackProducer createItemStackProducer(Item item, int min, int max, int damage)
	{
		return new DefaultStackProducer(item, min, max, damage);
	}
	
	/**
	 * Creates an ItemStackProducer which produces ItemStacks of the given id,
	 * with a random count anywhere between min and max, inclusive. The damage value will be 0.
	 * @param id - the item or block id for all ItemStacks produced by this producer.
	 * @param min - the minimum (inclusive) count (number of stacked item) for ItemStacks produced by this producer.
	 * @param max - the maximum (inclusive) count (number of stacked item) for ItemStacks produced by this producer.
	 * @return An ItemStackProducer based on the above parameters.
	 */
	public static ItemStackProducer createItemStackProducer(Item item, int min, int max)
	{
		return createItemStackProducer(item, min, max, 0);
	}
	
	/**
	 * Creates an ItemStackProducer which produces ItemStacks of the given id and damage value,
	 * with a random count anywhere between min and max (inclusive), and a random enchantment equivalent to an
	 * enchantment on this item at a level anywhere between enchMin and enchMax (inclusive). Note that items with
	 * an enchantability of 0 will simply not receive enchantments.
	 * @param id - the item or block id for all ItemStacks produced by this producer.
	 * @param min - the minimum (inclusive) count (number of stacked item) for ItemStacks produced by this producer.
	 * @param max - the maximum (inclusive) count (number of stacked item) for ItemStacks produced by this producer.
	 * @param damage - the damage value for all ItemStacks produced by this producer.
	 * @param enchMin - the minimum (inclusive) level this producer will enchant an item at.
	 * @param enchMax - the maximum (inclusive) level this producer will enchant an item at.
	 * @return An ItemStackProducer based on the above parameters.
	 */
	public static ItemStackProducer createItemStackProducer(Item item, int min, int max, int damage, int enchMin, int enchMax)
	{
		return new EnchantedStackProducer(item, min, max, damage, enchMin, enchMax);
	}
	
	/**
	 * Creates an ItemStackProducer which will always produce an ItemStack identical to the input stack.
	 * Note that it is possible the produced ItemStack will simply be a reference to the actual ItemStack passed as a parameter here.
	 * @param stack - the (NMS) ItemStack this ItemStackProducer will produce.
	 * @return An ItemStackProducer which produces ItemStacks identical to the stack parameter.
	 */
	public static ItemStackProducer createItemStackProducer(ItemStack stack)
	{
		return new FixedStackProducer(stack);
	}
	
	/**
	 * Creates an ItemStackProducer which will always produce an ItemStack identical to the input stack.
	 * Note that it is possible the produced ItemStack will simply be a reference to the actual ItemStack passed as a parameter here.
	 * @param stack - the ItemStack this ItemStackProducer will produce.
	 * @return An ItemStackProducer which produces (NMS) ItemStacks based on the stack parameter.
	 */
	public static ItemStackProducer createItemStackProducer(org.bukkit.inventory.ItemStack stack)
	{
		return createItemStackProducer( CraftItemStack.asNMSCopy(stack) );
	}
	
	
	private static class DefaultStackProducer implements ItemStackProducer
	{
		protected Item item;
		protected int minimum;
		protected int maximum;
		protected int damage;
		
		public DefaultStackProducer(Item _item, int min, int max, int dmg)
		{
			item = _item;
			minimum = min;
			maximum = max;
			damage = dmg;
		}
		
		public ItemStack getItemStack()
		{
			if(maximum > minimum)
				return new ItemStack(item, minimum + random.nextInt(maximum - minimum + 1), damage);
			else
				return new ItemStack(item, minimum, damage);
		}
		
	}
	
	private static class EnchantedStackProducer extends DefaultStackProducer implements ItemStackProducer
	{
		protected int minimumLevel;
		protected int maximumLevel;
		
		public EnchantedStackProducer(Item item, int min, int max, int dmg, int enchMin, int enchMax)
		{
			super(item, min, max, dmg);
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
		
		public ItemStack getItemStack()
		{
			return stack;
		}
		
	}
}
