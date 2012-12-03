package com.hotmail.wolfiemario.rebalancevillagers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.hotmail.wolfiemario.rebalancevillagers.offers.AbstractOffer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.CustomOffer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.PotentialOffersList;
import com.hotmail.wolfiemario.rebalancevillagers.offers.SimpleOffer;

import net.minecraft.server.Block;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.IMerchant;
import net.minecraft.server.IMonster;
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
 * A custom extension of the EntityVillager class, for custom properties and offer generation.
 * @author Gerrard Lukacs
 * @author Mojang staff (likely Jeb): a lot of this class is copied from EntityVillager.
 */
public class BalancedVillager extends EntityVillager
	implements NPC, IMerchant
{
	
	/**
	 * @param world - the World for this BalancedVillager
	 */
	public BalancedVillager(World world)
	{
		this(world, 0);
	}
	
	/**
	 * @param world - the World for this BalancedVillager
	 * @param p - this BalancedVillager's profession
	 */
	public BalancedVillager(World world, int k)
	{
		super(world, k);
		
		profession = 0;
		f = false;
		g = false;
		village = null;
		setProfession(k);
		texture = "/mob/villager/villager.png";
		bG = 0.5F;
		getNavigation().b(true);
		getNavigation().a(true);
//        goalSelector.a(0, new PathfinderGoalFloat(this));
//        goalSelector.a(1, new PathfinderGoalAvoidPlayer(this, net/minecraft/server/EntityZombie, 8F, 0.3F, 0.35F));
//        goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
//        goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
//        goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
//        goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
//        goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
//        goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.3F));
//        goalSelector.a(6, new PathfinderGoalMakeLove(this));
//        goalSelector.a(7, new PathfinderGoalTakeFlower(this));
//        goalSelector.a(8, new PathfinderGoalPlay(this, 0.32F));
//        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/EntityHuman, 3F, 1.0F));
//        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/EntityVillager, 5F, 0.02F));
//        goalSelector.a(9, new PathfinderGoalRandomStroll(this, 0.3F));
//        goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, net/minecraft/server/EntityLiving, 8F));
	}
	
	/**
	 * (NMS) EntityVillager method: isAIEnabled(): use new AI
	 */
	public boolean be()
	{
		return true;
	}
	
	/**
	 * (NMS) EntityVillager method: updateAITick()
	 */
	protected void bm()
	{
		if(--profession <= 0)     //standard behavior
		{
			world.villages.a(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ));
			profession = 70 + random.nextInt(50);
			village = world.villages.getClosestVillage(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ), 32);
			if(village == null)
			{
				aL(); //detatchHome
			} else
			{
				ChunkCoordinates chunkcoordinates = village.getCenter();
				b(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z, (int)((float)village.getSize() * 0.6F));
                if(bL)
                {
                    bL = false;
                    village.b(5);
                }
			}
		}
		if(!p() && j > 0) // trading related behavior - p == isTrading, j == timeUntilReset
		{
			j--;
			if(j <= 0) // timeUntilReset
			{
				if(bI) // bI == needsInitilization - were we adding a new offer?
				{
                    if(i.size() > 1 && offerRemoval)
                    {
                        Iterator<?> iterator = i.iterator();
                        do
                        {
                            if(!iterator.hasNext())
                                break;
                            MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();
                            if(merchantrecipe.g()) {
                                int firstDice = (removalMaximum - removalMinimum)/2 + 1;
                                int secondDice = removalMaximum - removalMinimum - firstDice + 2;
                                
                                //Insurance if either value came out invalid.
                                firstDice = (firstDice < 1) ? 1 : firstDice;
                                secondDice = (secondDice < 1) ? 1 : secondDice;
		          
                                merchantrecipe.a( random.nextInt(firstDice) + random.nextInt(secondDice) + removalMinimum);
                            }
                            
                        } while(true);
                        j = removalTicks;
                    }
                    if(village != null && bK != null)
                    {
                        world.broadcastEntityEffect(this, (byte)14);
                        village.a(bK, 1);
                    }
                    generateNewOffers(newOfferCount); //Add new offer(s)
                    bI = false;
				}
				addEffect(new MobEffect(MobEffectList.REGENERATION.id, particleTicks, 0));  // addEffect(new MobEffect(MobEffectList.REGENERATION.id, 200, 0));
			}
		}
		super.bm();
	}
	
	/**
	 * (NMS) EntityVillager method: interact: Attempt to trade with entityhuman
	 */
	public boolean a(EntityHuman entityhuman)
	{
        ItemStack itemstack = entityhuman.inventory.getItemInHand();
        boolean flag = itemstack != null && itemstack.id == Item.MONSTER_EGG.id;
		if(!flag && isAlive() && (!p() || allowMultivending) && (!isBaby() || canTradeChildren)) //alive, adult, and nobody else is trading
		{
			if(!world.isStatic)
			{
				b_(entityhuman);
				entityhuman.openTrade(this);
			}
			return true;
		} else
		{
			return super.a(entityhuman);
		}
	}
	
	/**
	 * (NMS) EntityVillager method: entityInit()
	 */
	protected void a()
	{
		super.a();
	}
	
	/**
	 * @return Max health of a BalancedVillager.
	 */
	public int getMaxHealth()
	{
		return maxHealth;
	}
	
	/**
	 * (NMS) EntityVillager method: stores this villager's NBT data.
	 */
	public void b(NBTTagCompound nbttagcompound)
	{
		super.b(nbttagcompound);
		nbttagcompound.setInt("Profession", getProfession());
		nbttagcompound.setInt("Riches", bJ);
		if(i != null)
			nbttagcompound.setCompound("Offers", i.a());
	}
	
	/**
	 * (NMS) EntityVillager method: loads this villager's NBT data.
	 */
	public void a(NBTTagCompound nbttagcompound)
	{
		super.a(nbttagcompound);
		setProfession(nbttagcompound.getInt("Profession"));
		bJ = nbttagcompound.getInt("Riches");
		if(nbttagcompound.hasKey("Offers"))
		{
			NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Offers");
			i = new MerchantRecipeList(nbttagcompound1);
		}
	}
	
	/**
	 * (NMS) EntityVillager method: canDespawn()
	 */
	protected boolean bj()
	{
		return false;
	}
	
	/**
	 * (NMS) EntityVillager method: idle sound string
	 */
	protected String aY()
	{
		return "mob.villager.default";
	}
	
	/**
	 * (NMS) EntityVillager method: hurt sound string
	 */
	protected String aZ()
	{
		return "mob.villager.defaulthurt";
	}
	
	/**
	 * (NMS) EntityVillager method: death sound string
	 */
	protected String ba()
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
	
	/**
	 * (NMS) EntityVillager method: isMating()
	 */
	public boolean n()
	{
		return f;
	}
	
	/**
	 * (NMS) EntityVillager method: set IsMating()
	 */
	public void f(boolean flag)
	{
		f = flag;
	}
	
	/**
	 * (NMS) EntityVillager method: set IsPlaying()
	 */
	public void g(boolean flag)
	{
		g = flag;
	}
	
	/**
	 * (NMS) EntityVillager method: isPlaying()
	 */
	public boolean o()
	{
		return g;
	}
	
	/**
	 * (NMS) EntityVillager method: setRevengeTarget()
	 */
	public void c(EntityLiving entityliving)
	{
		super.c(entityliving);
		if(village != null && entityliving != null)
		{
			village.a(entityliving); //enemy of the state
            if(entityliving instanceof EntityHuman)
            {
                byte byte0 = -1;
                if(isBaby())
                    byte0 = -3;
                village.a(((EntityHuman)entityliving).getName(), byte0);
                if(isAlive())
                    world.broadcastEntityEffect(this, (byte)13);
            }
        }
    }

    public void die(DamageSource damagesource)
    {
        if(village != null)
        {
            Entity entity = damagesource.getEntity();
            if(entity != null)
            {
                if(entity instanceof EntityHuman)
                    village.a(((EntityHuman)entity).getName(), -2);
                else
                if(entity instanceof IMonster)
                    village.h();
            } else
            if(entity == null)
            {
                EntityHuman entityhuman = world.findNearbyPlayer(this, 16D);
                if(entityhuman != null)
                    village.h();
            }
        }
        super.die(damagesource);
	}
	
	/**
	 * (NMS) EntityVillager method: Binds a player to this Villager
	 */
	public void b_(EntityHuman entityhuman)
	{
		h = entityhuman;
	}
	
	/**
	 * (NMS) EntityVillager method: Returns the player bound to this Villager
	 */
	public EntityHuman m_()
	{
		return h;
	}
	
	/**
	 * (NMS) EntityVillager method: Is a player bound to this Villager?
	 */
	public boolean p()
	{
		return h != null;
	}
	
	/**
	 * (NMS) EntityVillager method: Offer addition and removal, and riches count, called when a trade is made.
	 */
	public void a(MerchantRecipe merchantrecipe)
	{
		merchantrecipe.f(); //increments offer uses
		if( (merchantrecipe.a( (MerchantRecipe)i.get(i.size() - 1) ) || newForAnyTrade) && (random.nextInt(100) < newProbability) ) //Does this offer equal the last offer on the list?
		{
			j = generationTicks; //set offer update ticks to n
			bI = true;
            if(h != null)
                bK = h.getName();
            else
                bK = null;
		}
		if(merchantrecipe.getBuyItem1().id == currencyId)
			bJ += merchantrecipe.getBuyItem1().count; //increment riches by amount of currency item.
	}
	
	/**
	 * (NMS) EntityVillager method: Gives offers, generating one if none exist.
	 */
	public MerchantRecipeList getOffers(EntityHuman entityhuman)
	{
		if(i == null)
			generateNewOffers(defaultOfferCount);    // t
		return i;
	}
	
//    private float j(float f1)
//    {
//        float f2 = f1 + bM;
//        if(f2 > 0.9F)
//            return 0.9F - (f2 - 0.9F);
//        else
//            return f2;
//    }
	
	/**
	 * Attempts to generate the specified number of offers. Limited by the amount of unique offers this villager can actually generate.
	 * @param numOffers - the number of offers to try generating
	 */
	@SuppressWarnings("unchecked")
	private void generateNewOffers(int numOffers)  // t()
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
		for(int l = 0; l < numOffers && l < merchantrecipelist.size(); l++)
			i.a((MerchantRecipe)merchantrecipelist.get(l));
		
	}
	
	/**
	 * Determines whether or not the specified AbstractOffer is considered for addition to a villager.
	 * @param offer - the AbstractOffer to check the probability value of
	 * @param random
	 * @return Whether or not this offer should occur.
	 */
	private static boolean offerOccurs(AbstractOffer offer, Random random)
	{
		return random.nextFloat() < offer.getProbability();
	}
	
	/**
	 * Creates the MerchantRecipe for a block or item ID, based on the specified map of offer values. Applies the Smart Stacking feature.
	 * @param id - the item or block ID involved in this offer
	 * @param valuesMap - the map defining the price for the offer
	 * @param random
	 * @return The MerchantRecipe built based on the parameters.
	 */
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
	
	/**
	 * Determines the value of an item or block, correcting for incorrect declarations.
	 * @param id - the item or block ID involved in this offer
	 * @param valuesMap - the map defining the price for the offer
	 * @param random
	 * @return 1 if the value could not be found, a positive number if the value represents the amount in the input slots of a MerchantRecipe,
	 * or a negative number if the value represents the negation of the amount in the output slot of a MerchantRecipe. 
	 */
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
	
	/**
	 * @param id - the id of the item or block to check
	 * @return Whether or not the Smart Compression feature can compress this item or block.
	 */
	private static boolean isCompressible(int id)
	{
		return compressedForms.containsKey(id);
	}
	
	/**
	 * (NMS) EntityVillager method: Adds a buy offer to the given list.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Deprecated
	private static void a(MerchantRecipeList merchantrecipelist, int id, Random random, float probabilityValue)
	{
		if(random.nextFloat() < probabilityValue)
			merchantrecipelist.add(new MerchantRecipe(a(id, random), Item.EMERALD));
	}
	
	/**
	 * (NMS) EntityVillager method: Creates a buy offer's ItemStack
	 */
	@Deprecated
	private static ItemStack a(int id, Random random)
	{
		return new ItemStack(id, b(id, random), 0);
	}
	
	/**
	 * (NMS) EntityVillager method: Determines the value of a buy offer, correcting for incorrect declarations.
	 */
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
	
	/**
	 * (NMS) EntityVillager method: Adds a sell offer to the given list.
	 */
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
	
	/**
	 * (NMS) EntityVillager method: Determines the value of a sale offer, correcting for incorrect declarations.
	 */
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
	
	
    private int profession;
    private boolean f;
    private boolean g;
    Village village;
    private EntityHuman h;
    private MerchantRecipeList i;
    private int j;
    private boolean bI;
    private int bJ;
    private String bK;
    private boolean bL;
//    private float bM;
    private static HashMap<Integer, Tuple> buyValues = new HashMap<Integer, Tuple>(); // bN 
    private static HashMap<Integer, Tuple> sellValues = new HashMap<Integer, Tuple>(); // bO
    
    
    // ADD START
    /**
     * Constructs a new BalacedVillager which is identical to the (NMS) EntityVillager vil.
     * Of course, the unique ID will not be the same, and the position still needs to be set.
     * @param vil - the (NMS) EntityVillager to clone as a BalancedVillager
     */
    public BalancedVillager(EntityVillager vil)
    {
        super(vil.world, vil.getProfession());
        
        NBTTagCompound dummyCompound = new NBTTagCompound();
        vil.b(dummyCompound); //Stores the villager data in the compound
        a(dummyCompound); //Retrieves that data in this object.
    }
    
    private static HashMap<Integer, Integer> compressedForms;  // private static final Map 
    
    private static int currencyId = Item.EMERALD.id;
    private static HashMap<Integer, PotentialOffersList> offersByProfession = new HashMap<Integer, PotentialOffersList>();

    
    private static boolean offerRemoval = true;
    private static int removalMinimum = 2;
    private static int removalMaximum = 13;
    private static int removalTicks = 20;
    
    private static int defaultOfferCount = 1;
    private static int newOfferCount = 1;
    private static int generationTicks = 40;
    private static boolean newForAnyTrade = false;
    private static int newProbability = 100;
    
    
    private static int particleTicks = 200;
    private static boolean allowMultivending = false;
    private static boolean canTradeChildren = false;
    
    private static int maxHealth = 20;
    
    static
    {        
        // MOD START
        // removed original put's
        //Sadly, can't include lapis because it would be considered equal to all dyes.
        compressedForms = new HashMap<Integer, Integer>();
        compressedForms.put(Item.EMERALD.id, Block.EMERALD_BLOCK.id);
        compressedForms.put(Item.GOLD_INGOT.id, Block.GOLD_BLOCK.id);
        compressedForms.put(Item.GOLD_NUGGET.id, Item.GOLD_INGOT.id);
        compressedForms.put(Item.DIAMOND.id, Block.DIAMOND_BLOCK.id);
        compressedForms.put(Item.IRON_INGOT.id, Block.IRON_BLOCK.id);
        // MOD END
    }
    
    
    /*
     * The following setters change properties for all BalancedVillagers.
     * These are invoked when loading the config.
     */
    public static void setOfferRemoval(boolean remove)      {   offerRemoval = remove;      }
    public static void setOfferRemovalRange(int min, int max)
    {
        removalMinimum = min;
        removalMaximum = max;
    }
    public static void setRemovalTicks(int ticks)           {   removalTicks = ticks;       }

    public static void setDefaultOfferCount(int count)      {   defaultOfferCount = count;  }
    public static void setNewOfferCount(int count)          {   newOfferCount = count;      }
    public static void setGenerationTicks(int ticks)        {   generationTicks = ticks;    }
    public static void setForAnyTrade(boolean allow)        {   newForAnyTrade = allow;     }
    public static void setNewProbability(int prob)          {   newProbability = prob;      }

    public static void setParticleTicks(int ticks)          {   particleTicks = ticks;      }
    public static void setAllowMultivending(boolean allow)  {   allowMultivending = allow;  }
    public static void setCanTradeChildren(boolean allow)   {   canTradeChildren = allow;   }
    
    public static void setMaxHealth(int health)             {   maxHealth = health;         }
    
    public static void setCurrencyItem(int id)              {   currencyId = id;            }
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
    
    /**
     * Populates a MerchantRecipeList with offers from a PotentialOffersList, based on their probability values.
     * @param merchantrecipelist - the list to populate
     * @param offers - the potential offers to populate it with
     * @param random - I never really understood the reasons for this model of passing Random...
     */
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
    
    // ADD END
}
