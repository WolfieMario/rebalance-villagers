package com.hotmail.wolfiemario.rebalancevillagers.offers;

import net.minecraft.server.v1_7_R2.Item;

/**
 * A SimpleOffer, like an AbstractOffer, represents a merchant offer's probability value. In addition, it represents
 * an item or block ID which is involved in this offer.
 * @author Gerrard Lukacs
 */
public class SimpleOffer extends AbstractOffer
{
	private Item item;
	
	/**
	 * Constructs a new SimpleOffer with the specified id and probability value.
	 * @param id - the item or block id this SimpleOffer references.
	 * @param prob - the probability value of this offer. Represents the chance of this offer being
	 * considered for addition to a villager's offer list. Valid range is [0, 1].
	 */
	public SimpleOffer(Item _item, float prob)
	{
		super(prob);
		item = _item;
	}
	
	/**
	 * @return The item or block id referenced by this SimpleOffer.
	 */
	public Item getItem()
	{
		return item;
	}
	
}
