package com.hotmail.wolfiemario.rebalancevillagers.offers;

import java.util.ArrayList;

/**
 * This class is used to organize all MerchantRecipes which may be created for a specific villager profession.
 * Following a paradigm based on vanilla Minecraft's offer generation system, a villager can have simple buy
 * or sell offers, which only specify the offer's probability value and the item being bought or sold - the
 * prices are determined by a mechanism outside of this class. A villager can also have special offers, which
 * are defined in detail.
 * @author Gerrard Lukacs
 */
public class PotentialOffersList
{
	private ArrayList<SimpleOffer> buyOffers;
	private ArrayList<SimpleOffer> sellOffers;
	private ArrayList<CustomOffer> otherOffers;
	
	/**
	 * @param buys - an ArrayList representing all simple buy offers.
	 * @param sells - an ArrayList representing all simple sell offers.
	 * @param other - an ArrayList representing all special offers.
	 */
	public PotentialOffersList(ArrayList<SimpleOffer> buys, ArrayList<SimpleOffer> sells, ArrayList<CustomOffer> other)
	{
		buyOffers = buys;
		sellOffers = sells;
		otherOffers = other;
	}
	
	/**
	 * @return An ArrayList representing all simple buy offers in this list.
	 */
	public ArrayList<SimpleOffer> getBuys()
	{
		return buyOffers;
	}
	
	/**
	 * @return An ArrayList representing all simple sell offers in this list.
	 */
	public ArrayList<SimpleOffer> getSells()
	{
		return sellOffers;
	}
	
	/**
	 * @return An ArrayList representing all special offers in this list.
	 */
	public ArrayList<CustomOffer> getOther()
	{
		return otherOffers;
	}
}
