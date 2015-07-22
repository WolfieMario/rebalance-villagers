package net.minecraft.server.v1_5_R1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.server.Block;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.DamageSource;
import net.minecraft.server.DataWatcher;
import net.minecraft.server.Enchantment;
import net.minecraft.server.EnchantmentInstance;
import net.minecraft.server.EnchantmentManager;
import net.minecraft.server.EntityAgeable;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.IMerchant;
import net.minecraft.server.IMonster;
import net.minecraft.server.Item;
import net.minecraft.server.ItemArmor;
import net.minecraft.server.ItemEnchantedBook;
import net.minecraft.server.ItemShears;
import net.minecraft.server.ItemStack;
import net.minecraft.server.MathHelper;
import net.minecraft.server.MerchantRecipe;
import net.minecraft.server.MerchantRecipeList;
import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NPC;
import net.minecraft.server.Navigation;
import net.minecraft.server.PathfinderGoalAvoidPlayer;
import net.minecraft.server.PathfinderGoalFloat;
import net.minecraft.server.PathfinderGoalInteract;
import net.minecraft.server.PathfinderGoalLookAtPlayer;
import net.minecraft.server.PathfinderGoalLookAtTradingPlayer;
import net.minecraft.server.PathfinderGoalMakeLove;
import net.minecraft.server.PathfinderGoalMoveIndoors;
import net.minecraft.server.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.PathfinderGoalOpenDoor;
import net.minecraft.server.PathfinderGoalPlay;
import net.minecraft.server.PathfinderGoalRandomStroll;
import net.minecraft.server.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.PathfinderGoalTakeFlower;
import net.minecraft.server.PathfinderGoalTradeWithPlayer;
import net.minecraft.server.PlayerInventory;
import net.minecraft.server.Tuple;
import net.minecraft.server.Village;
import net.minecraft.server.VillageCollection;
import net.minecraft.server.World;

public class EntityVillager extends net.minecraft.server.v1_5_R1.EntityAgeable
    implements net.minecraft.server.v1_5_R1.NPC, net.minecraft.server.v1_5_R1.IMerchant
{

    public EntityVillager(net.minecraft.server.v1_5_R1.World world)
    {
        this(world, 0);
    }

    public EntityVillager(net.minecraft.server.v1_5_R1.World world, int k)
    {
        super(world);
        profession = 0;
        f = false;
        g = false;
        village = null;
        setProfession(k);
        texture = "/mob/villager/villager.png";
        bI = 0.5F;
        a(0.6F, 1.8F);
        getNavigation().b(true);
        getNavigation().a(true);
        goalSelector.a(0, new net.minecraft.server.v1_5_R1.PathfinderGoalFloat(this));
        goalSelector.a(1, new net.minecraft.server.v1_5_R1.PathfinderGoalAvoidPlayer(this, net/minecraft/server/v1_5_R1/EntityZombie, 8F, 0.3F, 0.35F));
        goalSelector.a(1, new net.minecraft.server.v1_5_R1.PathfinderGoalTradeWithPlayer(this));
        goalSelector.a(1, new net.minecraft.server.v1_5_R1.PathfinderGoalLookAtTradingPlayer(this));
        goalSelector.a(2, new net.minecraft.server.v1_5_R1.PathfinderGoalMoveIndoors(this));
        goalSelector.a(3, new net.minecraft.server.v1_5_R1.PathfinderGoalRestrictOpenDoor(this));
        goalSelector.a(4, new net.minecraft.server.v1_5_R1.PathfinderGoalOpenDoor(this, true));
        goalSelector.a(5, new net.minecraft.server.v1_5_R1.PathfinderGoalMoveTowardsRestriction(this, 0.3F));
        goalSelector.a(6, new net.minecraft.server.v1_5_R1.PathfinderGoalMakeLove(this));
        goalSelector.a(7, new net.minecraft.server.v1_5_R1.PathfinderGoalTakeFlower(this));
        goalSelector.a(8, new net.minecraft.server.v1_5_R1.PathfinderGoalPlay(this, 0.32F));
        goalSelector.a(9, new net.minecraft.server.v1_5_R1.PathfinderGoalInteract(this, net/minecraft/server/v1_5_R1/EntityHuman, 3F, 1.0F));
        goalSelector.a(9, new net.minecraft.server.v1_5_R1.PathfinderGoalInteract(this, net/minecraft/server/v1_5_R1/EntityVillager, 5F, 0.02F));
        goalSelector.a(9, new net.minecraft.server.v1_5_R1.PathfinderGoalRandomStroll(this, 0.3F));
        goalSelector.a(10, new net.minecraft.server.v1_5_R1.PathfinderGoalLookAtPlayer(this, net/minecraft/server/v1_5_R1/EntityLiving, 8F));
    }

    public boolean bh()
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
                aO();
            } else
            {
                net.minecraft.server.v1_5_R1.ChunkCoordinates chunkcoordinates = village.getCenter();
                b(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z, (int)((float)village.getSize() * 0.6F));
                if(bN)
                {
                    bN = false;
                    village.b(5);
                }
            }
        }
        if(!p() && j > 0)
        {
            j--;
            if(j <= 0)
            {
                if(bK)
                {
                    if(i.size() > 1)
                    {
                        Iterator iterator = i.iterator();
                        do
                        {
                            if(!iterator.hasNext())
                                break;
                            net.minecraft.server.v1_5_R1.MerchantRecipe merchantrecipe = (net.minecraft.server.v1_5_R1.MerchantRecipe)iterator.next();
                            if(merchantrecipe.g())
                                merchantrecipe.a(random.nextInt(6) + random.nextInt(6) + 2);
                        } while(true);
                    }
                    t(1);
                    bK = false;
                    if(village != null && bM != null)
                    {
                        world.broadcastEntityEffect(this, (byte)14);
                        village.a(bM, 1);
                    }
                }
                addEffect(new net.minecraft.server.v1_5_R1.MobEffect(MobEffectList.REGENERATION.id, 200, 0));
            }
        }
        super.bp();
    }

    public boolean a_(net.minecraft.server.v1_5_R1.EntityHuman entityhuman)
    {
        net.minecraft.server.v1_5_R1.ItemStack itemstack = entityhuman.inventory.getItemInHand();
        boolean flag = itemstack != null && itemstack.id == Item.MONSTER_EGG.id;
        if(!flag && isAlive() && !p() && !isBaby())
        {
            if(!world.isStatic)
            {
                a(entityhuman);
                entityhuman.openTrade(this, getCustomName());
            }
            return true;
        } else
        {
            return super.a_(entityhuman);
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

    public void b(net.minecraft.server.v1_5_R1.NBTTagCompound nbttagcompound)
    {
        super.b(nbttagcompound);
        nbttagcompound.setInt("Profession", getProfession());
        nbttagcompound.setInt("Riches", bL);
        if(i != null)
            nbttagcompound.setCompound("Offers", i.a());
    }

    public void a(net.minecraft.server.v1_5_R1.NBTTagCompound nbttagcompound)
    {
        super.a(nbttagcompound);
        setProfession(nbttagcompound.getInt("Profession"));
        bL = nbttagcompound.getInt("Riches");
        if(nbttagcompound.hasKey("Offers"))
        {
            net.minecraft.server.v1_5_R1.NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Offers");
            i = new net.minecraft.server.v1_5_R1.MerchantRecipeList(nbttagcompound1);
        }
    }

    protected boolean isTypeNotPersistent()
    {
        return false;
    }

    protected String bb()
    {
        return "mob.villager.default";
    }

    protected String bc()
    {
        return "mob.villager.defaulthurt";
    }

    protected String bd()
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

    public void i(boolean flag)
    {
        f = flag;
    }

    public void j(boolean flag)
    {
        g = flag;
    }

    public boolean o()
    {
        return g;
    }

    public void c(net.minecraft.server.v1_5_R1.EntityLiving entityliving)
    {
        super.c(entityliving);
        if(village != null && entityliving != null)
        {
            village.a(entityliving);
            if(entityliving instanceof net.minecraft.server.v1_5_R1.EntityHuman)
            {
                byte byte0 = -1;
                if(isBaby())
                    byte0 = -3;
                village.a(((net.minecraft.server.v1_5_R1.EntityHuman)entityliving).getName(), byte0);
                if(isAlive())
                    world.broadcastEntityEffect(this, (byte)13);
            }
        }
    }

    public void die(net.minecraft.server.v1_5_R1.DamageSource damagesource)
    {
        if(village != null)
        {
            Entity entity = damagesource.getEntity();
            if(entity != null)
            {
                if(entity instanceof net.minecraft.server.v1_5_R1.EntityHuman)
                    village.a(((net.minecraft.server.v1_5_R1.EntityHuman)entity).getName(), -2);
                else
                if(entity instanceof net.minecraft.server.v1_5_R1.IMonster)
                    village.h();
            } else
            if(entity == null)
            {
                net.minecraft.server.v1_5_R1.EntityHuman entityhuman = world.findNearbyPlayer(this, 16D);
                if(entityhuman != null)
                    village.h();
            }
        }
        super.die(damagesource);
    }

    public void a(net.minecraft.server.v1_5_R1.EntityHuman entityhuman)
    {
        h = entityhuman;
    }

    public net.minecraft.server.v1_5_R1.EntityHuman m_()
    {
        return h;
    }

    public boolean p()
    {
        return h != null;
    }

    public void a(net.minecraft.server.v1_5_R1.MerchantRecipe merchantrecipe)
    {
        merchantrecipe.f();
        if(merchantrecipe.a((net.minecraft.server.v1_5_R1.MerchantRecipe)i.get(i.size() - 1)))
        {
            j = 40;
            bK = true;
            if(h != null)
                bM = h.getName();
            else
                bM = null;
        }
        if(merchantrecipe.getBuyItem1().id == Item.EMERALD.id)
            bL += merchantrecipe.getBuyItem1().count;
    }

    public net.minecraft.server.v1_5_R1.MerchantRecipeList getOffers(net.minecraft.server.v1_5_R1.EntityHuman entityhuman)
    {
        if(i == null)
            t(1);
        return i;
    }

    private float j(float f1)
    {
        float f2 = f1 + bO;
        if(f2 > 0.9F)
            return 0.9F - (f2 - 0.9F);
        else
            return f2;
    }

    private void t(int k)
    {
        if(i != null)
            bO = MathHelper.c(i.size()) * 0.2F;
        else
            bO = 0.0F;
        net.minecraft.server.v1_5_R1.MerchantRecipeList merchantrecipelist = new net.minecraft.server.v1_5_R1.MerchantRecipeList();
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
                merchantrecipelist.add(new net.minecraft.server.v1_5_R1.MerchantRecipe(new net.minecraft.server.v1_5_R1.ItemStack(Block.GRAVEL, 10), new net.minecraft.server.v1_5_R1.ItemStack(Item.EMERALD), new net.minecraft.server.v1_5_R1.ItemStack(Item.FLINT.id, 4 + random.nextInt(2), 0)));
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
            if(random.nextFloat() < j(0.07F))
            {
                net.minecraft.server.v1_5_R1.Enchantment enchantment = Enchantment.c[random.nextInt(Enchantment.c.length)];
                int i1 = MathHelper.nextInt(random, enchantment.getStartLevel(), enchantment.getMaxLevel());
                net.minecraft.server.v1_5_R1.ItemStack itemstack = Item.ENCHANTED_BOOK.a(new net.minecraft.server.v1_5_R1.EnchantmentInstance(enchantment, i1));
                int k1 = 2 + random.nextInt(5 + i1 * 10) + 3 * i1;
                merchantrecipelist.add(new net.minecraft.server.v1_5_R1.MerchantRecipe(new net.minecraft.server.v1_5_R1.ItemStack(Item.BOOK), new net.minecraft.server.v1_5_R1.ItemStack(Item.EMERALD, k1), itemstack));
            }
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
            int j1 = ai1.length;
            for(int l1 = 0; l1 < j1; l1++)
            {
                int i2 = ai1[l1];
                if(random.nextFloat() < j(0.05F))
                    merchantrecipelist.add(new net.minecraft.server.v1_5_R1.MerchantRecipe(new net.minecraft.server.v1_5_R1.ItemStack(i2, 1, 0), new net.minecraft.server.v1_5_R1.ItemStack(Item.EMERALD, 2 + random.nextInt(3), 0), EnchantmentManager.a(random, new net.minecraft.server.v1_5_R1.ItemStack(i2, 1, 0), 5 + random.nextInt(15))));
            }

            break;
        }
        if(merchantrecipelist.isEmpty())
            a(merchantrecipelist, Item.GOLD_INGOT.id, random, 1.0F);
        Collections.shuffle(merchantrecipelist);
        if(i == null)
            i = new net.minecraft.server.v1_5_R1.MerchantRecipeList();
        for(int l = 0; l < k && l < merchantrecipelist.size(); l++)
            i.a((net.minecraft.server.v1_5_R1.MerchantRecipe)merchantrecipelist.get(l));

    }

    private static void a(net.minecraft.server.v1_5_R1.MerchantRecipeList merchantrecipelist, int k, Random random, float f1)
    {
        if(random.nextFloat() < f1)
            merchantrecipelist.add(new net.minecraft.server.v1_5_R1.MerchantRecipe(a(k, random), Item.EMERALD));
    }

    private static net.minecraft.server.v1_5_R1.ItemStack a(int k, Random random)
    {
        return new net.minecraft.server.v1_5_R1.ItemStack(k, b(k, random), 0);
    }

    private static int b(int k, Random random)
    {
        net.minecraft.server.v1_5_R1.Tuple tuple = (net.minecraft.server.v1_5_R1.Tuple)bP.get(Integer.valueOf(k));
        if(tuple == null)
            return 1;
        if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
            return ((Integer)tuple.a()).intValue();
        else
            return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
    }

    private static void b(net.minecraft.server.v1_5_R1.MerchantRecipeList merchantrecipelist, int k, Random random, float f1)
    {
        if(random.nextFloat() < f1)
        {
            int l = c(k, random);
            net.minecraft.server.v1_5_R1.ItemStack itemstack;
            net.minecraft.server.v1_5_R1.ItemStack itemstack1;
            if(l < 0)
            {
                itemstack = new net.minecraft.server.v1_5_R1.ItemStack(Item.EMERALD.id, 1, 0);
                itemstack1 = new net.minecraft.server.v1_5_R1.ItemStack(k, -l, 0);
            } else
            {
                itemstack = new net.minecraft.server.v1_5_R1.ItemStack(Item.EMERALD.id, l, 0);
                itemstack1 = new net.minecraft.server.v1_5_R1.ItemStack(k, 1, 0);
            }
            merchantrecipelist.add(new net.minecraft.server.v1_5_R1.MerchantRecipe(itemstack, itemstack1));
        }
    }

    private static int c(int k, Random random)
    {
        net.minecraft.server.v1_5_R1.Tuple tuple = (net.minecraft.server.v1_5_R1.Tuple)bQ.get(Integer.valueOf(k));
        if(tuple == null)
            return 1;
        if(((Integer)tuple.a()).intValue() >= ((Integer)tuple.b()).intValue())
            return ((Integer)tuple.a()).intValue();
        else
            return ((Integer)tuple.a()).intValue() + random.nextInt(((Integer)tuple.b()).intValue() - ((Integer)tuple.a()).intValue());
    }

    public void bJ()
    {
        setProfession(world.random.nextInt(5));
    }

    public void q()
    {
        bN = true;
    }

    public EntityVillager b(net.minecraft.server.v1_5_R1.EntityAgeable entityageable)
    {
        EntityVillager entityvillager = new EntityVillager(world);
        entityvillager.bJ();
        return entityvillager;
    }

    public net.minecraft.server.v1_5_R1.EntityAgeable createChild(net.minecraft.server.v1_5_R1.EntityAgeable entityageable)
    {
        return b(entityageable);
    }

    private int profession;
    private boolean f;
    private boolean g;
    net.minecraft.server.v1_5_R1.Village village;
    private net.minecraft.server.v1_5_R1.EntityHuman h;
    private net.minecraft.server.v1_5_R1.MerchantRecipeList i;
    private int j;
    private boolean bK;
    private int bL;
    private String bM;
    private boolean bN;
    private float bO;
    private static final Map bP;
    private static final Map bQ;

    static 
    {
        bP = new HashMap();
        bQ = new HashMap();
        bP.put(Integer.valueOf(Item.COAL.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(16), Integer.valueOf(24)));
        bP.put(Integer.valueOf(Item.IRON_INGOT.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bP.put(Integer.valueOf(Item.GOLD_INGOT.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bP.put(Integer.valueOf(Item.DIAMOND.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bP.put(Integer.valueOf(Item.PAPER.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(24), Integer.valueOf(36)));
        bP.put(Integer.valueOf(Item.BOOK.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(11), Integer.valueOf(13)));
        bP.put(Integer.valueOf(Item.WRITTEN_BOOK.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        bP.put(Integer.valueOf(Item.ENDER_PEARL.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bP.put(Integer.valueOf(Item.EYE_OF_ENDER.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(2), Integer.valueOf(3)));
        bP.put(Integer.valueOf(Item.PORK.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bP.put(Integer.valueOf(Item.RAW_BEEF.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bP.put(Integer.valueOf(Item.RAW_CHICKEN.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(14), Integer.valueOf(18)));
        bP.put(Integer.valueOf(Item.COOKED_FISH.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(9), Integer.valueOf(13)));
        bP.put(Integer.valueOf(Item.SEEDS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(34), Integer.valueOf(48)));
        bP.put(Integer.valueOf(Item.MELON_SEEDS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(30), Integer.valueOf(38)));
        bP.put(Integer.valueOf(Item.PUMPKIN_SEEDS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(30), Integer.valueOf(38)));
        bP.put(Integer.valueOf(Item.WHEAT.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(18), Integer.valueOf(22)));
        bP.put(Integer.valueOf(Block.WOOL.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(14), Integer.valueOf(22)));
        bP.put(Integer.valueOf(Item.ROTTEN_FLESH.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(36), Integer.valueOf(64)));
        bQ.put(Integer.valueOf(Item.FLINT_AND_STEEL.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bQ.put(Integer.valueOf(Item.SHEARS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bQ.put(Integer.valueOf(Item.IRON_SWORD.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(7), Integer.valueOf(11)));
        bQ.put(Integer.valueOf(Item.DIAMOND_SWORD.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(12), Integer.valueOf(14)));
        bQ.put(Integer.valueOf(Item.IRON_AXE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(6), Integer.valueOf(8)));
        bQ.put(Integer.valueOf(Item.DIAMOND_AXE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(9), Integer.valueOf(12)));
        bQ.put(Integer.valueOf(Item.IRON_PICKAXE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(7), Integer.valueOf(9)));
        bQ.put(Integer.valueOf(Item.DIAMOND_PICKAXE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bQ.put(Integer.valueOf(Item.IRON_SPADE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bQ.put(Integer.valueOf(Item.DIAMOND_SPADE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bQ.put(Integer.valueOf(Item.IRON_HOE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bQ.put(Integer.valueOf(Item.DIAMOND_HOE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bQ.put(Integer.valueOf(Item.IRON_BOOTS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bQ.put(Integer.valueOf(Item.DIAMOND_BOOTS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bQ.put(Integer.valueOf(Item.IRON_HELMET.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(4), Integer.valueOf(6)));
        bQ.put(Integer.valueOf(Item.DIAMOND_HELMET.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(7), Integer.valueOf(8)));
        bQ.put(Integer.valueOf(Item.IRON_CHESTPLATE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(10), Integer.valueOf(14)));
        bQ.put(Integer.valueOf(Item.DIAMOND_CHESTPLATE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(16), Integer.valueOf(19)));
        bQ.put(Integer.valueOf(Item.IRON_LEGGINGS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(8), Integer.valueOf(10)));
        bQ.put(Integer.valueOf(Item.DIAMOND_LEGGINGS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(11), Integer.valueOf(14)));
        bQ.put(Integer.valueOf(Item.CHAINMAIL_BOOTS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(5), Integer.valueOf(7)));
        bQ.put(Integer.valueOf(Item.CHAINMAIL_HELMET.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(5), Integer.valueOf(7)));
        bQ.put(Integer.valueOf(Item.CHAINMAIL_CHESTPLATE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(11), Integer.valueOf(15)));
        bQ.put(Integer.valueOf(Item.CHAINMAIL_LEGGINGS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(9), Integer.valueOf(11)));
        bQ.put(Integer.valueOf(Item.BREAD.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-4), Integer.valueOf(-2)));
        bQ.put(Integer.valueOf(Item.MELON.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        bQ.put(Integer.valueOf(Item.APPLE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        bQ.put(Integer.valueOf(Item.COOKIE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-10), Integer.valueOf(-7)));
        bQ.put(Integer.valueOf(Block.GLASS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-5), Integer.valueOf(-3)));
        bQ.put(Integer.valueOf(Block.BOOKSHELF.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        bQ.put(Integer.valueOf(Item.LEATHER_CHESTPLATE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(4), Integer.valueOf(5)));
        bQ.put(Integer.valueOf(Item.LEATHER_BOOTS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bQ.put(Integer.valueOf(Item.LEATHER_HELMET.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bQ.put(Integer.valueOf(Item.LEATHER_LEGGINGS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(2), Integer.valueOf(4)));
        bQ.put(Integer.valueOf(Item.SADDLE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(6), Integer.valueOf(8)));
        bQ.put(Integer.valueOf(Item.EXP_BOTTLE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-4), Integer.valueOf(-1)));
        bQ.put(Integer.valueOf(Item.REDSTONE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-4), Integer.valueOf(-1)));
        bQ.put(Integer.valueOf(Item.COMPASS.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bQ.put(Integer.valueOf(Item.WATCH.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(10), Integer.valueOf(12)));
        bQ.put(Integer.valueOf(Block.GLOWSTONE.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-3), Integer.valueOf(-1)));
        bQ.put(Integer.valueOf(Item.GRILLED_PORK.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-7), Integer.valueOf(-5)));
        bQ.put(Integer.valueOf(Item.COOKED_BEEF.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-7), Integer.valueOf(-5)));
        bQ.put(Integer.valueOf(Item.COOKED_CHICKEN.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-8), Integer.valueOf(-6)));
        bQ.put(Integer.valueOf(Item.EYE_OF_ENDER.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(7), Integer.valueOf(11)));
        bQ.put(Integer.valueOf(Item.ARROW.id), new net.minecraft.server.v1_5_R1.Tuple(Integer.valueOf(-12), Integer.valueOf(-8)));
    }
}
