package com.hotmail.wolfiemario.rebalancevillagers;

import java.util.*;

import com.hotmail.wolfiemario.rebalancevillagers.offers.AbstractOffer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.CustomOffer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.PotentialOffersList;
import com.hotmail.wolfiemario.rebalancevillagers.offers.SimpleOffer;

import net.minecraft.server.v1_7_R1.*;

public class BalancedVillager extends EntityVillager
    implements IMerchant, NPC
{

    public BalancedVillager(World world)
    {
        this(world, 0);
    }

    public BalancedVillager(World world, int k)
    {
        super(world);
        setProfession(k);
        a(0.6F, 1.8F);
        getNavigation().b(true);
        getNavigation().a(true);

    }

    protected void aD()
    {
        super.aD();
        getAttributeInstance(GenericAttributes.d).setValue(0.5D);
    }

    public boolean bk()
    {
        return true;
    }

    /**
     * (NMS) EntityVillager method: updateAITick()
     */
    @SuppressWarnings("unchecked")
    protected void bp()
    {
        if(--profession <= 0)     //standard behavior
        {
            world.villages.a(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ));
            profession = 70 + random.nextInt(50);
            village = world.villages.getClosestVillage(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ), 32);
            if(village == null)
            {
                bV(); //detatchHome
            } else
            {
                ChunkCoordinates chunkcoordinates = village.getCenter();
                a(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z, (int)((float)village.getSize() * 0.6F));
                if(bz)
                {
                    bz = false;
                    village.b(5);
                }
            }
        }
        
        // check for outdated offers if needed
        if (initialUpdateCheck) findOutdatedOffers();
        
        if(!ca() && offerUpdateTicks > 0) // trading related behavior - bS == isTrading, bv == timeUntilReset
        {
            offerUpdateTicks--;
            if(offerUpdateTicks <= 0) // timeUntilReset
            {
                if(needsInitilization) // == needsInitilization - were we adding a new offer?
                {
                    if(village != null && by != null)
                    {
                        world.broadcastEntityEffect(this, (byte)14);
                        village.a(by, 1);
                    }
                    generateNewOffers(newOfferCount); //Add new offer(s)
                    
                    if(mrList.size() > 1)
                    {
                        ArrayList<MerchantRecipe> toRemove = null;
                        Iterator<?> iterator = mrList.iterator();
                        do
                        {
                            if(!iterator.hasNext())
                                break;
                            MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();
                            if(merchantrecipe.g())
                            {
                                if (offerRemoval)
                                {
                                    if (toRemove == null) toRemove = new ArrayList<MerchantRecipe>();
                                    toRemove.add(merchantrecipe);
                                }
                                else
                                {
                                    // reset maxUses so item is usable again
                                    merchantrecipe.a(maxUses(random));
                                }
                            }
                        } while(true);
                        
                        if (toRemove != null) {
                            // if we would remove all of our recipes, reactivate at least the first one!
                            if (toRemove.size() >= mrList.size()) {
                                boolean firstOne = true;
                                for (MerchantRecipe merchantrecipe : toRemove) {
                                    if (firstOne) {
                                        RebalanceVillagers.debugMsg("Reactivate first one...");
                                        merchantrecipe.a(maxUses(random));
                                        firstOne = false;
                                    } else {
                                        mrList.remove(merchantrecipe);
                                    }
                                }
                            } else {
                                mrList.removeAll(toRemove);
                            }
                        }
                        
                    }

                    needsInitilization = false;

                }
                addEffect(new MobEffect(MobEffectList.REGENERATION.id, particleTicks, 0));
            }
        }
        
        // if we still have no active offer, activate at least one offer so we don't run dry...
        checkForInactiveOffersOnly(false);
        
        super.bp();
    }

    /**
     * (NMS) EntityVillager method: interact: Attempt to trade with entityhuman
     */
    public boolean a(EntityHuman entityhuman)
    {
        ItemStack itemstack = entityhuman.inventory.getItemInHand();
        boolean flag = itemstack != null && itemstack.getItem() == Items.MONSTER_EGG;
        if(!flag && isAlive() && (!ca() || allowMultivending) && (!isBaby() || canTradeChildren)) //alive, adult, and nobody else is trading
        {
            if(!world.isStatic)
            {
                a_(entityhuman);
                entityhuman.openTrade(this, getCustomName());
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
    protected void c()
    {
        super.c();
    }

    /**
     * (NMS) EntityVillager method: stores this villager's NBT data.
     */
    public void b(NBTTagCompound nbttagcompound)
    {
        super.b(nbttagcompound);
        nbttagcompound.setInt("Profession", getProfession());
        nbttagcompound.setInt("Riches", riches);
        if(mrList != null)
            nbttagcompound.set("Offers", mrList.a());
    }

    public void a(NBTTagCompound nbttagcompound)
    {
        super.a(nbttagcompound);
        setProfession(nbttagcompound.getInt("Profession"));
        riches = nbttagcompound.getInt("Riches");
        if(nbttagcompound.hasKeyOfType("Offers", 10))
        {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Offers");
            mrList = new MerchantRecipeList(nbttagcompound1);
        }
    }

    /**
     * (NMS) EntityVillager method: isTypeNotPersistent()
     */
    protected boolean isTypeNotPersistent()
    {
        return false;
    }

    /**
     * (NMS) EntityVillager method: idle sound string
     */
    protected String t()
    {
        if(ca())
            return "mob.villager.haggle";
        else
            return "mob.villager.idle";
    }

    /**
     * (NMS) EntityVillager method: hurt sound string
     */
    protected String aT()
    {
        return "mob.villager.hit";
    }

    /**
     * (NMS) EntityVillager method: death sound string
     */
    protected String aU()
    {
        return "mob.villager.death";
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
    public boolean bY()
    {
        return br;
    }

    /**
     * (NMS) EntityVillager method: set IsMating()
     */
    public void i(boolean flag)
    {
        br = flag;
    }

    /**
     * (NMS) EntityVillager method: set IsPlaying()
     */
    public void j(boolean flag)
    {
        bs = flag;
    }

    /**
     * (NMS) EntityVillager method: isPlaying()
     */
    public boolean bZ()
    {
        return bs;
    }

    /**
     * (NMS) EntityVillager method: setRevengeTarget()
     */
    public void b(EntityLiving entityliving)
    {
        super.b(entityliving);
        if(village != null && entityliving != null)
        {
            village.a(entityliving); //enemy of the state
            if(entityliving instanceof EntityHuman)
            {
                byte byte0 = -1;
                if(isBaby())
                    byte0 = -3;
                village.a(entityliving.getName(), byte0);
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
                    village.a(entity.getName(), -2);
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
    public void a_(EntityHuman entityhuman)
    {
        tradingPlayer = entityhuman;
    }

    /**
     * (NMS) EntityVillager method: Returns the player bound to this Villager
     */
    public EntityHuman b()
    {
        return tradingPlayer;
    }

    /**
     * (NMS) EntityVillager method: Is a player bound to this Villager?
     */
    public boolean ca()
    {
        return tradingPlayer != null;
    }

    /**
     * (NMS) EntityVillager method: Offer addition and removal, and riches count, called when a trade is made.
     */
    public void a(MerchantRecipe merchantrecipe)
    {
        merchantrecipe.f(); //increments offer uses
        a_ = -q();
        makeSound("mob.villager.yes", bf(), bg());
        if( (merchantrecipe.a((MerchantRecipe)mrList.get(mrList.size() - 1)) || newForAnyTrade) && (random.nextInt(100) < newProbability) ) //Does this offer equal the last offer on the list?
        {
            offerUpdateTicks = generationTicks; //set offer update ticks to n
            needsInitilization = true;
            if(tradingPlayer != null)
                by = tradingPlayer.getName();
            else
                by = null;
        }
        if(merchantrecipe.getBuyItem1().getItem() == currencyItem)
            riches += merchantrecipe.getBuyItem1().count; //increment riches by amount of currency item.
    }

    public void a_(ItemStack itemstack)
    {
        if(!world.isStatic && a_ > -q() + 20)
        {
            a_ = -q();
            if(itemstack != null)
                makeSound("mob.villager.yes", bf(), bg());
            else
                makeSound("mob.villager.no", bf(), bg());
        }
    }

    /**
     * (NMS) EntityVillager method: Gives offers, generating one if none exist.
     */
    public MerchantRecipeList getOffers(EntityHuman entityhuman)
    {
        if(mrList == null)
            generateNewOffers(defaultOfferCount);    // t
        return mrList;
    }

    public GroupDataEntity a(GroupDataEntity groupdataentity)
    {
        groupdataentity = super.a(groupdataentity);
        setProfession(world.random.nextInt(5));
        return groupdataentity;
    }

    public void cb()
    {
        bz = true;
    }

    public EntityVillager b(EntityAgeable entityageable)
    {
        EntityVillager entityvillager = new EntityVillager(world);
        entityvillager.a(((GroupDataEntity) (null)));
        return entityvillager;
    }

    public boolean bK()
    {
        return false;
    }

    public EntityAgeable createChild(EntityAgeable entityageable)
    {
        return b(entityageable);
    }

    private int profession;
    private boolean br;
    private boolean bs;
    Village village;
    private EntityHuman tradingPlayer;
    private MerchantRecipeList mrList;
    private int offerUpdateTicks;
    private boolean needsInitilization;
    private int riches;
    private String by;
    private boolean bz;

    private static HashMap<Item, Tuple> buyValues = new HashMap<Item, Tuple>(); // bP 
    private static HashMap<Item, Tuple> sellValues = new HashMap<Item, Tuple>(); // bQ

    // MOD ADDITIONS
    
    /**
     * Attempts to generate the specified number of offers. Limited by the amount of unique offers this villager can actually generate.
     * @param numOffers - the number of offers to try generating
     */
    private void generateNewOffers(int numOffers)
    {
        MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
        
        PotentialOffersList offers = offersByProfession.get(getProfession());
        
        if(offers != null) populateMerchantRecipeList(merchantrecipelist, offers, random);

        findAndRemoveAlreadyActiveRecipes(merchantrecipelist); // remove items which are already in the list (fixes villager running dry)

        Collections.shuffle(merchantrecipelist);
        
        if(mrList == null) {
            mrList = new MerchantRecipeList();
            if (merchantrecipelist.isEmpty()) addDefaultRecipes();
        } else dryrunCheck = dryrunCheckTicks;
        
        for(int l = 0; l < numOffers && l < merchantrecipelist.size(); l++)
            mrList.a((MerchantRecipe)merchantrecipelist.get(l));

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
    private static MerchantRecipe getOffer(Item item, HashMap<Item, Tuple> valuesMap, Random random)
    {

        
        int value = offerValue(item, valuesMap, random);
        
        //Don't allow zero of an item!
        if(value == 0) value = 1;
        
        boolean buy = valuesMap == buyValues;
        
        ItemStack buyA;
        ItemStack buyB = null;
        ItemStack sell;
        
        //Depending on whether we're buying or selling, the input and output are swapped.
        Item input = buy ? item : currencyItem;
        Item output = buy ? currencyItem : item;
        
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
        
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.set("buy", buyA.save(new NBTTagCompound()));
        nbttagcompound.set("sell", sell.save(new NBTTagCompound()));
        if(buyB != null) nbttagcompound.set("buyB", buyB.save(new NBTTagCompound()));
        nbttagcompound.setInt("uses", 0);
        nbttagcompound.setInt("maxUses", maxUses(random));
        
        return new MerchantRecipe(nbttagcompound);
    }
    
    /**
     * Determines the value of an item or block, correcting for incorrect declarations.
     * @param id - the item or block ID involved in this offer
     * @param valuesMap - the map defining the price for the offer
     * @param random
     * @return 1 if the value could not be found, a positive number if the value represents the amount in the input slots of a MerchantRecipe,
     * or a negative number if the value represents the negation of the amount in the output slot of a MerchantRecipe. 
     */
    private static int offerValue(Item item, HashMap<Item, Tuple> valuesMap, Random random)
    {
        Tuple tuple = (Tuple)valuesMap.get(item);
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
    private static boolean isCompressible(Item item)
    {
        return compressedForms.containsKey(item);
    }
    
    
    /**
     * Constructs a new BalacedVillager which is identical to the (NMS) EntityVillager vil.
     * Of course, the unique ID will not be the same, and the position still needs to be set.
     * @param vil - the (NMS) EntityVillager to clone as a BalancedVillager
     */
    public BalancedVillager(EntityVillager vil)
    {
        this(vil, false);
    }
        
    public BalancedVillager(EntityVillager vil, boolean check) {

        super(vil.world, vil.getProfession());
        
        NBTTagCompound dummyCompound = new NBTTagCompound();
        vil.b(dummyCompound); //Stores the villager data in the compound
        a(dummyCompound); //Retrieves that data in this object.
    }
    
    @SuppressWarnings("unchecked")
    private void findOutdatedOffers() {
        if (i == null) return;
        if (mrList == null || mrList.size() == 0) return;

        initialUpdateCheck = false;
        RebalanceVillagers.debugMsg("Checking for outdated offers in villager at: " + this.locX + "," + this.locY + "," + this.locZ);

        Iterator<MerchantRecipe> iIterator = mrList.iterator();
        ArrayList<MerchantRecipe> outdated = null;
        while (iIterator.hasNext()) {
            MerchantRecipe activeReceipe = iIterator.next();
            RebalanceVillagers.debugMsg("-> Checking: " + activeReceipe);

            ItemStack buy1 = activeReceipe.getBuyItem1();
            ItemStack buy2 = activeReceipe.getBuyItem2();
            ItemStack buy3 = activeReceipe.getBuyItem3();
            
            if (!checkOffer(buy1, buy2, buy3)) {
                if (outdated == null) outdated = new ArrayList<MerchantRecipe>();
                outdated.add(activeReceipe);
                RebalanceVillagers.debugMsg("=> Removing outdated offer!");
                
            }
        }
        if (outdated != null) {
            mrList.removeAll(outdated);
            if (mrList.isEmpty()) {
                offerUpdateTicks = generationTicks; //set offer update ticks to n
                needsInitilization = true;
            }
        }
        
    }
    
    private boolean checkOffer(ItemStack buy1, ItemStack buy2, ItemStack buy3) {
        // if he sells things
        if (buy1 != null && (buy1.getItem() == currencyItem || getUncompressed(buy1.getItem()) == currencyItem) && buy3 != null) {

            Tuple tuple = (Tuple)sellValues.get(buy3.getItem());
            if (tuple == null) return false;
            
            int min = ((Integer)tuple.a()).intValue();
            int max = ((Integer)tuple.b()).intValue();

            // check if we have negative prices (so we sell x items for one currency item)
            boolean neg = min < 0;
            min = Math.abs(min);
            max = Math.abs(max);
            
            if (neg) {
                int amount = buy3.count * (isCompressed(buy3.getItem()) ? 9 : 1);

                RebalanceVillagers.debugMsg("--> Sell offer [" + min + " < " + amount + " < " + max + "]");
                if (amount < max || amount > min) return false;
                
            } else {
                int amount = buy1.count * (isCompressed(buy1.getItem()) ? 9 : 1);
                if (buy2 != null) amount = amount + (buy2.count * (isCompressed(buy2.getItem()) ? 9 : 1));
                
                RebalanceVillagers.debugMsg("+-> Sell offer [" + min + " < " + amount + " < " + max + "]");
                if (amount < min || amount > max) return false;
            }

        // if he buys things
        } else if (buy3 != null && (buy3.getItem() == currencyItem || getUncompressed(buy3.getItem()) == currencyItem) && buy1 != null){

            Tuple tuple = (Tuple)buyValues.get(buy1.getItem());
            if (tuple == null) return false;
            
            int min = ((Integer)tuple.a()).intValue();
            int max = ((Integer)tuple.b()).intValue();
            
            // check if we have negative prices (so we buy x items for one currency item)
            boolean neg = min < 0;
            min = Math.abs(min);
            max = Math.abs(max);
            
            if (neg) {
                int price = buy3.count * (isCompressed(buy3.getItem()) ? 9 : 1);
                
                RebalanceVillagers.debugMsg("--> Buy offer [" + min + " < " + price + " < " + max + "]");
                if (price < max || price > min) return false;
                
            } else {
                int amount = buy1.count * (isCompressed(buy1.getItem()) ? 9 : 1);
                if (buy2 != null) amount = amount + (buy2.count * (isCompressed(buy2.getItem()) ? 9 : 1));
                
                RebalanceVillagers.debugMsg("+-> Buy offer [" + min + " < " + amount + " < " + max + "]");
                if (amount < min || amount > max) return false;

            }

        }
        
        RebalanceVillagers.debugMsg("--> Offer seems ok");
        return true;
    }
    
    private Item getUncompressed(Item cItem) {
        if (compressedForms.containsValue(cItem)) {
            for (Item key : compressedForms.keySet()) {
                if (compressedForms.get(key) == cItem) return key;
            }
        }
        return null;
    }
    private boolean isCompressed(Item cItem) {
        return getUncompressed(cItem) != null;
    }

    private static HashMap<Item, Item> compressedForms;  // private static final Map 
    
    private static Item currencyItem = Items.EMERALD;
    private static HashMap<Integer, PotentialOffersList> offersByProfession = new HashMap<Integer, PotentialOffersList>();

    
    private static boolean offerRemoval = true;
    private static int removalMinimum = 2;
    private static int removalMaximum = 13;
    
    private static int defaultOfferCount = 1;
    private static int newOfferCount = 1;
    private static int generationTicks = 40;
    private static boolean newForAnyTrade = false;
    private static int newProbability = 100;
    
    private static int dryrunCheckTicks = 200;
    private int dryrunCheck = -1;

    private static int particleTicks = 200;
    private static boolean allowMultivending = false;
    private static boolean canTradeChildren = false;
        
    private boolean initialUpdateCheck = true;

    static
    {
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
    

    public static void setCheckDryRun(int count)           {   dryrunCheckTicks = count;  }

    public static void setDefaultOfferCount(int count)      {   defaultOfferCount = count;  }
    public static void setNewOfferCount(int count)          {   newOfferCount = count;      }
    public static void setGenerationTicks(int ticks)        {   generationTicks = ticks;    }
    public static void setForAnyTrade(boolean allow)        {   newForAnyTrade = allow;     }
    public static void setNewProbability(int prob)          {   newProbability = prob;      }

    public static void setParticleTicks(int ticks)          {   particleTicks = ticks;      }
    public static void setAllowMultivending(boolean allow)  {   allowMultivending = allow;  }
    public static void setCanTradeChildren(boolean allow)   {   canTradeChildren = allow;   }
        
    public static void setCurrencyItem(Item item)              {   currencyItem = item;            }
    public static void setOffersByProfession(HashMap<Integer, PotentialOffersList> offers)
    {
        offersByProfession = offers;
    }
    public static void setBuyValues(HashMap<Item, Tuple> buys)
    {
        buyValues = buys;
    }
    public static void setSellValues(HashMap<Item, Tuple> sells)
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
                merchantrecipelist.add(getOffer(buy.getItem(), buyValues, random));
        }
        
        for(SimpleOffer sell: offers.getSells())
        {
            if(offerOccurs(sell, random))
                merchantrecipelist.add(getOffer(sell.getItem(), sellValues, random));
        }
        
        for(CustomOffer other: offers.getOther())
        {
            if(offerOccurs(other, random))
                merchantrecipelist.add(other.getOffer());
        }
    }
    
    private boolean itemStackEqual(ItemStack a, ItemStack b) {
        if (a != null) {
            if (b == null) return false;
            
            if (!a.doMaterialsMatch(b)) return false;
            if (a.count != b.count) return false;
            
        } else if (b != null) return false;
        
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private void findAndRemoveAlreadyActiveRecipes(MerchantRecipeList merchantrecipelist) {
        if (mrList == null || mrList.size() == 0) return;
        
        ArrayList<MerchantRecipe> doubles = null;
        Iterator<MerchantRecipe> merchantrecipelistIterator = merchantrecipelist.iterator();
        while (merchantrecipelistIterator.hasNext()) {
            MerchantRecipe recipeToAdd = merchantrecipelistIterator.next();

            Iterator<MerchantRecipe> iIterator = mrList.iterator();
            while (iIterator.hasNext()) {
                MerchantRecipe activeReceipe = iIterator.next();
                
                boolean check1 = itemStackEqual(recipeToAdd.getBuyItem1(),activeReceipe.getBuyItem1());
                boolean check2 = itemStackEqual(recipeToAdd.getBuyItem2(),activeReceipe.getBuyItem2());
                boolean check3 = itemStackEqual(recipeToAdd.getBuyItem3(),activeReceipe.getBuyItem3());

                if (check1 && check2 && check3) {
                    if (doubles == null) doubles = new ArrayList<MerchantRecipe>();
                    doubles.add(recipeToAdd);
                    break;
                }
            }
            
        }
        if (doubles != null) merchantrecipelist.removeAll(doubles);
    }
    
    private void checkForInactiveOffersOnly(boolean force) {
        if (i == null) return;
        
        if (!force) {
            if (dryrunCheck < 0) return;
            else if(dryrunCheck > 0) dryrunCheck--;
            if(dryrunCheck > 0) return;
        }
        dryrunCheck = -1;
        
        // check for inactive recipes only
        if(mrList.size() > 1)
        {
            MerchantRecipe first = null;
            boolean foundActive = false;
            Iterator<?> iterator = mrList.iterator();
            do
            {
                if(!iterator.hasNext()) break;
                MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();
                if(merchantrecipe.g())  // if uses exceeded maxUses 
                {
                    if (first == null) first = merchantrecipe;
                } else {
                    foundActive = true;
                    break;
                }
            } while(true);
            
            if (!foundActive) {
                first.a(random.nextInt(6) + random.nextInt(6) + 2);
            }
            
        // we don't have any recipes, create more
        } else {
            generateNewOffers(1);
        }
    }
    
    /**
     * Generates the random max uses for an offer
     * @param random - pass a random object here, so we don't need to create one every time
     * @return a random max use count
     */
    private static int maxUses(Random random) {
        int firstDice = (removalMaximum - removalMinimum)/2 + 1;
        int secondDice = removalMaximum - removalMinimum - firstDice + 2;
        
        //Insurance if either value came out invalid.
        firstDice = (firstDice < 1) ? 1 : firstDice;
        secondDice = (secondDice < 1) ? 1 : secondDice;

        return random.nextInt(firstDice) + random.nextInt(secondDice) + removalMinimum;
    }

    @SuppressWarnings("unchecked")
    private void addDefaultRecipes() {
        MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
        if(offersByProfession.containsKey(-1)) populateMerchantRecipeList(merchantrecipelist, offersByProfession.get(-1), random); //Attempt loading user-specified defaults.
        if(merchantrecipelist.isEmpty()) merchantrecipelist.add(getOffer(Items.GOLD_INGOT, buyValues, random)); //If all else fails...
        for(int l = 0; l < merchantrecipelist.size(); l++) mrList.a((MerchantRecipe)merchantrecipelist.get(l));
    }
    
    static 
    {
        // removed original put's
        //Sadly, can't include lapis because it would be considered equal to all dyes.
        compressedForms = new HashMap<Item, Item>();
        compressedForms.put(Items.EMERALD, Item.getItemOf(Blocks.EMERALD_BLOCK));
        compressedForms.put(Items.GOLD_INGOT, Item.getItemOf(Blocks.GOLD_BLOCK));
        compressedForms.put(Items.GOLD_NUGGET, Items.GOLD_INGOT);
        compressedForms.put(Items.DIAMOND, Item.getItemOf(Blocks.DIAMOND_BLOCK));
        compressedForms.put(Items.IRON_INGOT, Item.getItemOf(Blocks.IRON_BLOCK));
        // MOD END
    }
    
}
