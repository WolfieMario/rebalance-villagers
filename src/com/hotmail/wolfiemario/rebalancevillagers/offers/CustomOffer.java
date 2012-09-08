package com.hotmail.wolfiemario.rebalancevillagers.offers;

import net.minecraft.server.ItemStack;
import net.minecraft.server.MerchantRecipe;

/**
 * @author Gerrard Lukacs
 */
public class CustomOffer extends AbstractOffer
{
	private ItemStackProducer buyA;
	private ItemStackProducer buyB;
	private ItemStackProducer sell;
	
	public CustomOffer(ItemStackProducer a, ItemStackProducer b, ItemStackProducer c, float prob)
	{
		super(prob);
		buyA = a;
		buyB = b;
		sell = c;
	}
	
	public CustomOffer(ItemStackProducer a, ItemStackProducer c, float prob)
	{
		this(a, null, c, prob);
	}
	
	public MerchantRecipe getOffer()
	{
		ItemStack stackBuyA = buyA.getItemStack();
		ItemStack stackSell = sell.getItemStack();
		
		if(buyB == null)
			return new MerchantRecipe(stackBuyA, stackSell);
		
		ItemStack stackBuyB = buyB.getItemStack();
		return new MerchantRecipe(stackBuyA, stackBuyB, stackSell);
	}
}
