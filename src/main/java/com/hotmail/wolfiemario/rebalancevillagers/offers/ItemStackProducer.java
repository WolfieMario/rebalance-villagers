package com.hotmail.wolfiemario.rebalancevillagers.offers;

import net.minecraft.server.v1_7_R1.ItemStack;

/**
 * An ItemStackProducer is a class capable of creating (NMS) ItemStacks. The exact ItemStack produced may be determined in
 * any way, including randomly, based on the particular implementation.
 * @author Gerrard Lukacs
 */
public interface ItemStackProducer
{
	/**
	 * @return An (NMS) ItemStack based on the parameters this ItemStackProducer was given when it was constructed.
	 * Note that returned ItemStacks will likely vary randomly in some way - this is determined by the particular
	 * implementation.
	 */
	public ItemStack getItemStack();
}
