package com.hotmail.wolfiemario.rebalancevillagers.offers;

/**
 * An AbstractOffer represents a merchant trading offer with a probability value. The probability value
 * specifies how likely the offer is to be considered for addition to a villager's offer list.
 * Note that AbstractOffer is abstract, and only specifies the probability value - no other detail of
 * the offer in question is represented by this class.
 * @author Gerrard Lukacs
 */
public abstract class AbstractOffer
{
	protected float probability;
	
	/**
	 * Constructs a new AbstractOffer with the specified probability value.
	 * @param prob - the probability value of this offer. Represents the chance of this offer being
	 * considered for addition to a villager's offer list. Valid range is [0, 1].
	 */
	public AbstractOffer(float prob)
	{
		probability = prob;
	}
	
	/**
	 * @return The probability value of this offer. Represents the chance of this offer being
	 * considered for addition to a villager's offer list.
	 */
	public float getProbability()
	{
		return probability;
	}
}
