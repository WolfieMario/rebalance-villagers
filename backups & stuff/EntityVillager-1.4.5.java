package net.minecraft.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

public class EntityVillager extends EntityAgeable
    implements NPC, IMerchant
{

    public EntityVillager(World world)
    {
        this(world, 0);
    }

    public EntityVillager(World world, int k)
    {
        super(world);
        profession = 0;
        f = false;
        g = false;
        village = null;
        setProfession(k);
        texture = "/mob/villager/villager.png";
        bG = 0.5F;
        getNavigation().b(true);
        getNavigation().a(true);
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalAvoidPlayer(this, net/minecraft/server/EntityZombie, 8F, 0.3F, 0.35F));
        goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
        goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
        goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
        goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
        goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
        goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.3F));
        goalSelector.a(6, new PathfinderGoalMakeLove(this));
        goalSelector.a(7, new PathfinderGoalTakeFlower(this));
        goalSelector.a(8, new PathfinderGoalPlay(this, 0.32F));
        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/EntityHuman, 3F, 1.0F));
        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/EntityVillager, 5F, 0.02F));
        goalSelector.a(9, new PathfinderGoalRandomStroll(this, 0.3F));
        goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, net/minecraft/server/EntityLiving, 8F));
    }

    public boolean be()
    {
        return true;
    }

    protected void bm()
    {
        if(--profession <= 0)
        {
            world.villages.a(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ));
            profession = 70 + random.nextInt(50);
            village = world.villages.getClosestVillage(MathHelper.floor(locX), MathHelper.floor(locY), MathHelper.floor(locZ), 32);
            if(village == null)
            {
                aL();
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
        if(!p() && j > 0)
        {
            j--;
            if(j <= 0)
            {
                if(bI)
                {
                    if(i.size() > 1)
                    {
                        Iterator iterator = i.iterator();
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
                    bI = false;
                    if(village != null && bK != null)
                    {
                        world.broadcastEntityEffect(this, (byte)14);
                        village.a(bK, 1);
                    }
                }
                addEffect(new MobEffect(MobEffectList.REGENERATION.id, 200, 0));
            }
        }
        super.bm();
    }

    public boolean a(EntityHuman entityhuman)
    {
        ItemStack itemstack = entityhuman.inventory.getItemInHand();
        boolean flag = itemstack != null && itemstack.id == Item.MONSTER_EGG.id;
        if(!flag && isAlive() && !p() && !isBaby())
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

    protected void a()
    {
        super.a();
        datawatcher.a(16, Integer.valueOf(0));
    }

    public int getMaxHealth()
    {
        return 20;
    }

    public void b(NBTTagCompound nbttagcompound)
    {
        super.b(nbttagcompound);
        nbttagcompound.setInt("Profession", getProfession());
        nbttagcompound.setInt("Riches", bJ);
        if(i != null)
            nbttagcompound.setCompound("Offers", i.a());
    }

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

    protected boolean bj()
    {
        return false;
    }

    protected String aY()
    {
        return "mob.villager.default";
    }

    protected String aZ()
    {
        return "mob.villager.defaulthurt";
    }

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

    public boolean n()
    {
        return f;
    }

    public void f(boolean flag)
    {
        f = flag;
    }

    public void g(boolean flag)
    {
        g = flag;
    }

    public boolean o()
    {
        return g;
    }

    public void c(EntityLiving entityliving)
    {
        super.c(entityliving);
        if(village != null && entityliving != null)
        {
            village.a(entityliving);
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

    public void b_(EntityHuman entityhuman)
    {
        h = entityhuman;
    }

    public EntityHuman m_()
    {
        return h;
    }

    public boolean p()
    {
        return h != null;
    }

    public void a(MerchantRecipe merchantrecipe)
    {
        merchantrecipe.f();
        if(merchantrecipe.a((MerchantRecipe)i.get(i.size() - 1)))
        {
            j = 40;
            bI = true;
            if(h != null)
                bK = h.getName();
            else
                bK = null;
        }
        if(merchantrecipe.getBuyItem1().id == Item.EMERALD.id)
            bJ += merchantrecipe.getBuyItem1().count;
    }

    public MerchantRecipeList getOffers(EntityHuman entityhuman)
    {
        if(i == null)
            t(1);
        return i;
    }

    private float j(float f1)
    {
        float f2 = f1 + bM;
        if(f2 > 0.9F)
            return 0.9F - (f2 - 0.9F);
        else
            return f2;
    }

    private void t(int k)
    {
        if(i != null)
            bM = MathHelper.c(i.size()) * 0.2F;
        else
            bM = 0.0F;
        MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
        switch(getProfession())
        {
        case 0: // '\0'
            a(merchantrecipelist, Item.WHEAT.id, random, j(0.9F));
            a(merchantrecipelist, Block.WOOL.id, random, j(0.5F));
            a(merchantrecipelist, Item.RAW_CHICKEN.id, random, j(0.5F));
            a(merchantrecipelist, Item.COOKED_FISH.id, random, j(0.4F));
            b(merchantrecipelist, Item.BREAD.id, random, j(0.9F));
            b(merchantrecipelist, Item.MELON.id, random, j(0.3F));
            b(merchantrecipelist, Item.APPLE.id, random, j(0.3F));
            b(merchantrecipelist, Item.COOKIE.id, random, j(0.3F));
            b(merchantrecipelist, Item.SHEARS.id, random, j(0.3F));
            b(merchantrecipelist, Item.FLINT_AND_STEEL.id, random, j(0.3F));
            b(merchantrecipelist, Item.COOKED_CHICKEN.id, random, j(0.3F));
            b(merchantrecipelist, Item.ARROW.id, random, j(0.5F));
            if(random.nextFloat() < j(0.5F))
                merchantrecipelist.add(new MerchantRecipe(new ItemStack(Block.GRAVEL, 10), new ItemStack(Item.EMERALD), new ItemStack(Item.FLINT.id, 4 + random.nextInt(2), 0)));
            break;

        case 4: // '\004'
            a(merchantrecipelist, Item.COAL.id, random, j(0.7F));
            a(merchantrecipelist, Item.PORK.id, random, j(0.5F));
            a(merchantrecipelist, Item.RAW_BEEF.id, random, j(0.5F));
            b(merchantrecipelist, Item.SADDLE.id, random, j(0.1F));
            b(merchantrecipelist, Item.LEATHER_CHESTPLATE.id, random, j(0.3F));
            b(merchantrecipelist, Item.LEATHER_BOOTS.id, random, j(0.3F));
            b(merchantrecipelist, Item.LEATHER_HELMET.id, random, j(0.3F));
            b(merchantrecipelist, Item.LEATHER_LEGGINGS.id, random, j(0.3F));
            b(merchantrecipelist, Item.GRILLED_PORK.id, random, j(0.3F));
            b(merchantrecipelist, Item.COOKED_BEEF.id, random, j(0.3F));
            break;

        case 3: // '\003'
            a(merchantrecipelist, Item.COAL.id, random, j(0.7F));
            a(merchantrecipelist, Item.IRON_INGOT.id, random, j(0.5F));
            a(merchantrecipelist, Item.GOLD_INGOT.id, random, j(0.5F));
            a(merchantrecipelist, Item.DIAMOND.id, random, j(0.5F));
            b(merchantrecipelist, Item.IRON_SWORD.id, random, j(0.5F));
            b(merchantrecipelist, Item.DIAMOND_SWORD.id, random, j(0.5F));
            b(merchantrecipelist, Item.IRON_AXE.id, random, j(0.3F));
            b(merchantrecipelist, Item.DIAMOND_AXE.id, random, j(0.3F));
            b(merchantrecipelist, Item.IRON_PICKAXE.id, random, j(0.5F));
            b(merchantrecipelist, Item.DIAMOND_PICKAXE.id, random, j(0.5F));
            b(merchantrecipelist, Item.IRON_SPADE.id, random, j(0.2F));
            b(merchantrecipelist, Item.DIAMOND_SPADE.id, random, j(0.2F));
            b(merchantrecipelist, Item.IRON_HOE.id, random, j(0.2F));
            b(merchantrecipelist, Item.DIAMOND_HOE.id, random, j(0.2F));
            b(merchantrecipelist, Item.IRON_BOOTS.id, random, j(0.2F));
            b(merchantrecipelist, Item.DIAMOND_BOOTS.id, random, j(0.2F));
            b(merchantrecipelist, Item.IRON_HELMET.id, random, j(0.2F));
            b(merchantrecipelist, Item.DIAMOND_HELMET.id, random, j(0.2F));
            b(merchantrecipelist, Item.IRON_CHESTPLATE.id, random, j(0.2F));
            b(merchantrecipelist, Item.DIAMOND_CHESTPLATE.id, random, j(0.2F));
            b(merchantrecipelist, Item.IRON_LEGGINGS.id, random, j(0.2F));
            b(merchantrecipelist, Item.DIAMOND_LEGGINGS.id, random, j(0.2F));
            b(merchantrecipelist, Item.CHAINMAIL_BOOTS.id, random, j(0.1F));
            b(merchantrecipelist, Item.CHAINMAIL_HELMET.id, random, j(0.1F));
            b(merchantrecipelist, Item.CHAINMAIL_CHESTPLATE.id, random, j(0.1F));
            b(merchantrecipelist, Item.CHAINMAIL_LEGGINGS.id, random, j(0.1F));
            break;

        case 1: // '\001'
            a(merchantrecipelist, Item.PAPER.id, random, j(0.8F));
            a(merchantrecipelist, Item.BOOK.id, random, j(0.8F));
            a(merchantrecipelist, Item.WRITTEN_BOOK.id, random, j(0.3F));
            b(merchantrecipelist, Block.BOOKSHELF.id, random, j(0.8F));
            b(merchantrecipelist, Block.GLASS.id, random, j(0.2F));
            b(merchantrecipelist, Item.COMPASS.id, random, j(0.2F));
            b(merchantrecipelist, Item.WATCH.id, random, j(0.2F));
            break;

        case 2: // '\002'
            b(merchantrecipelist, Item.EYE_OF_ENDER.id, random, j(0.3F));
            b(merchantrecipelist, Item.EXP_BOTTLE.id, random, j(0.2F));
            b(merchantrecipelist, Item.REDSTONE.id, random, j(0.4F));
            b(merchantrecipelist, Block.GLOWSTONE.id, random, j(0.3F));
            int ai[] = {
                Item.IRON_SWORD.id, Item.DIAMOND_SWORD.id, Item.IRON_CHESTPLATE.id, Item.DIAMOND_CHESTPLATE.id, Item.IRON_AXE.id, Item.DIAMOND_AXE.id, Item.IRON_PICKAXE.id, Item.DIAMOND_PICKAXE.id
            };
            int ai1[] = ai;
            int i1 = ai1.length;
            for(int j1 = 0; j1 < i1; j1++)
            {
                int k1 = ai1[j1];
                if(random.nextFloat() < j(0.05F))
                    merchantrecipelist.add(new MerchantRecipe(new ItemStack(k1, 1, 0), new ItemStack(Item.EMERALD, 2 + random.nextInt(3), 0), EnchantmentManager.a(random, new ItemStack(k1, 1, 0), 5 + random.nextInt(15))));
            }

            break;
        }
        if(merchantrecipelist.isEmpty())
            a(merchantrecipelist, Item.GOLD_INGOT.id, random, 1.0F);
        Collections.shuffle(merchantrecipelist);
        if(i == null)
            i = new MerchantRecipeList();
        for(int l = 0; l < k && l < merchantrecipelist.size(); l++)
            i.a((MerchantRecipe)merchantrecipelist.get(l));

    }

    private static void a(MerchantRecipeList merchantrecipelist, int k, Random random, float f1)
    {
        if(random.nextFloat() < f1)
            merchantrecipelist.add(new MerchantRecipe(a(k, random), Item.EMERALD));
    }

    private static ItemStack a(int k, Random random)
    {
        return new ItemStack(k, b(k, random), 0);
    }

    private static int b(int k, Random random)
    {
        Tuple tuple = (Tuple)bN.get(Integer.valueOf(k));
        if(tuple == null)
            return 1;
        if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
            return ((Integer)tuple.a()).intValue();
        else
            return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
    }

    private static void b(MerchantRecipeList merchantrecipelist, int k, Random random, float f1)
    {
        if(random.nextFloat() < f1)
        {
            int l = c(k, random);
            ItemStack itemstack;
            ItemStack itemstack1;
            if(l < 0)
            {
                itemstack = new ItemStack(Item.EMERALD.id, 1, 0);
                itemstack1 = new ItemStack(k, -l, 0);
            } else
            {
                itemstack = new ItemStack(Item.EMERALD.id, l, 0);
                itemstack1 = new ItemStack(k, 1, 0);
            }
            merchantrecipelist.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }

    private static int c(int k, Random random)
    {
        Tuple tuple = (Tuple)bO.get(Integer.valueOf(k));
        if(tuple == null)
            return 1;
        if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
            return ((Integer)tuple.a()).intValue();
        else
            return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
    }

    public void bG()
    {
        setProfession(world.random.nextInt(5));
    }

    public void q()
    {
        bL = true;
    }

    public EntityVillager b(EntityAgeable entityageable)
    {
        EntityVillager entityvillager = new EntityVillager(world);
        entityvillager.bG();
        return entityvillager;
    }

    public EntityAgeable createChild(EntityAgeable entityageable)
    {
        return b(entityageable);
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
    private float bM;
    private static final Map bN;
    private static final Map bO;

    static 
    {
        bN = new HashMap();
        bO = new HashMap();
        bN.put(Integer.valueOf(Item.COAL.id), new Tuple(Integer.valueOf(16), Integer.valueOf(24)));
        bN.put(Integer.valueOf(Item.IRON_INGOT.id), new Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bN.put(Integer.valueOf(Item.GOLD_INGOT.id), new Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bN.put(Integer.valueOf(Item.DIAMOND.id), new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bN.put(Integer.valueOf(Item.PAPER.id), new Tuple(Integer.valueOf(24), Integer.valueOf(36)));
        bN.put(Integer.valueOf(Item.BOOK.id), new Tuple(Integer.valueOf(11), Integer.valueOf(13)));
        bN.put(Integer.valueOf(Item.WRITTEN_BOOK.id), new Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        bN.put(Integer.valueOf(Item.ENDER_PEARL.id), new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bN.put(Integer.valueOf(Item.EYE_OF_ENDER.id), new Tuple(Integer.valueOf(2), Integer.valueOf(3)));
        bN.put(Integer.valueOf(Item.PORK.id), new Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bN.put(Integer.valueOf(Item.RAW_BEEF.id), new Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bN.put(Integer.valueOf(Item.RAW_CHICKEN.id), new Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bN.put(Integer.valueOf(Item.COOKED_FISH.id), new Tuple(Integer.valueOf(9), Integer.valueOf(13)));
        bN.put(Integer.valueOf(Item.SEEDS.id), new Tuple(Integer.valueOf(34), Integer.valueOf(48)));
        bN.put(Integer.valueOf(Item.MELON_SEEDS.id), new Tuple(Integer.valueOf(30), Integer.valueOf(38)));
        bN.put(Integer.valueOf(Item.PUMPKIN_SEEDS.id), new Tuple(Integer.valueOf(30), Integer.valueOf(38)));
        bN.put(Integer.valueOf(Item.WHEAT.id), new Tuple(Integer.valueOf(18), Integer.valueOf(22)));
        bN.put(Integer.valueOf(Block.WOOL.id), new Tuple(Integer.valueOf(14), Integer.valueOf(22)));
        bN.put(Integer.valueOf(Item.ROTTEN_FLESH.id), new Tuple(Integer.valueOf(36), Integer.valueOf(64)));
        bO.put(Integer.valueOf(Item.FLINT_AND_STEEL.id), new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bO.put(Integer.valueOf(Item.SHEARS.id), new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bO.put(Integer.valueOf(Item.IRON_SWORD.id), new Tuple(Integer.valueOf(7), Integer.valueOf(11)));
        bO.put(Integer.valueOf(Item.DIAMOND_SWORD.id), new Tuple(Integer.valueOf(12), Integer.valueOf(14)));
        bO.put(Integer.valueOf(Item.IRON_AXE.id), new Tuple(Integer.valueOf(6), Integer.valueOf(8)));
        bO.put(Integer.valueOf(Item.DIAMOND_AXE.id), new Tuple(Integer.valueOf(9), Integer.valueOf(12)));
        bO.put(Integer.valueOf(Item.IRON_PICKAXE.id), new Tuple(Integer.valueOf(7), Integer.valueOf(9)));
        bO.put(Integer.valueOf(Item.DIAMOND_PICKAXE.id), new Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bO.put(Integer.valueOf(Item.IRON_SPADE.id), new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bO.put(Integer.valueOf(Item.DIAMOND_SPADE.id), new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bO.put(Integer.valueOf(Item.IRON_HOE.id), new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bO.put(Integer.valueOf(Item.DIAMOND_HOE.id), new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bO.put(Integer.valueOf(Item.IRON_BOOTS.id), new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bO.put(Integer.valueOf(Item.DIAMOND_BOOTS.id), new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bO.put(Integer.valueOf(Item.IRON_HELMET.id), new Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bO.put(Integer.valueOf(Item.DIAMOND_HELMET.id), new Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bO.put(Integer.valueOf(Item.IRON_CHESTPLATE.id), new Tuple(Integer.valueOf(10), Integer.valueOf(14)));
        bO.put(Integer.valueOf(Item.DIAMOND_CHESTPLATE.id), new Tuple(Integer.valueOf(16), Integer.valueOf(19)));
        bO.put(Integer.valueOf(Item.IRON_LEGGINGS.id), new Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bO.put(Integer.valueOf(Item.DIAMOND_LEGGINGS.id), new Tuple(Integer.valueOf(11), Integer.valueOf(14)));
        bO.put(Integer.valueOf(Item.CHAINMAIL_BOOTS.id), new Tuple(Integer.valueOf(5), Integer.valueOf(7)));
        bO.put(Integer.valueOf(Item.CHAINMAIL_HELMET.id), new Tuple(Integer.valueOf(5), Integer.valueOf(7)));
        bO.put(Integer.valueOf(Item.CHAINMAIL_CHESTPLATE.id), new Tuple(Integer.valueOf(11), Integer.valueOf(15)));
        bO.put(Integer.valueOf(Item.CHAINMAIL_LEGGINGS.id), new Tuple(Integer.valueOf(9), Integer.valueOf(11)));
        bO.put(Integer.valueOf(Item.BREAD.id), new Tuple(Integer.valueOf(-4), Integer.valueOf(-2)));
        bO.put(Integer.valueOf(Item.MELON.id), new Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        bO.put(Integer.valueOf(Item.APPLE.id), new Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        bO.put(Integer.valueOf(Item.COOKIE.id), new Tuple(Integer.valueOf(-10), Integer.valueOf(-7)));
        bO.put(Integer.valueOf(Block.GLASS.id), new Tuple(Integer.valueOf(-5), Integer.valueOf(-3)));
        bO.put(Integer.valueOf(Block.BOOKSHELF.id), new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bO.put(Integer.valueOf(Item.LEATHER_CHESTPLATE.id), new Tuple(Integer.valueOf(4), Integer.valueOf(5)));
        bO.put(Integer.valueOf(Item.LEATHER_BOOTS.id), new Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bO.put(Integer.valueOf(Item.LEATHER_HELMET.id), new Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bO.put(Integer.valueOf(Item.LEATHER_LEGGINGS.id), new Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bO.put(Integer.valueOf(Item.SADDLE.id), new Tuple(Integer.valueOf(6), Integer.valueOf(8)));
        bO.put(Integer.valueOf(Item.EXP_BOTTLE.id), new Tuple(Integer.valueOf(-4), Integer.valueOf(-1)));
        bO.put(Integer.valueOf(Item.REDSTONE.id), new Tuple(Integer.valueOf(-4), Integer.valueOf(-1)));
        bO.put(Integer.valueOf(Item.COMPASS.id), new Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bO.put(Integer.valueOf(Item.WATCH.id), new Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bO.put(Integer.valueOf(Block.GLOWSTONE.id), new Tuple(Integer.valueOf(-3), Integer.valueOf(-1)));
        bO.put(Integer.valueOf(Item.GRILLED_PORK.id), new Tuple(Integer.valueOf(-7), Integer.valueOf(-5)));
        bO.put(Integer.valueOf(Item.COOKED_BEEF.id), new Tuple(Integer.valueOf(-7), Integer.valueOf(-5)));
        bO.put(Integer.valueOf(Item.COOKED_CHICKEN.id), new Tuple(Integer.valueOf(-8), Integer.valueOf(-6)));
        bO.put(Integer.valueOf(Item.EYE_OF_ENDER.id), new Tuple(Integer.valueOf(7), Integer.valueOf(11)));
        bO.put(Integer.valueOf(Item.ARROW.id), new Tuple(Integer.valueOf(-12), Integer.valueOf(-8)));
    }
}
