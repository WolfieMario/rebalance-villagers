// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SourceFile

package net.minecraft.server.v1_7_R1;

import java.util.*;

public class EntityVillager extends EntityAgeable
    implements IMerchant, NPC
{

    public EntityVillager(World world)
    {
        this(world, 0);
    }

    public EntityVillager(World world, int k)
    {
        super(world);
        setProfession(k);
        a(0.6F, 1.8F);
        getNavigation().b(true);
        getNavigation().a(true);
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalAvoidPlayer(this, net/minecraft/server/v1_7_R1/EntityZombie, 8F, 0.59999999999999998D, 0.59999999999999998D));
        goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
        goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
        goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
        goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
        goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
        goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.59999999999999998D));
        goalSelector.a(6, new PathfinderGoalMakeLove(this));
        goalSelector.a(7, new PathfinderGoalTakeFlower(this));
        goalSelector.a(8, new PathfinderGoalPlay(this, 0.32000000000000001D));
        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/v1_7_R1/EntityHuman, 3F, 1.0F));
        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/v1_7_R1/EntityVillager, 5F, 0.02F));
        goalSelector.a(9, new PathfinderGoalRandomStroll(this, 0.59999999999999998D));
        goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, net/minecraft/server/v1_7_R1/EntityInsentient, 8F));
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

    protected void bp()
    {
        if(--profession <= 0)
        {
            world.villages.a(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ));
            profession = 70 + random.nextInt(50);
            village = world.villages.getClosestVillage(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ), 32);
            if(village == null)
            {
                bV();
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
        if(!ca() && bv > 0)
        {
            bv--;
            if(bv <= 0)
            {
                if(bw)
                {
                    if(bu.size() > 1)
                    {
                        Iterator iterator = bu.iterator();
                        do
                        {
                            if(!iterator.hasNext())
                                break;
                            MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();
                            if(merchantrecipe.g())
                                merchantrecipe.a(random.nextInt(6) + random.nextInt(6) + 2);
                        } while(true);
                    }
                    t(1);
                    bw = false;
                    if(village != null && by != null)
                    {
                        world.broadcastEntityEffect(this, (byte)14);
                        village.a(by, 1);
                    }
                }
                addEffect(new MobEffect(MobEffectList.REGENERATION.id, 200, 0));
            }
        }
        super.bp();
    }

    public boolean a(EntityHuman entityhuman)
    {
        ItemStack itemstack = entityhuman.inventory.getItemInHand();
        boolean flag = itemstack != null && itemstack.getItem() == Items.MONSTER_EGG;
        if(!flag && isAlive() && !ca() && !isBaby())
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

    protected void c()
    {
        super.c();
        datawatcher.a(16, Integer.valueOf(0));
    }

    public void b(NBTTagCompound nbttagcompound)
    {
        super.b(nbttagcompound);
        nbttagcompound.setInt("Profession", getProfession());
        nbttagcompound.setInt("Riches", riches);
        if(bu != null)
            nbttagcompound.set("Offers", bu.a());
    }

    public void a(NBTTagCompound nbttagcompound)
    {
        super.a(nbttagcompound);
        setProfession(nbttagcompound.getInt("Profession"));
        riches = nbttagcompound.getInt("Riches");
        if(nbttagcompound.hasKeyOfType("Offers", 10))
        {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Offers");
            bu = new MerchantRecipeList(nbttagcompound1);
        }
    }

    protected boolean isTypeNotPersistent()
    {
        return false;
    }

    protected String t()
    {
        if(ca())
            return "mob.villager.haggle";
        else
            return "mob.villager.idle";
    }

    protected String aT()
    {
        return "mob.villager.hit";
    }

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

    public boolean bY()
    {
        return br;
    }

    public void i(boolean flag)
    {
        br = flag;
    }

    public void j(boolean flag)
    {
        bs = flag;
    }

    public boolean bZ()
    {
        return bs;
    }

    public void b(EntityLiving entityliving)
    {
        super.b(entityliving);
        if(village != null && entityliving != null)
        {
            village.a(entityliving);
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

    public void a_(EntityHuman entityhuman)
    {
        tradingPlayer = entityhuman;
    }

    public EntityHuman b()
    {
        return tradingPlayer;
    }

    public boolean ca()
    {
        return tradingPlayer != null;
    }

    public void a(MerchantRecipe merchantrecipe)
    {
        merchantrecipe.f();
        a_ = -q();
        makeSound("mob.villager.yes", bf(), bg());
        if(merchantrecipe.a((MerchantRecipe)bu.get(bu.size() - 1)))
        {
            bv = 40;
            bw = true;
            if(tradingPlayer != null)
                by = tradingPlayer.getName();
            else
                by = null;
        }
        if(merchantrecipe.getBuyItem1().getItem() == Items.EMERALD)
            riches += merchantrecipe.getBuyItem1().count;
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

    public MerchantRecipeList getOffers(EntityHuman entityhuman)
    {
        if(bu == null)
            t(1);
        return bu;
    }

    private float p(float f)
    {
        float f1 = f + bA;
        if(f1 > 0.9F)
            return 0.9F - (f1 - 0.9F);
        else
            return f1;
    }

    private void t(int k)
    {
        if(bu != null)
            bA = MathHelper.c(bu.size()) * 0.2F;
        else
            bA = 0.0F;
        MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
        switch(getProfession())
        {
        case 0: // '\0'
            a(merchantrecipelist, Items.WHEAT, random, p(0.9F));
            a(merchantrecipelist, Item.getItemOf(Blocks.WOOL), random, p(0.5F));
            a(merchantrecipelist, Items.RAW_CHICKEN, random, p(0.5F));
            a(merchantrecipelist, Items.COOKED_FISH, random, p(0.4F));
            b(merchantrecipelist, Items.BREAD, random, p(0.9F));
            b(merchantrecipelist, Items.MELON, random, p(0.3F));
            b(merchantrecipelist, Items.APPLE, random, p(0.3F));
            b(merchantrecipelist, Items.COOKIE, random, p(0.3F));
            b(merchantrecipelist, Items.SHEARS, random, p(0.3F));
            b(merchantrecipelist, Items.FLINT_AND_STEEL, random, p(0.3F));
            b(merchantrecipelist, Items.COOKED_CHICKEN, random, p(0.3F));
            b(merchantrecipelist, Items.ARROW, random, p(0.5F));
            if(random.nextFloat() < p(0.5F))
                merchantrecipelist.add(new MerchantRecipe(new ItemStack(Blocks.GRAVEL, 10), new ItemStack(Items.EMERALD), new ItemStack(Items.FLINT, 4 + random.nextInt(2), 0)));
            break;

        case 4: // '\004'
            a(merchantrecipelist, Items.COAL, random, p(0.7F));
            a(merchantrecipelist, Items.PORK, random, p(0.5F));
            a(merchantrecipelist, Items.RAW_BEEF, random, p(0.5F));
            b(merchantrecipelist, Items.SADDLE, random, p(0.1F));
            b(merchantrecipelist, Items.LEATHER_CHESTPLATE, random, p(0.3F));
            b(merchantrecipelist, Items.LEATHER_BOOTS, random, p(0.3F));
            b(merchantrecipelist, Items.LEATHER_HELMET, random, p(0.3F));
            b(merchantrecipelist, Items.LEATHER_LEGGINGS, random, p(0.3F));
            b(merchantrecipelist, Items.GRILLED_PORK, random, p(0.3F));
            b(merchantrecipelist, Items.COOKED_BEEF, random, p(0.3F));
            break;

        case 3: // '\003'
            a(merchantrecipelist, Items.COAL, random, p(0.7F));
            a(merchantrecipelist, Items.IRON_INGOT, random, p(0.5F));
            a(merchantrecipelist, Items.GOLD_INGOT, random, p(0.5F));
            a(merchantrecipelist, Items.DIAMOND, random, p(0.5F));
            b(merchantrecipelist, Items.IRON_SWORD, random, p(0.5F));
            b(merchantrecipelist, Items.DIAMOND_SWORD, random, p(0.5F));
            b(merchantrecipelist, Items.IRON_AXE, random, p(0.3F));
            b(merchantrecipelist, Items.DIAMOND_AXE, random, p(0.3F));
            b(merchantrecipelist, Items.IRON_PICKAXE, random, p(0.5F));
            b(merchantrecipelist, Items.DIAMOND_PICKAXE, random, p(0.5F));
            b(merchantrecipelist, Items.IRON_SPADE, random, p(0.2F));
            b(merchantrecipelist, Items.DIAMOND_SPADE, random, p(0.2F));
            b(merchantrecipelist, Items.IRON_HOE, random, p(0.2F));
            b(merchantrecipelist, Items.DIAMOND_HOE, random, p(0.2F));
            b(merchantrecipelist, Items.IRON_BOOTS, random, p(0.2F));
            b(merchantrecipelist, Items.DIAMOND_BOOTS, random, p(0.2F));
            b(merchantrecipelist, Items.IRON_HELMET, random, p(0.2F));
            b(merchantrecipelist, Items.DIAMOND_HELMET, random, p(0.2F));
            b(merchantrecipelist, Items.IRON_CHESTPLATE, random, p(0.2F));
            b(merchantrecipelist, Items.DIAMOND_CHESTPLATE, random, p(0.2F));
            b(merchantrecipelist, Items.IRON_LEGGINGS, random, p(0.2F));
            b(merchantrecipelist, Items.DIAMOND_LEGGINGS, random, p(0.2F));
            b(merchantrecipelist, Items.CHAINMAIL_BOOTS, random, p(0.1F));
            b(merchantrecipelist, Items.CHAINMAIL_HELMET, random, p(0.1F));
            b(merchantrecipelist, Items.CHAINMAIL_CHESTPLATE, random, p(0.1F));
            b(merchantrecipelist, Items.CHAINMAIL_LEGGINGS, random, p(0.1F));
            break;

        case 1: // '\001'
            a(merchantrecipelist, Items.PAPER, random, p(0.8F));
            a(merchantrecipelist, Items.BOOK, random, p(0.8F));
            a(merchantrecipelist, Items.WRITTEN_BOOK, random, p(0.3F));
            b(merchantrecipelist, Item.getItemOf(Blocks.BOOKSHELF), random, p(0.8F));
            b(merchantrecipelist, Item.getItemOf(Blocks.GLASS), random, p(0.2F));
            b(merchantrecipelist, Items.COMPASS, random, p(0.2F));
            b(merchantrecipelist, Items.WATCH, random, p(0.2F));
            if(random.nextFloat() < p(0.07F))
            {
                Enchantment enchantment = Enchantment.c[random.nextInt(Enchantment.c.length)];
                int i1 = MathHelper.nextInt(random, enchantment.getStartLevel(), enchantment.getMaxLevel());
                ItemStack itemstack = Items.ENCHANTED_BOOK.a(new EnchantmentInstance(enchantment, i1));
                int k1 = 2 + random.nextInt(5 + i1 * 10) + 3 * i1;
                merchantrecipelist.add(new MerchantRecipe(new ItemStack(Items.BOOK), new ItemStack(Items.EMERALD, k1), itemstack));
            }
            break;

        case 2: // '\002'
            b(merchantrecipelist, Items.EYE_OF_ENDER, random, p(0.3F));
            b(merchantrecipelist, Items.EXP_BOTTLE, random, p(0.2F));
            b(merchantrecipelist, Items.REDSTONE, random, p(0.4F));
            b(merchantrecipelist, Item.getItemOf(Blocks.GLOWSTONE), random, p(0.3F));
            Item aitem[] = {
                Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE
            };
            Item aitem1[] = aitem;
            int j1 = aitem1.length;
            for(int l1 = 0; l1 < j1; l1++)
            {
                Item item = aitem1[l1];
                if(random.nextFloat() < p(0.05F))
                    merchantrecipelist.add(new MerchantRecipe(new ItemStack(item, 1, 0), new ItemStack(Items.EMERALD, 2 + random.nextInt(3), 0), EnchantmentManager.a(random, new ItemStack(item, 1, 0), 5 + random.nextInt(15))));
            }

            break;
        }
        if(merchantrecipelist.isEmpty())
            a(merchantrecipelist, Items.GOLD_INGOT, random, 1.0F);
        Collections.shuffle(merchantrecipelist);
        if(bu == null)
            bu = new MerchantRecipeList();
        for(int l = 0; l < k && l < merchantrecipelist.size(); l++)
            bu.a((MerchantRecipe)merchantrecipelist.get(l));

    }

    private static void a(MerchantRecipeList merchantrecipelist, Item item, Random random, float f)
    {
        if(random.nextFloat() < f)
            merchantrecipelist.add(new MerchantRecipe(a(item, random), Items.EMERALD));
    }

    private static ItemStack a(Item item, Random random)
    {
        return new ItemStack(item, b(item, random), 0);
    }

    private static int b(Item item, Random random)
    {
        Tuple tuple = (Tuple)bB.get(item);
        if(tuple == null)
            return 1;
        if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
            return ((Integer)tuple.a()).intValue();
        else
            return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
    }

    private static void b(MerchantRecipeList merchantrecipelist, Item item, Random random, float f)
    {
        if(random.nextFloat() < f)
        {
            int k = c(item, random);
            ItemStack itemstack;
            ItemStack itemstack1;
            if(k < 0)
            {
                itemstack = new ItemStack(Items.EMERALD, 1, 0);
                itemstack1 = new ItemStack(item, -k, 0);
            } else
            {
                itemstack = new ItemStack(Items.EMERALD, k, 0);
                itemstack1 = new ItemStack(item, 1, 0);
            }
            merchantrecipelist.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }

    private static int c(Item item, Random random)
    {
        Tuple tuple = (Tuple)bC.get(item);
        if(tuple == null)
            return 1;
        if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
            return ((Integer)tuple.a()).intValue();
        else
            return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
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
    private MerchantRecipeList bu;
    private int bv;
    private boolean bw;
    private int riches;
    private String by;
    private boolean bz;
    private float bA;
    private static final Map bB;
    private static final Map bC;

    static 
    {
        bB = new HashMap();
        bC = new HashMap();
        bB.put(Items.COAL, new Tuple(Integer.valueOf(16), Integer.valueOf(24)));
        bB.put(Items.IRON_INGOT, new Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bB.put(Items.GOLD_INGOT, new Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bB.put(Items.DIAMOND, new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bB.put(Items.PAPER, new Tuple(Integer.valueOf(24), Integer.valueOf(36)));
        bB.put(Items.BOOK, new Tuple(Integer.valueOf(11), Integer.valueOf(13)));
        bB.put(Items.WRITTEN_BOOK, new Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        bB.put(Items.ENDER_PEARL, new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bB.put(Items.EYE_OF_ENDER, new Tuple(Integer.valueOf(2), Integer.valueOf(3)));
        bB.put(Items.PORK, new Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bB.put(Items.RAW_BEEF, new Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bB.put(Items.RAW_CHICKEN, new Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bB.put(Items.COOKED_FISH, new Tuple(Integer.valueOf(9), Integer.valueOf(13)));
        bB.put(Items.SEEDS, new Tuple(Integer.valueOf(34), Integer.valueOf(48)));
        bB.put(Items.MELON_SEEDS, new Tuple(Integer.valueOf(30), Integer.valueOf(38)));
        bB.put(Items.PUMPKIN_SEEDS, new Tuple(Integer.valueOf(30), Integer.valueOf(38)));
        bB.put(Items.WHEAT, new Tuple(Integer.valueOf(18), Integer.valueOf(22)));
        bB.put(Item.getItemOf(Blocks.WOOL), new Tuple(Integer.valueOf(14), Integer.valueOf(22)));
        bB.put(Items.ROTTEN_FLESH, new Tuple(Integer.valueOf(36), Integer.valueOf(64)));
        bC.put(Items.FLINT_AND_STEEL, new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bC.put(Items.SHEARS, new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bC.put(Items.IRON_SWORD, new Tuple(Integer.valueOf(7), Integer.valueOf(11)));
        bC.put(Items.DIAMOND_SWORD, new Tuple(Integer.valueOf(12), Integer.valueOf(14)));
        bC.put(Items.IRON_AXE, new Tuple(Integer.valueOf(6), Integer.valueOf(8)));
        bC.put(Items.DIAMOND_AXE, new Tuple(Integer.valueOf(9), Integer.valueOf(12)));
        bC.put(Items.IRON_PICKAXE, new Tuple(Integer.valueOf(7), Integer.valueOf(9)));
        bC.put(Items.DIAMOND_PICKAXE, new Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bC.put(Items.IRON_SPADE, new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bC.put(Items.DIAMOND_SPADE, new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bC.put(Items.IRON_HOE, new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bC.put(Items.DIAMOND_HOE, new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bC.put(Items.IRON_BOOTS, new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bC.put(Items.DIAMOND_BOOTS, new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bC.put(Items.IRON_HELMET, new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bC.put(Items.DIAMOND_HELMET, new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bC.put(Items.IRON_CHESTPLATE, new Tuple(Integer.valueOf(10), Integer.valueOf(14)));
        bC.put(Items.DIAMOND_CHESTPLATE, new Tuple(Integer.valueOf(16), Integer.valueOf(19)));
        bC.put(Items.IRON_LEGGINGS, new Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bC.put(Items.DIAMOND_LEGGINGS, new Tuple(Integer.valueOf(11), Integer.valueOf(14)));
        bC.put(Items.CHAINMAIL_BOOTS, new Tuple(Integer.valueOf(5), Integer.valueOf(7)));
        bC.put(Items.CHAINMAIL_HELMET, new Tuple(Integer.valueOf(5), Integer.valueOf(7)));
        bC.put(Items.CHAINMAIL_CHESTPLATE, new Tuple(Integer.valueOf(11), Integer.valueOf(15)));
        bC.put(Items.CHAINMAIL_LEGGINGS, new Tuple(Integer.valueOf(9), Integer.valueOf(11)));
        bC.put(Items.BREAD, new Tuple(Integer.valueOf(-4), Integer.valueOf(-2)));
        bC.put(Items.MELON, new Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        bC.put(Items.APPLE, new Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        bC.put(Items.COOKIE, new Tuple(Integer.valueOf(-10), Integer.valueOf(-7)));
        bC.put(Item.getItemOf(Blocks.GLASS), new Tuple(Integer.valueOf(-5), Integer.valueOf(-3)));
        bC.put(Item.getItemOf(Blocks.BOOKSHELF), new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bC.put(Items.LEATHER_CHESTPLATE, new Tuple(Integer.valueOf(4), Integer.valueOf(5)));
        bC.put(Items.LEATHER_BOOTS, new Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bC.put(Items.LEATHER_HELMET, new Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bC.put(Items.LEATHER_LEGGINGS, new Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bC.put(Items.SADDLE, new Tuple(Integer.valueOf(6), Integer.valueOf(8)));
        bC.put(Items.EXP_BOTTLE, new Tuple(Integer.valueOf(-4), Integer.valueOf(-1)));
        bC.put(Items.REDSTONE, new Tuple(Integer.valueOf(-4), Integer.valueOf(-1)));
        bC.put(Items.COMPASS, new Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bC.put(Items.WATCH, new Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bC.put(Item.getItemOf(Blocks.GLOWSTONE), new Tuple(Integer.valueOf(-3), Integer.valueOf(-1)));
        bC.put(Items.GRILLED_PORK, new Tuple(Integer.valueOf(-7), Integer.valueOf(-5)));
        bC.put(Items.COOKED_BEEF, new Tuple(Integer.valueOf(-7), Integer.valueOf(-5)));
        bC.put(Items.COOKED_CHICKEN, new Tuple(Integer.valueOf(-8), Integer.valueOf(-6)));
        bC.put(Items.EYE_OF_ENDER, new Tuple(Integer.valueOf(7), Integer.valueOf(11)));
        bC.put(Items.ARROW, new Tuple(Integer.valueOf(-12), Integer.valueOf(-8)));
    }
}
