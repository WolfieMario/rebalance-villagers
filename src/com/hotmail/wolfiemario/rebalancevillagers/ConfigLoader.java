package com.hotmail.wolfiemario.rebalancevillagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.server.Item;
import net.minecraft.server.Tuple;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.hotmail.wolfiemario.ItemIDGetter;
import com.hotmail.wolfiemario.rebalancevillagers.offers.CustomOffer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.ItemStackProducer;
import com.hotmail.wolfiemario.rebalancevillagers.offers.ItemStackProducerFactory;
import com.hotmail.wolfiemario.rebalancevillagers.offers.PotentialOffersList;
import com.hotmail.wolfiemario.rebalancevillagers.offers.SimpleOffer;

public class ConfigLoader
{
	private RebalanceVillagers plugin;
	
	private static HashMap<String, Integer> professionsMap;
	static
	{
		professionsMap = new HashMap<String, Integer>();
		
		professionsMap.put("farmer", 0);
		professionsMap.put("librarian", 1);
		professionsMap.put("priest", 2);
		professionsMap.put("blacksmith", 3);
		professionsMap.put("butcher", 4);
		
		professionsMap.put("default-offers", -1);
	}
	
	private static final String CONFIG_WARNING_POSTFIX = "Please check your config.yml file!";
	private static final String OFFERS_WARNING_POSTFIX = "Please check your offers.yml file!";
	
	private static final String CONFIG_OFFER_REMOVAL = "offer-removal";
	private static final String CONFIG_REMOVE_OFFERS = CONFIG_OFFER_REMOVAL + ".remove-offers";
	private static final String CONFIG_REMOVAL_CHANCE = CONFIG_OFFER_REMOVAL + ".removal-chance";
	
	private static final String CONFIG_OFFER_GENERATION = "offer-generation";
	private static final String CONFIG_GENERAL_TRADING = "general-trading";
	private static final String CONFIG_GENERAL = "general";
	private static final String CONFIG_SHOPKEEPERS = "shopkeepers";
	
	private static final String CONFIG_CURRENCY_ITEM = "currency-item";
	private static final String CONFIG_POTENTIAL_OFFERS = "potential-offers";
	private static final String CONFIG_BUY_VALUES = "buy-values";
	private static final String CONFIG_SELL_VALUES = "sell-values";
	
	public ConfigLoader(RebalanceVillagers handle)
	{
		plugin = handle;
	}
	
	//Delegates for commonly-called methods.
	private FileConfiguration getConfig()		{	return plugin.getConfig();		}
	private FileConfiguration getOfferConfig()	{	return plugin.getOfferConfig();	}
	private Logger getLogger()					{	return plugin.getLogger();		}

	void applyConfig()
	{
		BalancedVillager.setOfferRemoval(getConfig().getBoolean(CONFIG_REMOVE_OFFERS, true));
		
		String removalMin = CONFIG_REMOVAL_CHANCE + ".minimum";
		int removalMinimum = getConfig().getInt(removalMin, 3);
		String removalMax = CONFIG_REMOVAL_CHANCE + ".maximum";
		int removalMaximum = getConfig().getInt(removalMax, 13);
		BalancedVillager.setOfferRemovalRange(removalMinimum, removalMaximum);
		if(removalMinimum > removalMaximum)
			getLogger().info("Warning: '" + removalMin + "' is greater than '" + removalMax + "'! " + CONFIG_WARNING_POSTFIX);
		
		String removalTicks = CONFIG_OFFER_REMOVAL + ".removal-ticks";
		BalancedVillager.setRemovalTicks(validateMinimumOfOne(getConfig().getInt(removalTicks, 20), removalTicks, CONFIG_WARNING_POSTFIX));
		
		String defaultOfferCount = CONFIG_OFFER_GENERATION + ".default-offer-count";
		BalancedVillager.setDefaultOfferCount(validateMinimumOfOne(getConfig().getInt(defaultOfferCount, 1), defaultOfferCount, CONFIG_WARNING_POSTFIX));
		String newOfferCount = CONFIG_OFFER_GENERATION + ".new-offer-count";
		BalancedVillager.setNewOfferCount(validateMinimumOfOne(getConfig().getInt(newOfferCount, 1), newOfferCount, CONFIG_WARNING_POSTFIX));
		String generationTicks = CONFIG_OFFER_GENERATION + ".generation-ticks";
		BalancedVillager.setGenerationTicks(validateMinimumOfOne(getConfig().getInt(generationTicks, 60), generationTicks, CONFIG_WARNING_POSTFIX));
		String forAny = CONFIG_OFFER_GENERATION + ".for-any-trade";
		BalancedVillager.setForAnyTrade(getConfig().getBoolean(forAny, false));
		String probability = CONFIG_OFFER_GENERATION + ".probability";
		BalancedVillager.setNewProbability(getConfig().getInt(probability, 100));
		
		String particleTicks = CONFIG_GENERAL_TRADING + ".particle-ticks";
		BalancedVillager.setParticleTicks(validateMinimumOfOne(getConfig().getInt(particleTicks, 200), particleTicks, CONFIG_WARNING_POSTFIX));
		String allowMulti = CONFIG_GENERAL_TRADING + ".allow-bickering";
		BalancedVillager.setAllowMultivending(getConfig().getBoolean(allowMulti, false));
		String allowChild = CONFIG_GENERAL_TRADING + ".can-trade-children";
		BalancedVillager.setCanTradeChildren(getConfig().getBoolean(allowChild, false));
		
		String maxHealth = CONFIG_GENERAL + ".max-health";
		BalancedVillager.setMaxHealth(validateMinimumOfOne(getConfig().getInt(maxHealth, 20), maxHealth, CONFIG_WARNING_POSTFIX));
		plugin.allowDamage = getConfig().getBoolean(CONFIG_GENERAL + ".allow-damage", true);
		
		String professions = CONFIG_GENERAL + ".allowed-spawn-professions";
		List<Integer> integerList = getConfig().getIntegerList(professions);
		
		if(!pathContentsExists(integerList, professions, CONFIG_WARNING_POSTFIX))
			plugin.allowedProfessions = RebalanceVillagers.DEFAULT_ALLOWED_PROFESSIONS;
		else
			plugin.allowedProfessions = integerList.toArray(new Integer[0]);
		
		if(plugin.allowedProfessions.length < 1)
		{
			plugin.allowedProfessions = RebalanceVillagers.DEFAULT_ALLOWED_PROFESSIONS;
			getLogger().info("Warning: '" + professions + "' must contain at least one number! " + CONFIG_WARNING_POSTFIX);
		}
		
		String checkAttempts = CONFIG_SHOPKEEPERS + ".times-to-check";
		plugin.shopkeeperCheckAttempts = validateMinimumOfOne(getConfig().getInt(checkAttempts, 5), checkAttempts, CONFIG_WARNING_POSTFIX);
		String checkDelay = CONFIG_SHOPKEEPERS + ".time-between-checks";
		plugin.shopkeeperCheckDelay = validateMinimumOfOne(getConfig().getInt(checkDelay, 50), checkDelay, CONFIG_WARNING_POSTFIX);
	}
	
	void applyOfferConfig()
	{
		//Get currency
		String currencyName = getOfferConfig().getString(CONFIG_CURRENCY_ITEM, "emerald");
		int currencyId = ItemIDGetter.getID(currencyName);
		if(!idExists(currencyId, currencyName, CONFIG_CURRENCY_ITEM, OFFERS_WARNING_POSTFIX))
			currencyId = Item.EMERALD.id;
		ItemIDGetter.registerName(CONFIG_CURRENCY_ITEM, currencyId); //Allow users to use "currency-item" instead of an item name
		BalancedVillager.setCurrencyItem(currencyId);
		
		//This is needed for the hardcoded default gold offer to work properly, so we prepare it ahead of time.
		HashMap<Integer, Tuple> buyValues = new HashMap<Integer, Tuple>();
		buyValues.put(Integer.valueOf(Item.GOLD_INGOT.id), new Tuple(Integer.valueOf(2), Integer.valueOf(3)));
		
		//Get offers
		HashMap<Integer, PotentialOffersList> offersByProfession = new HashMap<Integer, PotentialOffersList>();
		ConfigurationSection professions = getOfferConfig().getConfigurationSection(CONFIG_POTENTIAL_OFFERS);
		if(!pathContentsExists(professions, CONFIG_POTENTIAL_OFFERS, OFFERS_WARNING_POSTFIX))
		{
			//The only possible offer at this point is the default, which is all we will assign.
			BalancedVillager.setBuyValues(buyValues);
			return;
		}
		Set<String> professionNames = professions.getKeys(false);
		
		for(String name: professionNames)
		{
			String path = CONFIG_POTENTIAL_OFFERS + "." + name;
			Integer professionId = professionsMap.get(name);
			
			//It's not one of our pre-defined villager classes.
			if(professionId == null)
			{
				try
				{
					//Is it a number?
					professionId = Integer.parseInt(name);
				}
				catch(NumberFormatException e)
				{
					getLogger().info("Warning: '" + path + "' is not a recognized villager profession! " + OFFERS_WARNING_POSTFIX);
					continue;
				}
			}
			
			//Load buy offers
			String configBuysPath = path + ".buys";
			ArrayList<SimpleOffer> buys = new ArrayList<SimpleOffer>();
			populateOffersList(buys, configBuysPath);
			
			//Load sell offers
			String configSellsPath = path + ".sells";
			ArrayList<SimpleOffer> sells = new ArrayList<SimpleOffer>();
			populateOffersList(sells, configSellsPath);
			
			//Load other offers
			String configOtherPath = path + ".other";
			ArrayList<CustomOffer> other = new ArrayList<CustomOffer>();
			ConfigurationSection configOthers = getOfferConfig().getConfigurationSection(configOtherPath);
			if(pathExists(configOtherPath, getOfferConfig(), OFFERS_WARNING_POSTFIX) && offersExist(configOthers, configOtherPath))
			{
				Set<String> potentialOthers = configOthers.getKeys(false);
				other.ensureCapacity(potentialOthers.size());
				populateCustomOffers(other, potentialOthers, configOtherPath);
			}
			
			PotentialOffersList offers = new PotentialOffersList(buys, sells, other);
			
			offersByProfession.put(professionId, offers);
		}
		BalancedVillager.setOffersByProfession(offersByProfession);
		
		//Get buy values
		ConfigurationSection configBuyValues = getOfferConfig().getConfigurationSection(CONFIG_BUY_VALUES);
		if(pathContentsExists(configBuyValues, CONFIG_BUY_VALUES, OFFERS_WARNING_POSTFIX))
		{
			Set<String> buyNames = configBuyValues.getKeys(false);
			populateValuesHashMap(buyValues, buyNames, CONFIG_BUY_VALUES);
		}
		BalancedVillager.setBuyValues(buyValues);
		
		//Get sell values
		HashMap<Integer, Tuple> sellValues = new HashMap<Integer, Tuple>();
		ConfigurationSection configSellValues = getOfferConfig().getConfigurationSection(CONFIG_SELL_VALUES);
		if(pathContentsExists(configSellValues, CONFIG_SELL_VALUES, OFFERS_WARNING_POSTFIX))
		{
			Set<String> sellNames = configSellValues.getKeys(false);
			populateValuesHashMap(sellValues, sellNames, CONFIG_SELL_VALUES);
		}
		BalancedVillager.setSellValues(sellValues);
		
	}

	//Convenience method since so many properties cannot be less than one.
	private int validateMinimumOfOne(int value, String path, String postfix)
	{
		if(value < 1)
		{
			value = 1;
			getLogger().info("Warning: '" + path + "' is less than 1! " + postfix);
		}
		
		return value;
	}
	
	private double validateProbability(String path, ConfigurationSection config, String postfix)
	{
		double probability;
		
		if(!config.isDouble(path))
			getLogger().info("Warning: '" + path + "' must be a decimal, such as 1.0! " + postfix);
		probability = getOfferConfig().getDouble(path);
		
		if(probability < 0 || probability > 1)
		{
			probability = probability < 0 ? 0 : 1;
			getLogger().info("Warning: '" + path + "' must be between 0.0 and 1.0 (inclusive)! " + postfix);
		}
		
		return probability;
	}

	private boolean pathContentsExists(Object value, String path, String postfix)
	{
		if(value == null)
		{
			getLogger().info("Warning: '" + path + "' was not found! " + postfix);
			return false;
		}
		return true;
	}
	
	private boolean pathExists(String path, ConfigurationSection config, String postfix)
	{
		if(!config.contains(path))
		{
			getLogger().info("Warning: '" + path + "' was not found! " + postfix);
			return false;
		}
		return true;
	}
	
	private boolean idExists(int id, String name, String path, String postfix)
	{
		if(id < 0)
		{
			getLogger().info("Warning: '" + name + "' at '" + path + "' is an unknown item! " + postfix + " Use a data value if necessary.");
			return false;
		}
		return true;
	}

	private boolean rangeExists(List<Integer> range, String path, String postfix)
	{
		if(range == null || range.size() < 1)
		{
			getLogger().info("Warning: '" + path + "' does not specify a range! " + postfix);
			return false;
		}
		return true;
	}
	
	private boolean offersExist(Object value, String path)
	{
		if(value == null)
		{
			//Disabled message as it was annoyingly spammy.
			//getLogger().info("Notice: '" + path + "' contains no offers. If this is not intentional, check your offers.yml file.");
			return false;
		}
		return true;
	}
	
	/**
	 * Allows easy access to a key's name, by trimming the path.
	 * I made this because I misunderstood how the getKeys method functions.
	 * @param path - the path representing this key
	 * @param header - the portion to be removed
	 * @return The name of the key.
	 */
	@SuppressWarnings("unused")
	private String trimPathHeader(String path, String header)
	{
		return path.substring(header.length() + 1);
	}
	
	private void populateOffersList(ArrayList<SimpleOffer> targetList, String path)
	{
		ConfigurationSection configOffers = getOfferConfig().getConfigurationSection(path);
		if(pathExists(path, getOfferConfig(), OFFERS_WARNING_POSTFIX) && offersExist(configOffers, path))
		{
			Set<String> potentialOffers = configOffers.getKeys(false);
			targetList.ensureCapacity(potentialOffers.size());
			populateSimpleOffers(targetList, potentialOffers, path);
		}
	}
	
	private void populateSimpleOffers(ArrayList<SimpleOffer> targetList, Set<String> potentialOffers, String pathHeader)
	{
		for(String name: potentialOffers)
		{
			String path = pathHeader + "." + name;
			
			int id = ItemIDGetter.getID(name);
			
			if(idExists(id, name, path, OFFERS_WARNING_POSTFIX))
			{
				double probability = validateProbability(path, getOfferConfig(), OFFERS_WARNING_POSTFIX);
				
				targetList.add(new SimpleOffer(id, (float) probability));
			}
		}
	}
	
	private void populateCustomOffers(ArrayList<CustomOffer> targetList, Set<String> potentialOffers, String pathHeader)
	{
		for(String name: potentialOffers)
		{
			String path = pathHeader + "." + name;
			String configBuyAPath = path + ".buyA";
			String configSellPath = path + ".sell";
			
			if(pathExists(configBuyAPath, getOfferConfig(), OFFERS_WARNING_POSTFIX) && pathExists(configSellPath, getOfferConfig(), OFFERS_WARNING_POSTFIX))
			{
				ItemStackProducer buyA = loadItemStackProducer(configBuyAPath);
				ItemStackProducer buyB = loadItemStackProducer(path + ".buyB"); //Will be null if nonexistent
				ItemStackProducer sell = loadItemStackProducer(configSellPath);
				double probability = validateProbability(path + ".probability", getOfferConfig(), OFFERS_WARNING_POSTFIX);
				
				if(buyA != null && sell != null)
					targetList.add(new CustomOffer(buyA, buyB, sell, (float) probability));
			}
		}
	}
	
	private ItemStackProducer loadItemStackProducer(String path)
	{
		if(!getOfferConfig().contains(path))
			return null;
		
		String itemPath = path + ".item";
		String amountPath = path + ".amount";
		String damagePath = path + ".damage";
		String enchantPath = path + ".enchant-level";
		String itemstackPath = path + ".itemstack";
		
		if(getOfferConfig().contains(itemPath))
		{
			String item = getOfferConfig().getString(itemPath);
			
			int id = ItemIDGetter.getID(item);
			
			if(idExists(id, item, path, OFFERS_WARNING_POSTFIX))
			{
				if(getOfferConfig().contains(itemstackPath))
					getLogger().info("Warning: '" + itemstackPath + "' was not checked, as '" + itemPath + "' was specified. " + OFFERS_WARNING_POSTFIX);
				
				List<Integer> amount = getOfferConfig().getIntegerList(amountPath);
				int minimum, maximum;
				if(getOfferConfig().contains(amountPath) && rangeExists(amount, amountPath, OFFERS_WARNING_POSTFIX))
				{
					minimum = amount.get(0);
					maximum = amount.get((amount.size() < 2) ? 0 : 1);
				}
				else
					minimum = maximum = 1;
				
				int damage = getOfferConfig().getInt(damagePath, 0);
				
				List<Integer> enchant = getOfferConfig().getIntegerList(enchantPath);
				if(getOfferConfig().contains(enchantPath) && rangeExists(enchant, enchantPath, OFFERS_WARNING_POSTFIX))
				{
					int minimumLevel = enchant.get(0);
					int maximumLevel = enchant.get((enchant.size() < 2) ? 0 : 1);
					
					return ItemStackProducerFactory.createItemStackProducer(id, minimum, maximum, damage, minimumLevel, maximumLevel);
				}
				else
					return ItemStackProducerFactory.createItemStackProducer(id, minimum, maximum, damage);
			}
		}
		if(getOfferConfig().contains(itemstackPath))
		{
			if(getOfferConfig().isItemStack(itemstackPath))
				return ItemStackProducerFactory.createItemStackProducer(getOfferConfig().getItemStack(itemstackPath));
			else
				getLogger().info("Warning: '" + itemstackPath + "' is not a valid ItemStack! " + OFFERS_WARNING_POSTFIX);
		}
		
		return null;
	}

	private void populateValuesHashMap(HashMap<Integer, Tuple> targetMap, Set<String> itemNames, String pathHeader)
	{
		for(String name: itemNames)
		{
			String path = pathHeader + "." + name;
			
			int id = ItemIDGetter.getID(name);
			
			if(idExists(id, name, path, OFFERS_WARNING_POSTFIX))
			{
				List<Integer> range = getOfferConfig().getIntegerList(path);
				
				if(rangeExists(range, path, OFFERS_WARNING_POSTFIX))
					targetMap.put(id, new Tuple(range.get(0), range.get( (range.size() < 2) ? 0 : 1) + 1));
			}
		}
	}
}
