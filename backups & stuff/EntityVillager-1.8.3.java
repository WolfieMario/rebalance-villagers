// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   EntityVillager.java

package net.minecraft.server.v1_8_R2;

import java.util.*;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftVillager;

public class EntityVillager extends EntityAgeable
    implements IMerchant, NPC
{
    static interface IMerchantRecipeOption
    {

        public abstract void a(MerchantRecipeList merchantrecipelist, Random random);
    }

    static class MerchantOptionRandomRange extends Tuple
    {

        public int a(Random random)
        {
            return ((Integer)a()).intValue() < ((Integer)b()).intValue() ? ((Integer)a()).intValue() + random.nextInt((((Integer)b()).intValue() - ((Integer)a()).intValue()) + 1) : ((Integer)a()).intValue();
        }

        public MerchantOptionRandomRange(int i, int j)
        {
            super(Integer.valueOf(i), Integer.valueOf(j));
        }
    }

    static class MerchantRecipeOptionBook
        implements IMerchantRecipeOption
    {

        public void a(MerchantRecipeList merchantrecipelist, Random random)
        {
            Enchantment enchantment = Enchantment.b[random.nextInt(Enchantment.b.length)];
            int i = MathHelper.nextInt(random, enchantment.getStartLevel(), enchantment.getMaxLevel());
            ItemStack itemstack = Items.ENCHANTED_BOOK.a(new WeightedRandomEnchant(enchantment, i));
            int j = 2 + random.nextInt(5 + i * 10) + 3 * i;
            if(j > 64)
                j = 64;
            merchantrecipelist.add(new MerchantRecipe(new ItemStack(Items.BOOK), new ItemStack(Items.EMERALD, j), itemstack));
        }

        public MerchantRecipeOptionBook()
        {
        }
    }

    static class MerchantRecipeOptionBuy
        implements IMerchantRecipeOption
    {

        public void a(MerchantRecipeList merchantrecipelist, Random random)
        {
            int i = 1;
            if(b != null)
                i = b.a(random);
            merchantrecipelist.add(new MerchantRecipe(new ItemStack(a, i, 0), Items.EMERALD));
        }

        public Item a;
        public MerchantOptionRandomRange b;

        public MerchantRecipeOptionBuy(Item item, MerchantOptionRandomRange entityvillager_merchantoptionrandomrange)
        {
            a = item;
            b = entityvillager_merchantoptionrandomrange;
        }
    }

    static class MerchantRecipeOptionEnchant
        implements IMerchantRecipeOption
    {

        public void a(MerchantRecipeList merchantrecipelist, Random random)
        {
            int i = 1;
            if(b != null)
                i = b.a(random);
            ItemStack itemstack = new ItemStack(Items.EMERALD, i, 0);
            ItemStack itemstack1 = new ItemStack(a.getItem(), 1, a.getData());
            itemstack1 = EnchantmentManager.a(random, itemstack1, 5 + random.nextInt(15));
            merchantrecipelist.add(new MerchantRecipe(itemstack, itemstack1));
        }

        public ItemStack a;
        public MerchantOptionRandomRange b;

        public MerchantRecipeOptionEnchant(Item item, MerchantOptionRandomRange entityvillager_merchantoptionrandomrange)
        {
            a = new ItemStack(item);
            b = entityvillager_merchantoptionrandomrange;
        }
    }

    static class MerchantRecipeOptionProcess
        implements IMerchantRecipeOption
    {

        public void a(MerchantRecipeList merchantrecipelist, Random random)
        {
            int i = 1;
            if(b != null)
                i = b.a(random);
            int j = 1;
            if(d != null)
                j = d.a(random);
            merchantrecipelist.add(new MerchantRecipe(new ItemStack(a.getItem(), i, a.getData()), new ItemStack(Items.EMERALD), new ItemStack(c.getItem(), j, c.getData())));
        }

        public ItemStack a;
        public MerchantOptionRandomRange b;
        public ItemStack c;
        public MerchantOptionRandomRange d;

        public MerchantRecipeOptionProcess(Item item, MerchantOptionRandomRange entityvillager_merchantoptionrandomrange, Item item1, MerchantOptionRandomRange entityvillager_merchantoptionrandomrange1)
        {
            a = new ItemStack(item);
            b = entityvillager_merchantoptionrandomrange;
            c = new ItemStack(item1);
            d = entityvillager_merchantoptionrandomrange1;
        }
    }

    static class MerchantRecipeOptionSell
        implements IMerchantRecipeOption
    {

        public void a(MerchantRecipeList merchantrecipelist, Random random)
        {
            int i = 1;
            if(b != null)
                i = b.a(random);
            ItemStack itemstack;
            ItemStack itemstack1;
            if(i < 0)
            {
                itemstack = new ItemStack(Items.EMERALD, 1, 0);
                itemstack1 = new ItemStack(a.getItem(), -i, a.getData());
            } else
            {
                itemstack = new ItemStack(Items.EMERALD, i, 0);
                itemstack1 = new ItemStack(a.getItem(), 1, a.getData());
            }
            merchantrecipelist.add(new MerchantRecipe(itemstack, itemstack1));
        }

        public ItemStack a;
        public MerchantOptionRandomRange b;

        public MerchantRecipeOptionSell(Item item, MerchantOptionRandomRange entityvillager_merchantoptionrandomrange)
        {
            a = new ItemStack(item);
            b = entityvillager_merchantoptionrandomrange;
        }

        public MerchantRecipeOptionSell(ItemStack itemstack, MerchantOptionRandomRange entityvillager_merchantoptionrandomrange)
        {
            a = itemstack;
            b = entityvillager_merchantoptionrandomrange;
        }
    }


    public EntityVillager(World world)
    {
        this(world, 0);
    }

    public EntityVillager(World world, int i)
    {
        super(world);
        inventory = new InventorySubcontainer("Items", false, 8, (CraftVillager)getBukkitEntity());
        setProfession(i);
        setSize(0.6F, 1.8F);
        ((Navigation)getNavigation()).b(true);
        ((Navigation)getNavigation()).a(true);
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalAvoidTarget(this, net/minecraft/server/v1_8_R2/EntityZombie, 8F, 0.59999999999999998D, 0.59999999999999998D));
        goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
        goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
        goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
        goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
        goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
        goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.59999999999999998D));
        goalSelector.a(6, new PathfinderGoalMakeLove(this));
        goalSelector.a(7, new PathfinderGoalTakeFlower(this));
        goalSelector.a(9, new PathfinderGoalInteract(this, net/minecraft/server/v1_8_R2/EntityHuman, 3F, 1.0F));
        goalSelector.a(9, new PathfinderGoalInteractVillagers(this));
        goalSelector.a(9, new PathfinderGoalRandomStroll(this, 0.59999999999999998D));
        goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, net/minecraft/server/v1_8_R2/EntityInsentient, 8F));
        j(true);
    }

    private void cv()
    {
        if(!bA)
        {
            bA = true;
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

    protected void initAttributes()
    {
        super.initAttributes();
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
                cj();
            } else
            {
                BlockPosition blockposition1 = village.a();
                a(blockposition1, (int)((float)village.b() * 1.0F));
                if(bz)
                {
                    bz = false;
                    village.b(5);
                }
            }
        }
        if(!co() && bs > 0)
        {
            bs--;
            if(bs <= 0)
            {
                if(bt)
                {
                    for(Iterator iterator = br.iterator(); iterator.hasNext();)
                    {
                        MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();
                        if(merchantrecipe.h())
                            merchantrecipe.a(random.nextInt(6) + random.nextInt(6) + 2);
                    }

                    cw();
                    bt = false;
                    if(village != null && bw != null)
                    {
                        world.broadcastEntityEffect(this, (byte)14);
                        village.a(bw, 1);
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
        if(!flag && isAlive() && !co() && !isBaby())
        {
            if(!world.isClientSide && (br == null || br.size() > 0))
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
        nbttagcompound.setInt("Career", bx);
        nbttagcompound.setInt("CareerLevel", by);
        nbttagcompound.setBoolean("Willing", bu);
        if(br != null)
            nbttagcompound.set("Offers", br.a());
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
        bx = nbttagcompound.getInt("Career");
        by = nbttagcompound.getInt("CareerLevel");
        bu = nbttagcompound.getBoolean("Willing");
        if(nbttagcompound.hasKeyOfType("Offers", 10))
        {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Offers");
            br = new MerchantRecipeList(nbttagcompound1);
        }
        NBTTagList nbttaglist = nbttagcompound.getList("Inventory", 10);
        for(int i = 0; i < nbttaglist.size(); i++)
        {
            ItemStack itemstack = ItemStack.createStack(nbttaglist.get(i));
            if(itemstack != null)
                inventory.a(itemstack);
        }

        j(true);
        cv();
    }

    protected boolean isTypeNotPersistent()
    {
        return false;
    }

    protected String z()
    {
        return co() ? "mob.villager.haggle" : "mob.villager.idle";
    }

    protected String bo()
    {
        return "mob.villager.hit";
    }

    protected String bp()
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

    public boolean cm()
    {
        return bo;
    }

    public void l(boolean flag)
    {
        bo = flag;
    }

    public void m(boolean flag)
    {
        bp = flag;
    }

    public boolean cn()
    {
        return bp;
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

    public EntityHuman v_()
    {
        return tradingPlayer;
    }

    public boolean co()
    {
        return tradingPlayer != null;
    }

    public boolean n(boolean flag)
    {
        if(!bu && flag && cr())
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
                bu = true;
                break;
            }

        }
        return bu;
    }

    public void o(boolean flag)
    {
        bu = flag;
    }

    public void a(MerchantRecipe merchantrecipe)
    {
        merchantrecipe.g();
        a_ = -w();
        makeSound("mob.villager.yes", bB(), bC());
        int i = 3 + random.nextInt(4);
        if(merchantrecipe.e() == 1 || random.nextInt(5) == 0)
        {
            bs = 40;
            bt = true;
            bu = true;
            if(tradingPlayer != null)
                bw = tradingPlayer.getName();
            else
                bw = null;
            i += 5;
        }
        if(merchantrecipe.getBuyItem1().getItem() == Items.EMERALD)
            riches += merchantrecipe.getBuyItem1().count;
        if(merchantrecipe.j())
            world.addEntity(new EntityExperienceOrb(world, locX, locY + 0.5D, locZ, i));
    }

    public void a_(ItemStack itemstack)
    {
        if(!world.isClientSide && a_ > -w() + 20)
        {
            a_ = -w();
            if(itemstack != null)
                makeSound("mob.villager.yes", bB(), bC());
            else
                makeSound("mob.villager.no", bB(), bC());
        }
    }

    public MerchantRecipeList getOffers(EntityHuman entityhuman)
    {
        if(br == null)
            cw();
        return br;
    }

    private void cw()
    {
        IMerchantRecipeOption aentityvillager_imerchantrecipeoption[][][] = bC[getProfession()];
        if(bx != 0 && by != 0)
        {
            by++;
        } else
        {
            bx = random.nextInt(aentityvillager_imerchantrecipeoption.length) + 1;
            by = 1;
        }
        if(br == null)
            br = new MerchantRecipeList();
        int i = bx - 1;
        int j = by - 1;
        IMerchantRecipeOption aentityvillager_imerchantrecipeoption1[][] = aentityvillager_imerchantrecipeoption[i];
        if(j < aentityvillager_imerchantrecipeoption1.length)
        {
            IMerchantRecipeOption aentityvillager_imerchantrecipeoption2[] = aentityvillager_imerchantrecipeoption1[j];
            IMerchantRecipeOption aentityvillager_imerchantrecipeoption3[] = aentityvillager_imerchantrecipeoption2;
            int k = aentityvillager_imerchantrecipeoption2.length;
            for(int l = 0; l < k; l++)
            {
                IMerchantRecipeOption entityvillager_imerchantrecipeoption = aentityvillager_imerchantrecipeoption3[l];
                entityvillager_imerchantrecipeoption.a(br, random);
            }

        }
    }

    public IChatBaseComponent getScoreboardDisplayName()
    {
        String s = getCustomName();
        if(s != null && s.length() > 0)
        {
            ChatComponentText chatcomponenttext = new ChatComponentText(s);
            chatcomponenttext.getChatModifier().setChatHoverable(aQ());
            chatcomponenttext.getChatModifier().setInsertion(getUniqueID().toString());
            return chatcomponenttext;
        }
        if(br == null)
            cw();
        String s1 = null;
        switch(getProfession())
        {
        case 0: // '\0'
            if(bx == 1)
                s1 = "farmer";
            else
            if(bx == 2)
                s1 = "fisherman";
            else
            if(bx == 3)
                s1 = "shepherd";
            else
            if(bx == 4)
                s1 = "fletcher";
            break;

        case 1: // '\001'
            s1 = "librarian";
            break;

        case 2: // '\002'
            s1 = "cleric";
            break;

        case 3: // '\003'
            if(bx == 1)
                s1 = "armor";
            else
            if(bx == 2)
                s1 = "weapon";
            else
            if(bx == 3)
                s1 = "tool";
            break;

        case 4: // '\004'
            if(bx == 1)
                s1 = "butcher";
            else
            if(bx == 2)
                s1 = "leather";
            break;
        }
        if(s1 != null)
        {
            ChatMessage chatmessage = new ChatMessage((new StringBuilder("entity.Villager.")).append(s1).toString(), new Object[0]);
            chatmessage.getChatModifier().setChatHoverable(aQ());
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
        cv();
        return groupdataentity;
    }

    public void cp()
    {
        bz = true;
    }

    public EntityVillager b(EntityAgeable entityageable)
    {
        EntityVillager entityvillager = new EntityVillager(world);
        entityvillager.prepare(world.E(new BlockPosition(entityvillager)), null);
        return entityvillager;
    }

    public boolean cb()
    {
        return false;
    }

    public void onLightningStrike(EntityLightning entitylightning)
    {
        if(!world.isClientSide && !dead)
        {
            EntityWitch entitywitch = new EntityWitch(world);
            entitywitch.setPositionRotation(locX, locY, locZ, yaw, pitch);
            entitywitch.prepare(world.E(new BlockPosition(entitywitch)), null);
            entitywitch.k(ce());
            if(hasCustomName())
            {
                entitywitch.setCustomName(getCustomName());
                entitywitch.setCustomNameVisible(getCustomNameVisible());
            }
            world.addEntity(entitywitch);
            die();
        }
    }

    public InventorySubcontainer cq()
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

    public boolean cr()
    {
        return s(1);
    }

    public boolean cs()
    {
        return s(2);
    }

    public boolean ct()
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

    public boolean cu()
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
    private boolean bo;
    private boolean bp;
    Village village;
    private EntityHuman tradingPlayer;
    private MerchantRecipeList br;
    private int bs;
    private boolean bt;
    private boolean bu;
    private int riches;
    private String bw;
    private int bx;
    private int by;
    private boolean bz;
    private boolean bA;
    public InventorySubcontainer inventory;
    private static final IMerchantRecipeOption bC[][][][];

    static 
    {
        bC = (new IMerchantRecipeOption[][][][] {
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
