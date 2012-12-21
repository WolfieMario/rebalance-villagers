package com.hotmail.wolfiemario.rebalancevillagers.offers;

import net.minecraft.server.v1_4_6.ItemStack;
import net.minecraft.server.v1_4_6.MerchantRecipe;

/**
 * A CustomOffer, like an AbstractOffer, represents a merchant offer's probability value. In addition, it represents
 * the offer itself, in the form of ItemStackProducers for the three slots involved in a MerchantRecipe.
 * @author Gerrard Lukacs
 */
public class CustomOffer extends AbstractOffer
{
	private ItemStackProducer buyA;
	private ItemStackProducer buyB;
	private ItemStackProducer sell;
	
	/**
	 * Constructs a new CustomOffer.
	 * @param a - an ItemStackProducer which will provide the ItemStack for the offer's first input slot.
	 * @param b - an ItemStackProducer which will provide the ItemStack for the offer's second input slot.
	 * @param c - an ItemStackProducer which will provide the ItemStack for the offer's output slot.
	 * @param prob - the probability value of this offer. Represents the chance of this offer being
	 * considered for addition to a villager's offer list. Valid range is [0, 1].
	 */
	public CustomOffer(ItemStackProducer a, ItemStackProducer b, ItemStackProducer c, float prob)
	{
		super(prob);
		buyA = a;
		buyB = b;
		sell = c;
	}
	
	/**
	 * Constructs a new CustomOffer.
	 * @param a - an ItemStackProducer which will provide the ItemStack for the offer's first input slot.
	 * @param c - an ItemStackProducer which will provide the ItemStack for the offer's output slot.
	 * @param prob - the probability value of this offer. Represents the chance of this offer being
	 * considered for addition to a villager's offer list. Valid range is [0, 1].
	 */
	public CustomOffer(ItemStackProducer a, ItemStackProducer c, float prob)
	{
		this(a, null, c, prob);
	}
	
	/**
	 * @return A MerchantRecipe (offer) based on the parameters this CustomOffer was given when it was constructed.
	 */
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
