package com.hotmail.wolfiemario.rebalancevillagers.offers;

/**
 * @author Gerrard Lukacs
 */
public class SimpleOffer extends AbstractOffer
{
	private int itemId;
	
	public SimpleOffer(int id, float prob)
	{
		super(prob);
		itemId = id;
	}
	
	public int getId()
	{
		return itemId;
	}
	
}
