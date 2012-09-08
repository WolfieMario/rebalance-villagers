package com.hotmail.wolfiemario.rebalancevillagers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import com.hotmail.wolfiemario.rebalancevillagers.offers.AbstractOffer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.CustomOffer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.PotentialOffersList;
import com.hotmail.wolfiemario.rebalancevillagers.offers.SimpleOffer;

import net.minecraft.server.Block;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.IMerchant;
import net.minecraft.server.Item;
import net.minecraft.server.ItemStack;
import net.minecraft.server.MathHelper;
import net.minecraft.server.MerchantRecipe;
import net.minecraft.server.MerchantRecipeList;
import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NPC;
import net.minecraft.server.Tuple;
import net.minecraft.server.Village;
import net.minecraft.server.World;

/**
 * @author Gerrard Lukacs
 */
public class BalancedVillager extends EntityVillager implements NPC, IMerchant
{
	private static int currencyId = Item.EMERALD.id;
	private static HashMap<Integer, PotentialOffersList> offersByProfession = new HashMap<Integer, PotentialOffersList>();
	private static HashMap<Integer, Tuple> buyValues = new HashMap<Integer, Tuple>();
	private static HashMap<Integer, Tuple> sellValues = new HashMap<Integer, Tuple>();
	private static HashMap<Integer, Integer> compressedForms;
	static
	{
		compressedForms = new HashMap<Integer, Integer>();
		compressedForms.put(Item.EMERALD.id, Block.EMERALD_BLOCK.id);
		compressedForms.put(Item.GOLD_INGOT.id, Block.GOLD_BLOCK.id);
		compressedForms.put(Item.GOLD_NUGGET.id, Item.GOLD_INGOT.id);
		compressedForms.put(Item.DIAMOND.id, Block.DIAMOND_BLOCK.id);
		compressedForms.put(Item.IRON_INGOT.id, Block.IRON_BLOCK.id);
		//Sadly, can't include lapis because it would be considered equal to all dyes.
	}
	
	private static boolean offerRemoval = true;
	private static int removalMinimum = 3;
	private static int removalMaximum = 13;
	private static int removalTicks = 20;
	
	private static int defaultOfferCount = 1;
	private static int newOfferCount = 1;
	private static int generationTicks = 60;
	private static boolean newForAnyTrade = false;
	private static int newProbability = 100;
	
	
	private static int particleTicks = 200;
	private static boolean allowMultivending = false;
	private static boolean canTradeChildren = false;
	
	private static int maxHealth = 20;
	
	private int profession;
	private boolean f;
	private boolean g;
	Village village;
	private EntityHuman h;
	private MerchantRecipeList i;
	private int j;
	private boolean by;
	private int bz;
	private MerchantRecipe bA;
	
	public BalancedVillager(World world)
	{
		this(world, 0);
	}
	
	public BalancedVillager(World world, int k)
	{
		super(world, k);
		
		profession = 0;
		f = false;
		g = false;
		village = null;
		setProfession(k);
		texture = "/mob/villager/villager.png";
		bw = 0.5F;
		getNavigation().b(true);
		getNavigation().a(true);
	}
	
	public BalancedVillager(EntityVillager vil)
	{
		super(vil.world, vil.getProfession());
		
		NBTTagCompound dummyCompound = new NBTTagCompound();
		vil.b(dummyCompound); //Stores the villager data in the compound
		a(dummyCompound); //Retrieves that data in this object.
	}
	
	public static void setOfferRemoval(boolean remove)		{	offerRemoval = remove;		}
	public static void setOfferRemovalRange(int min, int max)
	{
		removalMinimum = min;
		removalMaximum = max;
	}
	public static void setRemovalTicks(int ticks)			{	removalTicks = ticks;		}

	public static void setDefaultOfferCount(int count)		{	defaultOfferCount = count;	}
	public static void setNewOfferCount(int count)			{	newOfferCount = count;		}
	public static void setGenerationTicks(int ticks)		{	generationTicks = ticks;	}
	public static void setForAnyTrade(boolean allow)		{	newForAnyTrade = allow;		}
	public static void setNewProbability(int prob)			{	newProbability = prob;		}

	public static void setParticleTicks(int ticks)			{	particleTicks = ticks;		}
	public static void setAllowMultivending(boolean allow)	{	allowMultivending = allow;	}
	public static void setCanTradeChildren(boolean allow)	{	canTradeChildren = allow;	}
	
	public static void setMaxHealth(int health)				{	maxHealth = health;			}
	
	public static void setCurrencyItem(int id)				{	currencyId = id; 			}
	public static void setOffersByProfession(HashMap<Integer, PotentialOffersList> offers)
	{
		offersByProfession = offers;
	}
	public static void setBuyValues(HashMap<Integer, Tuple> buys)
	{
		buyValues = buys;
	}
	public static void setSellValues(HashMap<Integer, Tuple> sells)
	{
		sellValues = sells;
	}
	
	//isAIEnabled(): use new AI
	public boolean aV()
	{
		return true;
	}

	//updateAITick()
	protected void bd()
	{
		//standard behavior
		if(--profession <= 0)
		{
			world.villages.a(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ));
			profession = 70 + random.nextInt(50);
			village = world.villages.getClosestVillage(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ), 32);
			if(village == null)
			{
				aE(); //detatchHome
			} else
			{
				ChunkCoordinates chunkcoordinates = village.getCenter();
				b(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z, village.getSize());
			}
		}
		
		//trading related behavior
		if(!q() && j > 0) //player is not bound to villager, and j>0 (is n when trade is made)
		{
			j--;
			if(j <= 0) //ooh, those ticks are up!
			{
				if(by) //were we adding a new offer?
				{
					c(newOfferCount); //Add new offer(s)
					by = false;
				}
				if(bA != null)
				{
					i.remove(bA);
					bA = null;
				}
				addEffect(new MobEffect(MobEffectList.REGENERATION.id, particleTicks, 0));
			}
		}
		super.bd();
	}

	//interact: Attempt to trade
	public boolean c(EntityHuman entityhuman)
	{
		if(isAlive() && (!q() || allowMultivending) && (!isBaby() || canTradeChildren)) //alive, adult, and nobody else is trading
		{
			if(!world.isStatic)
			{
				a_(entityhuman);
				entityhuman.openTrade(this);
			}
			return true;
		} else
		{
			return super.c(entityhuman);
		}
	}

	//entityInit()
	protected void a()
	{
		super.a();
	}

	//Gets max health
	public int getMaxHealth()
	{
		return maxHealth;
	}

	//Store NBT Data
	public void b(NBTTagCompound nbttagcompound)
	{
		super.b(nbttagcompound);
		nbttagcompound.setInt("Profession", getProfession());
		nbttagcompound.setInt("Riches", bz);
		if(i != null)
			nbttagcompound.setCompound("Offers", i.a());
	}

	//Load NBT Data
	public void a(NBTTagCompound nbttagcompound)
	{
		super.a(nbttagcompound);
		setProfession(nbttagcompound.getInt("Profession"));
		bz = nbttagcompound.getInt("Riches");
		if(nbttagcompound.hasKey("Offers"))
		{
			NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Offers");
			i = new MerchantRecipeList(nbttagcompound1);
		}
	}

	//canDespawn()
	protected boolean ba()
	{
		return false;
	}

	//idle sound
	protected String aQ()
	{
		return "mob.villager.default";
	}

	//hurt sound
	protected String aR()
	{
		return "mob.villager.defaulthurt";
	}

	//death sound
	protected String aS()
	{
		return "mob.villager.defaultdeath";
	}

	public void setProfession(int k)
	{
		datawatcher.watch(16, Integer.valueOf(k));
	}

	public int getProfession()
	{
		return datawatcher.getInt(16);
	}

	//isMating
	public boolean o()
	{
		return f;
	}

	//set isMating
	public void e(boolean flag)
	{
		f = flag;
	}

	//set isPlaying
	public void f(boolean flag)
	{
		g = flag;
	}

	//isPlaying
	public boolean p()
	{
		return g;
	}

	//setRevengeTarget
	public void c(EntityLiving entityliving)
	{
		super.c(entityliving);
		if(village != null && entityliving != null)
			village.a(entityliving); //enemy of the state
	}

	//Binds a player to this Villager
	public void a_(EntityHuman entityhuman)
	{
		h = entityhuman;
	}

	//Returns the player bound to this Villager
	public EntityHuman l_()
	{
		return h;
	}

	//Is a player bound to this Villager?
	public boolean q()
	{
		return h != null;
	}

	//Offer addition and removal, and riches count, called when a trade is made.
	public void a(MerchantRecipe merchantrecipe)
	{
		merchantrecipe.f(); //increments offer uses
		if( (merchantrecipe.a( (MerchantRecipe)i.get(i.size() - 1) ) || newForAnyTrade) && (random.nextInt(100) < newProbability) ) //Does this offer equal the last offer on the list?
		{
			j = generationTicks; //set offer update ticks to n
			by = true;
		}
		else if(i.size() > 1 && offerRemoval)
		{
			int firstDice = (removalMaximum - removalMinimum)/2 + 1;
			int secondDice = removalMaximum - removalMinimum - firstDice + 2;
			
			//Insurance if either value came out invalid.
			firstDice = (firstDice < 1) ? 1 : firstDice;
			secondDice = (secondDice < 1) ? 1 : secondDice;
			
			int k = random.nextInt(firstDice) + random.nextInt(secondDice) + removalMinimum;
			if(k <= merchantrecipe.getUses())
			{
				j = removalTicks;
				bA = merchantrecipe;
			}
		}
		if(merchantrecipe.getBuyItem1().id == currencyId)
			bz += merchantrecipe.getBuyItem1().count; //increment riches by amount of currency item.
	}

	//Gives offers, generating one if none exist.
	public MerchantRecipeList getOffers(EntityHuman entityhuman)
	{
		if(i == null)
			c(defaultOfferCount);
		return i;
	}
	
	//Generate new offers
	@SuppressWarnings("unchecked")
	private void c(int k)
	{
		MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
		
		PotentialOffersList offers = offersByProfession.get(getProfession());
		
		if(offers != null)
			populateMerchantRecipeList(merchantrecipelist, offers, random);
		
		if(merchantrecipelist.isEmpty() && offersByProfession.containsKey(-1))
			populateMerchantRecipeList(merchantrecipelist, offersByProfession.get(-1), random); //Attempt loading user-specified defaults.
		
		if(merchantrecipelist.isEmpty())
			merchantrecipelist.add(getOffer(Item.GOLD_INGOT.id, buyValues, random)); //If all else fails...
		
		Collections.shuffle(merchantrecipelist);
		if(i == null)
			i = new MerchantRecipeList();
		for(int l = 0; l < k && l < merchantrecipelist.size(); l++)
			i.a((MerchantRecipe)merchantrecipelist.get(l));
		
	}
	
	private static boolean offerOccurs(AbstractOffer offer, Random random)
	{
		return random.nextFloat() < offer.getProbability();
	}
	
	@SuppressWarnings("unchecked")
	private static void populateMerchantRecipeList(MerchantRecipeList merchantrecipelist, PotentialOffersList offers, Random random)
	{
		for(SimpleOffer buy: offers.getBuys())
		{
			if(offerOccurs(buy, random))
				merchantrecipelist.add(getOffer(buy.getId(), buyValues, random));
		}
		
		for(SimpleOffer sell: offers.getSells())
		{
			if(offerOccurs(sell, random))
				merchantrecipelist.add(getOffer(sell.getId(), sellValues, random));
		}
		
		for(CustomOffer other: offers.getOther())
		{
			if(offerOccurs(other, random))
				merchantrecipelist.add(other.getOffer());
		}
	}
	
	//Creates the MerchantRecipe of a SimpleOffer
	private static MerchantRecipe getOffer(int id, HashMap<Integer, Tuple> valuesMap, Random random)
	{
		int value = offerValue(id, valuesMap, random);
		
		//Don't allow zero of an item!
		if(value == 0)
			value = 1;
		
		boolean buy = valuesMap == buyValues;
		
		ItemStack buyA;
		ItemStack buyB = null;
		ItemStack sell;
		
		//Depending on whether we're buying or selling, the input and output are swapped.
		int input = buy ? id : currencyId;
		int output = buy ? currencyId : id;
		
		if(value < 0)
		{
			buyA = new ItemStack(input, 1, 0);
			sell = new ItemStack(output, -value, 0);
		}
		else
		{
			if(value <= 64)
			{
				buyA = new ItemStack(input, value, 0);
				sell = new ItemStack(output, 1, 0);
			}
			else if(value <= 128 || !isCompressible(input)) //if the input can't be compressed, this is the end of the line.
			{
				buyA = new ItemStack(input, 64, 0);
				buyB = new ItemStack(input, value - 64, 0);
				sell = new ItemStack(output, 1, 0);
			}
			else
			{
				//=IF(P77<= 128; IF(O77=P77;O77;O77 & "-" & P77) & IF(AND(H77<1; L77>1;P77>1);" per Emerald"; IF(P77=1;" Emerald";" Emeralds"));
				//ROUNDDOWN(ROUND(O77)/9) & " Emerald Blocks & " & MOD(ROUND(O77);9) & "-" & MOD(ROUND(O77);9)+P77-O77 & " Emeralds" )
				int numCompressed = (int) Math.floor(value/9.0);
				numCompressed = Math.min(numCompressed, 64); //If we cap the blocks at 64, we guarantee amounts up to 640 are tradeable with this mechanic.
				int numUncompressed = value - (numCompressed * 9);
				buyA = new ItemStack(compressedForms.get(input), numCompressed, 0);
				buyB = new ItemStack(input, numUncompressed, 0);
				sell = new ItemStack(output, 1, 0);
			}
			
		}
		
		if(buyB == null)
			return new MerchantRecipe(buyA, sell);
		else
			return new MerchantRecipe(buyA, buyB, sell);
	}
	
	//Determines the value of an offer, correcting for incorrect declarations.
	private static int offerValue(int id, HashMap<Integer, Tuple> valuesMap, Random random)
	{
		Tuple tuple = (Tuple)valuesMap.get(Integer.valueOf(id));
		if(tuple == null)
			return 1;
		if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
			return ((Integer)tuple.a()).intValue();
		else
			return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
	}
	
	private static boolean isCompressible(int id)
	{
		return compressedForms.containsKey(id);
	}
	
	//Adds a buy offer to the given list.
	@SuppressWarnings({ "unchecked", "unused" })
	@Deprecated
	private static void a(MerchantRecipeList merchantrecipelist, int id, Random random, float probabilityValue)
	{
		if(random.nextFloat() < probabilityValue)
			merchantrecipelist.add(new MerchantRecipe(a(id, random), Item.EMERALD));
	}
	
	//Creates a buy offer's ItemStack
	@Deprecated
	private static ItemStack a(int id, Random random)
	{
		return new ItemStack(id, b(id, random), 0);
	}
	
	//Determines the value of a buy offer, correcting for incorrect declarations.
	@Deprecated
	private static int b(int id, Random random)
	{
		Tuple tuple = (Tuple)buyValues.get(Integer.valueOf(id));
		if(tuple == null)
			return 1;
		if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
			return ((Integer)tuple.a()).intValue();
		else
			return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
	}
	
	//Adds a sell offer to the given list.
	@SuppressWarnings({ "unchecked", "unused" })
	@Deprecated
	private static void b(MerchantRecipeList merchantrecipelist, int id, Random random, float probabilityValue)
	{
		if(random.nextFloat() < probabilityValue)
		{
			int value = c(id, random);
			ItemStack buyA;
			ItemStack buyB = null;
			ItemStack sell;
			if(value < 0)
			{
				buyA = new ItemStack(Item.EMERALD.id, 1, 0);
				sell = new ItemStack(id, -value, 0);
			}
			else
			{
				if(value <= 64)
				{
					buyA = new ItemStack(Item.EMERALD.id, value, 0);
					sell = new ItemStack(id, 1, 0);
				}
				else if(value <= 128)
				{
					buyA = new ItemStack(Item.EMERALD.id, 64, 0);
					buyB = new ItemStack(Item.EMERALD.id, value - 64, 0);
					sell = new ItemStack(id, 1, 0);
				}
				else
				{
					//=IF(P77<= 128; IF(O77=P77;O77;O77 & "-" & P77) & IF(AND(H77<1; L77>1;P77>1);" per Emerald"; IF(P77=1;" Emerald";" Emeralds"));
					//ROUNDDOWN(ROUND(O77)/9) & " Emerald Blocks & " & MOD(ROUND(O77);9) & "-" & MOD(ROUND(O77);9)+P77-O77 & " Emeralds" )
					int blocks = (int) Math.floor(value/9.0);
					int emeralds = value - (blocks * 9);
					buyA = new ItemStack(Block.EMERALD_BLOCK.id, blocks, 0);
					buyB = new ItemStack(Item.EMERALD.id, emeralds, 0);
					sell = new ItemStack(id, 1, 0);
				}
				
			}
			if(buyB == null)
				merchantrecipelist.add(new MerchantRecipe(buyA, sell));
			else
				merchantrecipelist.add(new MerchantRecipe(buyA, buyB, sell));
		}
	}
	
	//Determines the value of a sale offer, correcting for incorrect declarations.
	@Deprecated
	private static int c(int id, Random random)
	{
		Tuple tuple = (Tuple)sellValues.get(Integer.valueOf(id));
		if(tuple == null)
			return 1;
		if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
			return ((Integer)tuple.a()).intValue();
		else
			return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
	}

}
