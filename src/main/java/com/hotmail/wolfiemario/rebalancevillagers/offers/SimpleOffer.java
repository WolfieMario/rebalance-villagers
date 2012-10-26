package com.hotmail.wolfiemario.rebalancevillagers.offers;

/**
 * A SimpleOffer, like an AbstractOffer, represents a merchant offer's probability value. In addition, it represents
 * an item or block ID which is involved in this offer.
 * @author Gerrard Lukacs
 */
public class SimpleOffer extends AbstractOffer
{
	private int itemId;
	
	/**
	 * Constructs a new SimpleOffer with the specified id and probability value.
	 * @param id - the item or block id this SimpleOffer references.
	 * @param prob - the probability value of this offer. Represents the chance of this offer being
	 * considered for addition to a villager's offer list. Valid range is [0, 1].
	 */
	public SimpleOffer(int id, float prob)
	{
		super(prob);
		itemId = id;
	}
	
	/**
	 * @return The item or block id referenced by this SimpleOffer.
	 */
	public int getId()
	{
		return itemId;
	}
	
}
