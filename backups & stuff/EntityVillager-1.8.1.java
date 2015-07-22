// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   EntityVillager.java

package net.minecraft.server.v1_8_R1;

import java.util.*;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftVillager;

public class EntityVillager extends EntityAgeable
    implements NPC, IMerchant
{

    public EntityVillager(World world)
    {
        this(world, 0);
    }

    public EntityVillager(World world, int i)
    {
        super(world);
        inventory = new InventorySubcontainer("Items", false, 8, (CraftVillager)getBukkitEntity());
        setProfession(i);
        a(0.6F, 1.8F);
        ((Navigation)getNavigation()).b(true);
        ((Navigation)getNavigation()).a(true);
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalAvoidTarget(this, new EntityVillagerInnerClass1(this), 8F, 0.59999999999999998D, 0.59999999999999998D));
        goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
        goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
        goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
        goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
        goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
        goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.59999999999999998D));
        goalSelector.a(6, new PathfinderGoalMakeLove(this));
        goalSelector.a(7, new PathfinderGoalTakeFlower(this));
        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/v1_8_R1/EntityHuman, 3F, 1.0F));
        goalSelector.a(9, new PathfinderGoalInteractVillagers(this));
        goalSelector.a(9, new PathfinderGoalRandomStroll(this, 0.59999999999999998D));
        goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, net/minecraft/server/v1_8_R1/EntityInsentient, 8F));
        j(true);
    }

    private void ct()
    {
        if(!by)
        {
            by = true;
            if(isBaby())
                goalSelector.a(8, new PathfinderGoalPlay(this, 0.32000000000000001D));
            else
            if(getProfession() == 0)
                goalSelector.a(6, new PathfinderGoalVillagerFarm(this, 0.59999999999999998D));
        }
    }

    protected void n()
    {
        if(getProfession() == 0)
            goalSelector.a(8, new PathfinderGoalVillagerFarm(this, 0.59999999999999998D));
        super.n();
    }

    protected void aW()
    {
        super.aW();
        getAttributeInstance(GenericAttributes.d).setValue(0.5D);
    }

    protected void E()
    {
        if(--profession <= 0)
        {
            BlockPosition blockposition = new BlockPosition(this);
            world.ae().a(blockposition);
            profession = 70 + random.nextInt(50);
            village = world.ae().getClosestVillage(blockposition, 32);
            if(village == null)
            {
                ch();
            } else
            {
                BlockPosition blockposition1 = village.a();
                a(blockposition1, (int)((float)village.b() * 1.0F));
                if(bx)
                {
                    bx = false;
                    village.b(5);
                }
            }
        }
        if(!cm() && bq > 0)
        {
            bq--;
            if(bq <= 0)
            {
                if(br)
                {
                    for(Iterator iterator = bp.iterator(); iterator.hasNext();)
                    {
                        MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();
                        if(merchantrecipe.h())
                            merchantrecipe.a(random.nextInt(6) + random.nextInt(6) + 2);
                    }

                    cu();
                    br = false;
                    if(village != null && bu != null)
                    {
                        world.broadcastEntityEffect(this, (byte)14);
                        village.a(bu, 1);
                    }
                }
                addEffect(new MobEffect(MobEffectList.REGENERATION.id, 200, 0));
            }
        }
        super.E();
    }

    public boolean a(EntityHuman entityhuman)
    {
        ItemStack itemstack = entityhuman.inventory.getItemInHand();
        boolean flag = itemstack != null && itemstack.getItem() == Items.SPAWN_EGG;
        if(!flag && isAlive() && !cm() && !isBaby())
        {
            if(!world.isStatic && (bp == null || bp.size() > 0))
            {
                a_(entityhuman);
                entityhuman.openTrade(this);
            }
            entityhuman.b(StatisticList.F);
            return true;
        } else
        {
            return super.a(entityhuman);
        }
    }

    protected void h()
    {
        super.h();
        datawatcher.a(16, Integer.valueOf(0));
    }

    public void b(NBTTagCompound nbttagcompound)
    {
        super.b(nbttagcompound);
        nbttagcompound.setInt("Profession", getProfession());
        nbttagcompound.setInt("Riches", riches);
        nbttagcompound.setInt("Career", bv);
        nbttagcompound.setInt("CareerLevel", bw);
        nbttagcompound.setBoolean("Willing", bs);
        if(bp != null)
            nbttagcompound.set("Offers", bp.a());
        NBTTagList nbttaglist = new NBTTagList();
        for(int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack itemstack = inventory.getItem(i);
            if(itemstack != null)
                nbttaglist.add(itemstack.save(new NBTTagCompound()));
        }

        nbttagcompound.set("Inventory", nbttaglist);
    }

    public void a(NBTTagCompound nbttagcompound)
    {
        super.a(nbttagcompound);
        setProfession(nbttagcompound.getInt("Profession"));
        riches = nbttagcompound.getInt("Riches");
        bv = nbttagcompound.getInt("Career");
        bw = nbttagcompound.getInt("CareerLevel");
        bs = nbttagcompound.getBoolean("Willing");
        if(nbttagcompound.hasKeyOfType("Offers", 10))
        {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Offers");
            bp = new MerchantRecipeList(nbttagcompound1);
        }
        NBTTagList nbttaglist = nbttagcompound.getList("Inventory", 10);
        for(int i = 0; i < nbttaglist.size(); i++)
        {
            ItemStack itemstack = ItemStack.createStack(nbttaglist.get(i));
            if(itemstack != null)
                inventory.a(itemstack);
        }

        j(true);
        ct();
    }

    protected boolean isTypeNotPersistent()
    {
        return false;
    }

    protected String z()
    {
        return cm() ? "mob.villager.haggle" : "mob.villager.idle";
    }

    protected String bn()
    {
        return "mob.villager.hit";
    }

    protected String bo()
    {
        return "mob.villager.death";
    }

    public void setProfession(int i)
    {
        datawatcher.watch(16, Integer.valueOf(i));
    }

    public int getProfession()
    {
        return Math.max(datawatcher.getInt(16) % 5, 0);
    }

    public boolean ck()
    {
        return bm;
    }

    public void l(boolean flag)
    {
        bm = flag;
    }

    public void m(boolean flag)
    {
        bn = flag;
    }

    public boolean cl()
    {
        return bn;
    }

    public void b(EntityLiving entityliving)
    {
        super.b(entityliving);
        if(village != null && entityliving != null)
        {
            village.a(entityliving);
            if(entityliving instanceof EntityHuman)
            {
                byte b0 = -1;
                if(isBaby())
                    b0 = -3;
                village.a(entityliving.getName(), b0);
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

    public EntityHuman u_()
    {
        return tradingPlayer;
    }

    public boolean cm()
    {
        return tradingPlayer != null;
    }

    public boolean n(boolean flag)
    {
        if(!bs && flag && cp())
        {
            boolean flag1 = false;
            for(int i = 0; i < inventory.getSize(); i++)
            {
                ItemStack itemstack = inventory.getItem(i);
                if(itemstack != null)
                    if(itemstack.getItem() == Items.BREAD && itemstack.count >= 3)
                    {
                        flag1 = true;
                        inventory.splitStack(i, 3);
                    } else
                    if((itemstack.getItem() == Items.POTATO || itemstack.getItem() == Items.CARROT) && itemstack.count >= 12)
                    {
                        flag1 = true;
                        inventory.splitStack(i, 12);
                    }
                if(!flag1)
                    continue;
                world.broadcastEntityEffect(this, (byte)18);
                bs = true;
                break;
            }

        }
        return bs;
    }

    public void o(boolean flag)
    {
        bs = flag;
    }

    public void a(MerchantRecipe merchantrecipe)
    {
        merchantrecipe.g();
        a_ = -w();
        makeSound("mob.villager.yes", bA(), bB());
        int i = 3 + random.nextInt(4);
        if(merchantrecipe.e() == 1 || random.nextInt(5) == 0)
        {
            bq = 40;
            br = true;
            bs = true;
            if(tradingPlayer != null)
                bu = tradingPlayer.getName();
            else
                bu = null;
            i += 5;
        }
        if(merchantrecipe.getBuyItem1().getItem() == Items.EMERALD)
            riches += merchantrecipe.getBuyItem1().count;
        if(merchantrecipe.j())
            world.addEntity(new EntityExperienceOrb(world, locX, locY + 0.5D, locZ, i));
    }

    public void a_(ItemStack itemstack)
    {
        if(!world.isStatic && a_ > -w() + 20)
        {
            a_ = -w();
            if(itemstack != null)
                makeSound("mob.villager.yes", bA(), bB());
            else
                makeSound("mob.villager.no", bA(), bB());
        }
    }

    public MerchantRecipeList getOffers(EntityHuman entityhuman)
    {
        if(bp == null)
            cu();
        return bp;
    }

    private void cu()
    {
        IMerchantRecipeOption aimerchantrecipeoption[][][] = bA[getProfession()];
        if(bv != 0 && bw != 0)
        {
            bw++;
        } else
        {
            bv = random.nextInt(aimerchantrecipeoption.length) + 1;
            bw = 1;
        }
        if(bp == null)
            bp = new MerchantRecipeList();
        int i = bv - 1;
        int j = bw - 1;
        IMerchantRecipeOption aimerchantrecipeoption1[][] = aimerchantrecipeoption[i];
        if(j < aimerchantrecipeoption1.length)
        {
            IMerchantRecipeOption aimerchantrecipeoption2[] = aimerchantrecipeoption1[j];
            IMerchantRecipeOption aimerchantrecipeoption3[] = aimerchantrecipeoption2;
            int k = aimerchantrecipeoption2.length;
            for(int l = 0; l < k; l++)
            {
                IMerchantRecipeOption imerchantrecipeoption = aimerchantrecipeoption3[l];
                imerchantrecipeoption.a(bp, random);
            }

        }
    }

    public IChatBaseComponent getScoreboardDisplayName()
    {
        String s = getCustomName();
        if(s != null && s.length() > 0)
            return new ChatComponentText(s);
        if(bp == null)
            cu();
        String s1 = null;
        switch(getProfession())
        {
        case 0: // '\0'
            if(bv == 1)
                s1 = "farmer";
            else
            if(bv == 2)
                s1 = "fisherman";
            else
            if(bv == 3)
                s1 = "shepherd";
            else
            if(bv == 4)
                s1 = "fletcher";
            break;

        case 1: // '\001'
            s1 = "librarian";
            break;

        case 2: // '\002'
            s1 = "cleric";
            break;

        case 3: // '\003'
            if(bv == 1)
                s1 = "armor";
            else
            if(bv == 2)
                s1 = "weapon";
            else
            if(bv == 3)
                s1 = "tool";
            break;

        case 4: // '\004'
            if(bv == 1)
                s1 = "butcher";
            else
            if(bv == 2)
                s1 = "leather";
            break;
        }
        if(s1 != null)
        {
            ChatMessage chatmessage = new ChatMessage((new StringBuilder("entity.Villager.")).append(s1).toString(), new Object[0]);
            chatmessage.getChatModifier().setChatHoverable(aP());
            chatmessage.getChatModifier().setInsertion(getUniqueID().toString());
            return chatmessage;
        } else
        {
            return super.getScoreboardDisplayName();
        }
    }

    public float getHeadHeight()
    {
        float f = 1.62F;
        if(isBaby())
            f = (float)((double)f - 0.81000000000000005D);
        return f;
    }

    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, GroupDataEntity groupdataentity)
    {
        groupdataentity = super.prepare(difficultydamagescaler, groupdataentity);
        setProfession(world.random.nextInt(5));
        ct();
        return groupdataentity;
    }

    public void cn()
    {
        bx = true;
    }

    public EntityVillager b(EntityAgeable entityageable)
    {
        EntityVillager entityvillager = new EntityVillager(world);
        entityvillager.prepare(world.E(new BlockPosition(entityvillager)), null);
        return entityvillager;
    }

    public boolean ca()
    {
        return false;
    }

    public void onLightningStrike(EntityLightning entitylightning)
    {
        if(!world.isStatic)
        {
            EntityWitch entitywitch = new EntityWitch(world);
            entitywitch.setPositionRotation(locX, locY, locZ, yaw, pitch);
            entitywitch.prepare(world.E(new BlockPosition(entitywitch)), null);
            world.addEntity(entitywitch);
            die();
        }
    }

    public InventorySubcontainer co()
    {
        return inventory;
    }

    protected void a(EntityItem entityitem)
    {
        ItemStack itemstack = entityitem.getItemStack();
        Item item = itemstack.getItem();
        if(a(item))
        {
            ItemStack itemstack1 = inventory.a(itemstack);
            if(itemstack1 == null)
                entityitem.die();
            else
                itemstack.count = itemstack1.count;
        }
    }

    private boolean a(Item item)
    {
        return item == Items.BREAD || item == Items.POTATO || item == Items.CARROT || item == Items.WHEAT || item == Items.WHEAT_SEEDS;
    }

    public boolean cp()
    {
        return s(1);
    }

    public boolean cq()
    {
        return s(2);
    }

    public boolean cr()
    {
        boolean flag = getProfession() == 0;
        return flag ? !s(5) : !s(1);
    }

    private boolean s(int i)
    {
        boolean flag = getProfession() == 0;
        for(int j = 0; j < inventory.getSize(); j++)
        {
            ItemStack itemstack = inventory.getItem(j);
            if(itemstack != null)
            {
                if(itemstack.getItem() == Items.BREAD && itemstack.count >= 3 * i || itemstack.getItem() == Items.POTATO && itemstack.count >= 12 * i || itemstack.getItem() == Items.CARROT && itemstack.count >= 12 * i)
                    return true;
                if(flag && itemstack.getItem() == Items.WHEAT && itemstack.count >= 9 * i)
                    return true;
            }
        }

        return false;
    }

    public boolean cs()
    {
        for(int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack itemstack = inventory.getItem(i);
            if(itemstack != null && (itemstack.getItem() == Items.WHEAT_SEEDS || itemstack.getItem() == Items.POTATO || itemstack.getItem() == Items.CARROT))
                return true;
        }

        return false;
    }

    public boolean d(int i, ItemStack itemstack)
    {
        if(super.d(i, itemstack))
            return true;
        int j = i - 300;
        if(j >= 0 && j < inventory.getSize())
        {
            inventory.setItem(j, itemstack);
            return true;
        } else
        {
            return false;
        }
    }

    public EntityAgeable createChild(EntityAgeable entityageable)
    {
        return b(entityageable);
    }

    private int profession;
    private boolean bm;
    private boolean bn;
    Village village;
    private EntityHuman tradingPlayer;
    private MerchantRecipeList bp;
    private int bq;
    private boolean br;
    private boolean bs;
    private int riches;
    private String bu;
    private int bv;
    private int bw;
    private boolean bx;
    private boolean by;
    public InventorySubcontainer inventory;
    private static final IMerchantRecipeOption bA[][][][];

    static 
    {
        bA = (new IMerchantRecipeOption[][][][] {
            new IMerchantRecipeOption[][][] {
                new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.WHEAT, new MerchantOptionRandomRange(18, 22)), new MerchantRecipeOptionBuy(Items.POTATO, new MerchantOptionRandomRange(15, 19)), new MerchantRecipeOptionBuy(Items.CARROT, new MerchantOptionRandomRange(15, 19)), new MerchantRecipeOptionSell(Items.BREAD, new MerchantOptionRandomRange(-4, -2))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Item.getItemOf(Blocks.PUMPKIN), new MerchantOptionRandomRange(8, 13)), new MerchantRecipeOptionSell(Items.PUMPKIN_PIE, new MerchantOptionRandomRange(-3, -2))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Item.getItemOf(Blocks.MELON_BLOCK), new MerchantOptionRandomRange(7, 12)), new MerchantRecipeOptionSell(Items.APPLE, new MerchantOptionRandomRange(-5, -7))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.COOKIE, new MerchantOptionRandomRange(-6, -10)), new MerchantRecipeOptionSell(Items.CAKE, new MerchantOptionRandomRange(1, 1))
                    }
                }, new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.STRING, new MerchantOptionRandomRange(15, 20)), new MerchantRecipeOptionBuy(Items.COAL, new MerchantOptionRandomRange(16, 24)), new MerchantRecipeOptionProcess(Items.FISH, new MerchantOptionRandomRange(6, 6), Items.COOKED_FISH, new MerchantOptionRandomRange(6, 6))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionEnchant(Items.FISHING_ROD, new MerchantOptionRandomRange(7, 8))
                    }
                }, new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Item.getItemOf(Blocks.WOOL), new MerchantOptionRandomRange(16, 22)), new MerchantRecipeOptionSell(Items.SHEARS, new MerchantOptionRandomRange(3, 4))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 0), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 1), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 2), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 3), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 4), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 5), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 6), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 7), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 8), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 9), new MerchantOptionRandomRange(1, 2)), 
                        new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 10), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 11), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 12), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 13), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 14), new MerchantOptionRandomRange(1, 2)), new MerchantRecipeOptionSell(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, 15), new MerchantOptionRandomRange(1, 2))
                    }
                }, new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.STRING, new MerchantOptionRandomRange(15, 20)), new MerchantRecipeOptionSell(Items.ARROW, new MerchantOptionRandomRange(-12, -8))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.BOW, new MerchantOptionRandomRange(2, 3)), new MerchantRecipeOptionProcess(Item.getItemOf(Blocks.GRAVEL), new MerchantOptionRandomRange(10, 10), Items.FLINT, new MerchantOptionRandomRange(6, 10))
                    }
                }
            }, new IMerchantRecipeOption[][][] {
                new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.PAPER, new MerchantOptionRandomRange(24, 36)), new MerchantRecipeOptionBook()
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.BOOK, new MerchantOptionRandomRange(8, 10)), new MerchantRecipeOptionSell(Items.COMPASS, new MerchantOptionRandomRange(10, 12)), new MerchantRecipeOptionSell(Item.getItemOf(Blocks.BOOKSHELF), new MerchantOptionRandomRange(3, 4))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.WRITTEN_BOOK, new MerchantOptionRandomRange(2, 2)), new MerchantRecipeOptionSell(Items.CLOCK, new MerchantOptionRandomRange(10, 12)), new MerchantRecipeOptionSell(Item.getItemOf(Blocks.GLASS), new MerchantOptionRandomRange(-5, -3))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBook()
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBook()
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.NAME_TAG, new MerchantOptionRandomRange(20, 22))
                    }
                }
            }, new IMerchantRecipeOption[][][] {
                new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.ROTTEN_FLESH, new MerchantOptionRandomRange(36, 40)), new MerchantRecipeOptionBuy(Items.GOLD_INGOT, new MerchantOptionRandomRange(8, 10))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.REDSTONE, new MerchantOptionRandomRange(-4, -1)), new MerchantRecipeOptionSell(new ItemStack(Items.DYE, 1, EnumColor.BLUE.getInvColorIndex()), new MerchantOptionRandomRange(-2, -1))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.ENDER_EYE, new MerchantOptionRandomRange(7, 11)), new MerchantRecipeOptionSell(Item.getItemOf(Blocks.GLOWSTONE), new MerchantOptionRandomRange(-3, -1))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.EXPERIENCE_BOTTLE, new MerchantOptionRandomRange(3, 11))
                    }
                }
            }, new IMerchantRecipeOption[][][] {
                new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.COAL, new MerchantOptionRandomRange(16, 24)), new MerchantRecipeOptionSell(Items.IRON_HELMET, new MerchantOptionRandomRange(4, 6))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.IRON_INGOT, new MerchantOptionRandomRange(7, 9)), new MerchantRecipeOptionSell(Items.IRON_CHESTPLATE, new MerchantOptionRandomRange(10, 14))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.DIAMOND, new MerchantOptionRandomRange(3, 4)), new MerchantRecipeOptionEnchant(Items.DIAMOND_CHESTPLATE, new MerchantOptionRandomRange(16, 19))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.CHAINMAIL_BOOTS, new MerchantOptionRandomRange(5, 7)), new MerchantRecipeOptionSell(Items.CHAINMAIL_LEGGINGS, new MerchantOptionRandomRange(9, 11)), new MerchantRecipeOptionSell(Items.CHAINMAIL_HELMET, new MerchantOptionRandomRange(5, 7)), new MerchantRecipeOptionSell(Items.CHAINMAIL_CHESTPLATE, new MerchantOptionRandomRange(11, 15))
                    }
                }, new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.COAL, new MerchantOptionRandomRange(16, 24)), new MerchantRecipeOptionSell(Items.IRON_AXE, new MerchantOptionRandomRange(6, 8))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.IRON_INGOT, new MerchantOptionRandomRange(7, 9)), new MerchantRecipeOptionEnchant(Items.IRON_SWORD, new MerchantOptionRandomRange(9, 10))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.DIAMOND, new MerchantOptionRandomRange(3, 4)), new MerchantRecipeOptionEnchant(Items.DIAMOND_SWORD, new MerchantOptionRandomRange(12, 15)), new MerchantRecipeOptionEnchant(Items.DIAMOND_AXE, new MerchantOptionRandomRange(9, 12))
                    }
                }, new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.COAL, new MerchantOptionRandomRange(16, 24)), new MerchantRecipeOptionEnchant(Items.IRON_SHOVEL, new MerchantOptionRandomRange(5, 7))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.IRON_INGOT, new MerchantOptionRandomRange(7, 9)), new MerchantRecipeOptionEnchant(Items.IRON_PICKAXE, new MerchantOptionRandomRange(9, 11))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.DIAMOND, new MerchantOptionRandomRange(3, 4)), new MerchantRecipeOptionEnchant(Items.DIAMOND_PICKAXE, new MerchantOptionRandomRange(12, 15))
                    }
                }
            }, new IMerchantRecipeOption[][][] {
                new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.PORKCHOP, new MerchantOptionRandomRange(14, 18)), new MerchantRecipeOptionBuy(Items.CHICKEN, new MerchantOptionRandomRange(14, 18))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.COAL, new MerchantOptionRandomRange(16, 24)), new MerchantRecipeOptionSell(Items.COOKED_PORKCHOP, new MerchantOptionRandomRange(-7, -5)), new MerchantRecipeOptionSell(Items.COOKED_CHICKEN, new MerchantOptionRandomRange(-8, -6))
                    }
                }, new IMerchantRecipeOption[][] {
                    new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionBuy(Items.LEATHER, new MerchantOptionRandomRange(9, 12)), new MerchantRecipeOptionSell(Items.LEATHER_LEGGINGS, new MerchantOptionRandomRange(2, 4))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionEnchant(Items.LEATHER_CHESTPLATE, new MerchantOptionRandomRange(7, 12))
                    }, new IMerchantRecipeOption[] {
                        new MerchantRecipeOptionSell(Items.SADDLE, new MerchantOptionRandomRange(8, 10))
                    }
                }
            }
        });
    }
}
