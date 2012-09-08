package com.hotmail.wolfiemario.rebalancevillagers.offers;

import java.util.ArrayList;

/**
 * @author Gerrard Lukacs
 */
public class PotentialOffersList
{
	private ArrayList<SimpleOffer> buyOffers;
	private ArrayList<SimpleOffer> sellOffers;
	private ArrayList<CustomOffer> otherOffers;
	
	public PotentialOffersList(ArrayList<SimpleOffer> buys, ArrayList<SimpleOffer> sells, ArrayList<CustomOffer> other)
	{
		buyOffers = buys;
		sellOffers = sells;
		otherOffers = other;
	}
	
	public ArrayList<SimpleOffer> getBuys()
	{
		return buyOffers;
	}
	
	public ArrayList<SimpleOffer> getSells()
	{
		return sellOffers;
	}
	
	public ArrayList<CustomOffer> getOther()
	{
		return otherOffers;
	}
}
