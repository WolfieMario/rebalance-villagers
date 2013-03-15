package com.hotmail.wolfiemario.rebalancevillagers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import net.minecraft.server.v1_5_R1.EntityVillager;

import org.bukkit.Bukkit;

/**
 * An attempt to wait for a newly spawned Villager to be added to
 * activeShopkeepers, so we can tell whether it was indeed a Shopkeeper.
 * Having learned about the Bukkit scheduler from nisovin, I intend to
 * change this mechanic, as multithreading leads to a potential
 * ConcurrentModificationException.
 * 
 * @author Gerrard Lukacs
 */
public class ShopkeeperWaiter implements Runnable {
    private EntityVillager villager;
    private net.minecraft.server.v1_5_R1.World mcWorld;
    private RebalanceVillagers plugin;
    
    static int shopkeeperCheckAttempts; // Number of times to check if a CUSTOM-spawned
    // villager has been registered as a
    // shopkeeper.
    
    static int shopkeeperCheckDelay; // The time in milliseconds before checking
    // whether a Shopkeeper is registered.


    public ShopkeeperWaiter(EntityVillager vil, net.minecraft.server.v1_5_R1.World world, RebalanceVillagers _plugin) {
        villager = vil;
        mcWorld = world;
        plugin = _plugin;
    }

    public void run() {
        try {
            for (int i = 0; i < ShopkeeperWaiter.shopkeeperCheckAttempts; i++) {
                Thread.sleep(ShopkeeperWaiter.shopkeeperCheckDelay);

                // check if that's a shopkeeper - but do that on main thread
                // to be sure (using the scheduler)
                boolean isShopkeeper = Bukkit.getScheduler()
                        .callSyncMethod(plugin, new Callable<Boolean>() {
                            public Boolean call() throws Exception {
                                return new Boolean(ShopkeepersHelper.isShopkeeper(villager.getBukkitEntity()));
                            }
                        }).get().booleanValue();

                if (isShopkeeper) {
                    // getLogger().info("Shopkeeper found.");
                    return;
                }
            }

            // getLogger().info("I think this isn't a Shopkeeper.");
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                    new Runnable() {
                        public void run() {
                            plugin.convertVillager(villager, mcWorld);
                        }
                    });

        } catch (InterruptedException e) {
            plugin.getLogger()
                    .info("Thread interruption: No clue how you just managed that."); // Seriously,
                                                                                      // assuming
                                                                                      // no
                                                                                      // reflection,
                                                                                      // that
                                                                                      // shouldn't
                                                                                      // be
                                                                                      // possible.
            e.printStackTrace();

        } catch (ExecutionException e) {
            plugin.getLogger()
                    .info("Bukkit Scheduler Execution interupted. Dunno if that's a problem.");
            e.printStackTrace();
        }
    }
}