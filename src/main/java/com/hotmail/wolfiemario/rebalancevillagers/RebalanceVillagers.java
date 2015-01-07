package com.hotmail.wolfiemario.rebalancevillagers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.v1_8_R1.EntityTypes;
import net.minecraft.server.v1_8_R1.EntityVillager;

import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

/**
 * The Rebalance Villagers plugin's main class.
 * 
 * @author Gerrard Lukacs
 */
public class RebalanceVillagers extends JavaPlugin implements Listener {
    private ConfigLoader configLoader;

    FileConfiguration offerConfig;
    File offerConfigFile;
    static final String OFFER_CONFIG_FILENAME = "offers.yml";
    static final String OFFER_DEFAULT_CONFIG_FILENAME = "offers-default.yml";
    static final String OFFER_VANILLA_CONFIG_FILENAME = "offers-vanilla.yml";

    private static boolean debug = false;
    private static RebalanceVillagers plugin;

    public boolean allowDamage;
    public static final Integer[] DEFAULT_ALLOWED_PROFESSIONS = { 0, 1, 2, 3, 4 };
    public Integer[] allowedProfessions;

    /**
     * Initializes the plugin.
     */
    public RebalanceVillagers() {
        offerConfig = null;
        offerConfigFile = null;
        configLoader = new ConfigLoader(this);
    }

    public void onEnable() {
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		
        RebalanceVillagers.plugin = this;

        // Load config
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) saveDefaultConfig();
        reloadConfig();
        
        // Load offer config
        File offerFile = new File(getDataFolder(), OFFER_CONFIG_FILENAME);
        if (!offerFile.exists()) saveDefaultOfferConfig();
        reloadOfferConfig();
        
        // Save sample offer configs
        saveSampleOfferConfigs();

        try {
            // Replaces default villagers with new villagers
            // Thanks to Icyene for the help with this! Also
            // http://forums.bukkit.org/threads/tutorial-how-to-customize-the-behaviour-of-a-mob-or-entity.54547/
      
            CustomEntityType.registerEntities();

            // Checks if Shopkeepers is running
            try {
                ShopkeepersHelper.connectWithShopkeepers(this);
            } catch (Exception e) {
                getLogger().info("Shopkeepers plugin not found. Ignoring...");
            }

            // Configure Plugin
            configLoader.applyConfig();
            configLoader.applyOfferConfig();

            // Convert existing villagers to balanced villagers
            convertExistingVillagers();
            getLogger().info("Existing villagers have been reloaded as BalancedVillagers.");

            // Registers this as a listener
            getServer().getPluginManager().registerEvents(this, this);
            
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().info("Failed to modify villagers! Plugin is unloading.");
            this.setEnabled(false);
        }
        
    }

    public void onDisable() {
    }
    
    public static void debugMsg(String msg) {
        if (RebalanceVillagers.debug)
             RebalanceVillagers.plugin.getLogger().log(Level.WARNING, msg);
    }

    /**
     * Listens to creatures which are spawned, and kills new EntityVillagers,
     * replacing them with identical BalancedVillagers. <br>
     * Avoids killing Shopkeepers.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getEntity();
        EntityType entityType = event.getEntityType();

        final net.minecraft.server.v1_8_R1.World mcWorld = ((CraftWorld) entity
                .getWorld()).getHandle();
        net.minecraft.server.v1_8_R1.Entity mcEntity = (((CraftEntity) entity)
                .getHandle());

        if (entityType == EntityType.VILLAGER) {
            final EntityVillager entityVil = (EntityVillager) mcEntity;

            // This should only occur if convertVillager triggered this event.
            // This is unnecessary repetition; skip it.
            if (mcEntity instanceof BalancedVillager
                    && event.getSpawnReason().equals(SpawnReason.CUSTOM))
                return;

            if (!Arrays.equals(allowedProfessions, DEFAULT_ALLOWED_PROFESSIONS)) // If
                                                                                 // we're
                                                                                 // not
                                                                                 // on
                                                                                 // the
                                                                                 // default
                                                                                 // set
                                                                                 // of
                                                                                 // professions,
                                                                                 // override
                entityVil
                        .setProfession(allowedProfessions[new java.util.Random()
                                .nextInt(allowedProfessions.length)]); // mouthful

            // Not a BalancedVillager yet!
            if ((mcEntity instanceof BalancedVillager) == false) {
                // Check if this is a Shopkeeper
                if (ShopkeepersHelper.shopkeepersActive()) {
                    Bukkit.getScheduler().runTaskAsynchronously(this, new ShopkeeperWaiter(entityVil, mcWorld, this));
                } else {
                	Bukkit.getScheduler().runTask(this, new Runnable() {
						@Override
						public void run() {
		                    convertVillager(entityVil, mcWorld);
						}
                	});
                }

            }

        }
    }

    /**
     * Listens to entity damage, canceling the event if the target is a
     * BalancedVillager and the plugin is configured to make them invulnerable.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!allowDamage
                && (((CraftEntity) event.getEntity()).getHandle()) instanceof BalancedVillager)
            event.setCancelled(true);
    }

    /**
     * Converts all existing (non-Shopkeeper) villagers in all worlds to
     * BalancedVillagers.
     */
    private void convertExistingVillagers() {
        List<World> worldList = getServer().getWorlds();

        for (World world : worldList) {
            Collection<Villager> villagerList = world
                    .getEntitiesByClass(Villager.class);

            net.minecraft.server.v1_8_R1.World mcWorld = ((CraftWorld) world)
                    .getHandle();

            for (Villager vil : villagerList) {
                // Detect Shopkeepers even on startup!
                if (!ShopkeepersHelper.shopkeepersActive() || !ShopkeepersHelper.isShopkeeper(vil)) {
                    EntityVillager entityVil = (EntityVillager) ((CraftEntity) vil).getHandle();

                    convertVillager(entityVil, mcWorld);
                }
            }
        }
    }

    /**
     * Converts the given villager into a BalancedVillager, leaving an identical
     * villager. Because the previous villager is removed, the new villager will
     * have a different unique ID.
     */
    public void convertVillager(EntityVillager vil,
            net.minecraft.server.v1_8_R1.World mcWorld) {
        Location location = vil.getBukkitEntity().getLocation();

        BalancedVillager balancedVil = new BalancedVillager(vil, true);
        balancedVil.setPosition(location.getX(), location.getY(),
                location.getZ());

        mcWorld.removeEntity(vil);
        mcWorld.addEntity(balancedVil, SpawnReason.CUSTOM);
    }



    // Methods for offers' custom config file
    /**
     * Reloads the offers.yml config.
     * 
     * @see org.bukkit.plugin.java.JavaPlugin.reloadConfig()
     */
    public void reloadOfferConfig() {
        if (offerConfigFile == null)
            offerConfigFile = new File(getDataFolder(), OFFER_CONFIG_FILENAME);

        offerConfig = YamlConfiguration.loadConfiguration(offerConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource(OFFER_CONFIG_FILENAME);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration
                    .loadConfiguration(defConfigStream);
            offerConfig.setDefaults(defConfig);
        }
    }

    /**
     * Gets the offers.yml config.
     * 
     * @see org.bukkit.plugin.java.JavaPlugin.getConfig()
     */
    public FileConfiguration getOfferConfig() {
        if (offerConfig == null) {
            this.reloadOfferConfig();
        }
        return offerConfig;
    }

    /**
     * Saves the offers.yml config.
     * 
     * @see org.bukkit.plugin.java.JavaPlugin.saveConfig()
     */
    public void saveOfferConfig() {
        if (offerConfig == null || offerConfigFile == null)
            return;

        try {
            getOfferConfig().save(offerConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE,
                    "Could not save config to " + offerConfigFile, ex);
        }
    }

    /**
     * Saves the default offers.yml config.
     * 
     * @see org.bukkit.plugin.java.JavaPlugin.saveDefaultConfig()
     */
    public void saveDefaultOfferConfig() {
        saveResource(OFFER_CONFIG_FILENAME, false);
    }

    /**
     * Saves two sample config files: offers-default.yml (a copy of offers.yml)
     * and offers-vanilla.yml.
     * 
     * @see org.bukkit.plugin.java.JavaPlugin.saveDefaultConfig()
     */
    public void saveSampleOfferConfigs() {
        File defaultConfig = new File(getDataFolder(),
                OFFER_DEFAULT_CONFIG_FILENAME);
        if (!defaultConfig.exists())
            saveResourceCopy(OFFER_CONFIG_FILENAME,
                    OFFER_DEFAULT_CONFIG_FILENAME);

        File vanillaConfig = new File(getDataFolder(),
                OFFER_VANILLA_CONFIG_FILENAME);
        if (!vanillaConfig.exists())
            saveResource(OFFER_VANILLA_CONFIG_FILENAME, false);
    }

    /**
     * Saves a resource to a custom destination name. <br>
     * Mostly copied from saveResource, but without exception throwing as this
     * isn't in an API.
     * 
     * @param resourceName
     *            - The name of the resource to save a copy of (not a path!)
     * @param destName
     *            - The destination name to which the resource is saved (again,
     *            not a path!)
     * @see org.bukkit.plugin.java.JavaPlugin.saveResource(String arg0, boolean
     *      arg1)
     */
    public void saveResourceCopy(String resourceName, String destName) {
        InputStream in = getResource(resourceName);

        File outFile = new File(getDataFolder(), destName);
        File outDir = getDataFolder();
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists()) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else
                Logger.getLogger(JavaPlugin.class.getName()).log(
                        Level.WARNING,
                        "Could not save " + outFile.getName() + " to "
                                + outFile + " because " + outFile.getName()
                                + " already exists.");
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
                    "Could not save " + outFile.getName() + " to " + outFile,
                    ex);
        }

    }

}
