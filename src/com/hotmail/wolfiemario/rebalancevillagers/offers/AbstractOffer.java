package com.hotmail.wolfiemario.rebalancevillagers.offers;

/**
 * @author Gerrard Lukacs
 */
public abstract class AbstractOffer
{
	protected float probability;
	
	AbstractOffer(float prob)
	{
		probability = prob;
	}
	
	public float getProbability()
	{
		return probability;
	}
}
