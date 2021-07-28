package preMangler.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

import globalResources.utilities.OperationResult;
import jogLib.core.JogPlugin;
import jogLib.core.OfflineNameTracker;
import preMangler.person.PeopleManager;

public class ShopManager implements Listener
{
	private static HashMap<String, String> wordConversions;
	private static HashMap<String, Material> generatedNames;
	private static boolean initialized = false;
	
	public static void init(JogPlugin plugin)
	{
		if (!initialized)
		{
			Bukkit.getPluginManager().registerEvents(new ShopManager(), plugin);
			
			plugin.getJogPluginLogger().log("Generating shortened material names...");
			wordConversions = new HashMap<String, String>();
			generatedNames = new HashMap<String, Material>();
			populateConversions();
			Material[] mats = Material.values();
			ArrayList<String> longNames = new ArrayList<>();
			ArrayList<String> duplicates = new ArrayList<>();
			for (int index = 0; index < mats.length; index++)
			{
				String name = generateName(mats[index], true);
				if (generatedNames.containsKey(name))
					duplicates.add(name);
				generatedNames.put(name, mats[index]);
				if (name.length() > 15)
					longNames.add(name);
			}
			plugin.getJogPluginLogger().log("names generated!");
			if (longNames.size() > 0)
			{
				String message = "The following names are too long: ";
				for (Iterator<String> iterator = longNames.iterator(); iterator.hasNext();)
				{
					message += iterator.next();
					if (iterator.hasNext())
						message += ", ";
				}
				plugin.getJogPluginLogger().log(message);
			}
			if (duplicates.size() > 0)
			{
				String message = "The following names appear more than once: ";
				for (Iterator<String> iterator = duplicates.iterator(); iterator.hasNext();)
				{
					message += iterator.next();
					if (iterator.hasNext())
						message += ", ";
				}
				plugin.getJogPluginLogger().log(message);
			}
			
			initialized = true;
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		List<Block> blocks = event.blockList();
		Stack<Block> shopBlocks = new Stack<Block>();
		for (Iterator<Block> iterator = blocks.iterator(); iterator.hasNext();)
		{
			Block block = iterator.next();
			if (shopPart(block)) shopBlocks.push(block);
		}
		while (!shopBlocks.isEmpty()) blocks.remove(shopBlocks.pop());
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
		for (Iterator<Block> iterator = event.getBlocks().iterator(); iterator.hasNext();) if (shopPart(iterator.next()))
		{
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
		for (Iterator<Block> iterator = event.getBlocks().iterator(); iterator.hasNext();) if (shopPart(iterator.next()))
		{
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event)
	{
		List<Block> blocks = event.blockList();
		Stack<Block> shopBlocks = new Stack<Block>();
		for (Iterator<Block> iterator = blocks.iterator(); iterator.hasNext();)
		{
			Block block = iterator.next();
			if (shopPart(block)) shopBlocks.push(block);
		}
		while (!shopBlocks.isEmpty()) blocks.remove(shopBlocks.pop());
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		if (shopPart(event.getBlock())) event.setCancelled(true);
	}
	
	public static boolean shopPart(Block block)
	{
		if (getShop(block) != null) return true;
		else
		{
			BlockFace[] faces = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
			for (int index = 0; index < faces.length; index++)
			{
				if (isWallSign(block.getRelative(faces[index])) && ((WallSign)block.getRelative(faces[index]).getBlockData()).getFacing().equals(faces[index])) return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent event)
	{
		if (event.getSource().getHolder() instanceof org.bukkit.block.Chest)
		{
			Shop shop = getShop(((org.bukkit.block.Chest)event.getSource().getHolder()).getBlock());
			if (shop != null)
			{
				if (!(event.getDestination() instanceof PlayerInventory))
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Shop shop = getShop(event.getBlock());
		if (shop != null && !shop.isOwner(event.getPlayer()))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage("You can not break another player's shop.");
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.hasBlock())
		{
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				Block block = event.getClickedBlock();
				Shop shop = getShop(block);
				if (shop != null)
				{
					if (isSign(block) || isWallSign(block))
					{
						shop.transact(PeopleManager.getPerson(event.getPlayer()));
					}
					else if (block.getType().equals(Material.CHEST))
					{
						if (!shop.isOwner(event.getPlayer()))
						{
							event.setCancelled(true);
							event.getPlayer().sendMessage("You can not open another player's shop!");
						}
					}
				}
			}
			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			{
				Block block = event.getClickedBlock();
				Shop shop = getShop(block);
				if (shop != null)
				{
					event.getPlayer().sendMessage(shop.getInfo());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event)
	{
		Block block = event.getBlockPlaced();
		if (block.getType().equals(Material.CHEST))
		{
			if (isShopSign(block.getRelative(BlockFace.UP)).successful())
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage("A chest can not be placed underneath a shop's sign.");
				event.setBuild(false);
			}
			else if (!event.getPlayer().isSneaking())
			{
				BlockFace[] faces = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
				for (int index = 0; index < faces.length; index++)
				{
					if (isShopChest(block.getRelative(faces[index])).successful())
					{
						event.setCancelled(true);
						event.getPlayer().sendMessage("A chest can not be attached to a shop chest.");
						event.setBuild(false);
					}
				}
			}
		}
		else if (isSign(block) || isWallSign(block))
		{
			BlockData data = block.getBlockData();
			block.setBlockData(event.getBlockReplacedState().getBlockData());
			if (isSign(data.getMaterial()))
			{
				if (isShopChest(block.getRelative(BlockFace.DOWN)).successful())
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage("A sign can not be placed above shop's chest.");
					event.setBuild(false);
				}
			}
			else if (isWallSign(data.getMaterial()))
			{
				if (isShopChest(event.getBlockAgainst()).successful())
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage("A sign can not be placed on a shop's chest.");
					event.setBuild(false);
				}
				else
				{
					if (isShopChest(block.getRelative(BlockFace.DOWN)).successful())
					{
						event.setCancelled(true);
						event.getPlayer().sendMessage("A sign can not be placed above shop's chest.");
						event.setBuild(false);
					}
				}
			}
			if (!event.isCancelled()) block.setBlockData(data);
		}
		
		if (!event.isCancelled())
		{
			OperationResult result = getShopAdvanced(block);
			if (result.successful())
			{
				Shop shop = (Shop)result.additionalInformation()[0];
				if (shop.isOwner(event.getPlayer())) event.getPlayer().sendMessage("Shop Created!\n" + shop.getInfo());
				else
				{
					event.setBuild(false);
					event.setCancelled(true);
					event.getPlayer().sendMessage("You can not create a shop in another player's name.");
				}
			}
			else
			{
				//reasoning(event.getPlayer(), result);
			}
		}
	}
	
	void reasoning(Player player, OperationResult result)
	{
		if (result.resultID() == 1)
		{
			OperationResult nested = (OperationResult)result.additionalInformation()[0];
			if (nested.resultID() != 5)
				player.sendMessage(nested.reason());
		}
		else
			player.sendMessage(result.reason());
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
		Block block = event.getBlock();
		OperationResult result = getShopAdvanced(block, event.getLines());
		if (result.successful())
		{
			Shop shop = (Shop)result.additionalInformation()[0];
			if (shop.isOwner(event.getPlayer())) event.getPlayer().sendMessage("Shop Created!\n" + shop.getInfo());
			else
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage("You can not create a shop in another player's name.");
			}
		}
		else
		{
			//reasoning(event.getPlayer(), result);
		}
	}
	
	public static Shop getShop(Block block)
	{
		return getShop(block, null);
	}
	
	static Shop getShop(Block block, String[] signText)
	{
		OperationResult result = getShopAdvanced(block, signText);
		if (result.successful()) return (Shop)result.additionalInformation()[0];
		else return null;
	}
	
	public static OperationResult getShopAdvanced(Block block)
	{
		return getShopAdvanced(block, null);
	}
	
	public static OperationResult getShopAdvanced(Block block, String[] signText)
	{
		Sign sign;
		org.bukkit.block.Chest chest;
		Object[] signData;
		if (block.getType().equals(Material.CHEST))
		{
			OperationResult result = isShopChest(block, signText);
			if (result.successful())
			{
				chest = (org.bukkit.block.Chest)block.getState();
				sign = (Sign)((Block)result.additionalInformation()[0]).getState();
				signData = new Object[5];
				signData[0] = ((OperationResult)result.additionalInformation()[1]).additionalInformation()[0];
				signData[1] = ((OperationResult)result.additionalInformation()[1]).additionalInformation()[1];
				signData[2] = ((OperationResult)result.additionalInformation()[1]).additionalInformation()[2];
				signData[3] = ((OperationResult)result.additionalInformation()[1]).additionalInformation()[3];
				signData[4] = ((OperationResult)result.additionalInformation()[1]).additionalInformation()[4];
			}
			else return new OperationResult(2, false, "Not a valid shop chest", OperationResult.defaultLongReason, result);
		}
		else if (isSign(block) || isWallSign(block))
		{
			OperationResult result = isShopSign(block, signText);
			if (result.successful())
			{
				chest = (org.bukkit.block.Chest)((Block)result.additionalInformation()[1]).getState();
				sign = (Sign)block.getState();
				OperationResult isShopChestResult = (OperationResult)result.additionalInformation()[0];
				signData = new Object[5];
				signData[0] = ((OperationResult)isShopChestResult.additionalInformation()[1]).additionalInformation()[0];
				signData[1] = ((OperationResult)isShopChestResult.additionalInformation()[1]).additionalInformation()[1];
				signData[2] = ((OperationResult)isShopChestResult.additionalInformation()[1]).additionalInformation()[2];
				signData[3] = ((OperationResult)isShopChestResult.additionalInformation()[1]).additionalInformation()[3];
				signData[4] = ((OperationResult)isShopChestResult.additionalInformation()[1]).additionalInformation()[4];
			}
			else return new OperationResult(1, false, "Not a valid shop sign", OperationResult.defaultLongReason, result);
		}
		else return new OperationResult(0, "Not a valid shop block");
		
		return new OperationResult(2, true, OperationResult.defaultShortReason, OperationResult.defaultLongReason, new Shop(chest, sign, signData));
	}
	
	public static boolean isSign(Block block)
	{
		return isSign(block.getType());
	}
	
	public static boolean isSign(Material type)
	{
		if (type.equals(Material.ACACIA_SIGN) || type.equals(Material.BIRCH_SIGN) || type.equals(Material.DARK_OAK_SIGN) || type.equals(Material.JUNGLE_SIGN)
				|| type.equals(Material.JUNGLE_SIGN) || type.equals(Material.OAK_SIGN) || type.equals(Material.SPRUCE_SIGN)) return true;
		else return false;
	}
	
	public static boolean isWallSign(Block block)
	{
		return isWallSign(block.getType());
	}
	
	public static boolean isWallSign(Material type)
	{
		if (type.equals(Material.ACACIA_WALL_SIGN) || type.equals(Material.BIRCH_WALL_SIGN) || type.equals(Material.DARK_OAK_WALL_SIGN) || type.equals(Material.JUNGLE_WALL_SIGN)
				|| type.equals(Material.JUNGLE_WALL_SIGN) || type.equals(Material.OAK_WALL_SIGN) || type.equals(Material.SPRUCE_WALL_SIGN)) return true;
		else return false;
	}
	
	public static OperationResult isShopSign(Block block)
	{
		return isShopSign(block, null);
	}
	
	//additional information: result, shop chest
	public static OperationResult isShopSign(Block block, String[] signText)
	{
		if (isSign(block))
		{
			OperationResult result = isShopChest(block.getRelative(BlockFace.DOWN), signText);
			if (result.successful()) return new OperationResult(1, true, OperationResult.defaultShortReason, OperationResult.defaultLongReason, result, block.getRelative(BlockFace.DOWN));
			else return new OperationResult(2, false, "Chest not valid shop", OperationResult.defaultLongReason, result);
		}
		else if (isWallSign(block))
		{
			OperationResult result = isShopChest(block.getRelative(BlockFace.DOWN), signText);
			if (result.successful()) return new OperationResult(1, true, OperationResult.defaultShortReason, OperationResult.defaultLongReason, result, block.getRelative(BlockFace.DOWN));
			else if (result.resultID() == 4)
			{
				Block chestBlock = block.getRelative(((WallSign)block.getBlockData()).getFacing().getOppositeFace());
				result = isShopChest(chestBlock, signText);
				if (result.successful()) return new OperationResult(1, true, OperationResult.defaultShortReason, OperationResult.defaultLongReason, result, chestBlock);
				else if (result.resultID() == 4) return new OperationResult(3, false, "No chest found", OperationResult.defaultLongReason, result);
				else return new OperationResult(2, false, "Chest not valid shop", OperationResult.defaultLongReason, result);
			}
			else return new OperationResult(2, false, "Chest not valid shop", OperationResult.defaultLongReason, result);
		}
		else return new OperationResult(0, "Not a sign block");
	}
	
	public static OperationResult isShopChest(Block block)
	{
		return isShopChest(block, null);
	}
	
	/*
	 * result codes:
	 * 0 - chest is wrong size
	 * 1 - chest has more than 1 attached sign
	 * 2 - attached sign's contents were not valid according to #isValidSign(Sign); additional information index 0 contains results from sign validation
	 * 3 - success; additional information index 0 contains sign block, additional information index 1 contains results from sign validation
	 * 4 - not a chest block
	 * 5 - sign not found
	 */
	public static OperationResult isShopChest(Block block, String[] signText)
	{
		if (block.getType().equals(Material.CHEST))
		{
			org.bukkit.block.Chest chest = (org.bukkit.block.Chest)block.getState();
			if (chest.getInventory().getSize() == 27)
			{
				Block shopSign = null;
				BlockFace[] faces = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
				for (int index = 0; index < faces.length; index++)
				{
					if (isWallSign(block.getRelative(faces[index])))
					{
						if (shopSign == null)
						{
							if (((WallSign)block.getRelative(faces[index]).getBlockData()).getFacing().equals(faces[index])) shopSign = block.getRelative(faces[index]);
						}
						else return new OperationResult(1, "Too many attatched signs");
					}
				}
				if (isSign(block.getRelative(BlockFace.UP)) || isWallSign(block.getRelative(BlockFace.UP)))
				{
					if (shopSign == null)
					{
						shopSign = block.getRelative(BlockFace.UP);
					}
					else return new OperationResult(1, "Too many attatched signs");
				}
				if (shopSign != null)
				{
					OperationResult result = isValidSign((Sign)shopSign.getState(), signText);
					if (result.successful()) return new OperationResult(3, true, OperationResult.defaultShortReason, OperationResult.defaultLongReason, shopSign, result);
					else return new OperationResult(result, "Invalid sign contents", 2);
				}
				else return new OperationResult(5, "Sign not found");
			}
			else return new OperationResult(0, "Wrong size");
		}
		else return new OperationResult(4, "Not a chest block");
	}
	
	public static OperationResult isValidSign(Sign sign)
	{
		return isValidSign(sign, null);
	}
	
	/*
	 * data format:
	 * boolean - if this shop is buying from customers
	 * material - material of item being sold
	 * integer - price of item (negative value means to use the global average)
	 * ShopOwner - owner of the shop
	 * integer - amount of items per transaction
	 * 
	 * result codes
	 * 0 - shop type (line 0) is invalid (not "BUING" or "SELLING")
	 * 1 - price line (line 2) does not start with '$'
	 * 2 - price line (line 2) could not be parsed into an integer
	 * 3 - price line (line 2) is a negative integer
	 * 4 - owner line (line 3) is not a valid shop owner
	 * 5 - success; additional data index 0 contains data array
	 * 6 - invalid material name (line 1)
	 * 7 - amount must be greater than 0
	 */
	public static OperationResult isValidSign(Sign sign, String[] signText)
	{
		if (signText == null) signText = sign.getLines();
		Object[] data = new Object[5];
		if (signText[0].startsWith("BUYING") || signText[0].startsWith("SELLING"))
		{
			boolean buying = signText[0].substring(0, 5).compareTo("BUYING") == 0;
			int amount = -1;
			if (buying)
			{
				if (signText[0].length() > 6)
				{
					if (signText[0].charAt(6) == 'x')
					{
						try
						{
							amount = Integer.parseInt(signText[0].substring(7));
						}
						catch (Exception e)
						{
							
						}
					}
				}
				else
					amount = 1;
			}
			else
			{
				if (signText[0].length() > 7)
				{
					if (signText[0].charAt(7) == 'x')
					{
						try
						{
							amount = Integer.parseInt(signText[0].substring(8));
						}
						catch (Exception e)
						{
							
						}
					}
				}
				else
					amount = 1;
			}
			
			if (amount < 1)
			{
				return new OperationResult(7, "Amount must be greater than 0");
			}
			
			data[0] = buying;
			data[4] = amount;
			Material material = getMaterial(signText[1]);
			if (!material.equals(Material.AIR))
			{
				data[1] = material;
				if (signText[2].compareTo("AVERAGE") == 0) data[2] = -1;
				else if (!(signText[2].startsWith("$") && signText[2].length() > 1)) return new OperationResult(1, "Price line is an invalid format");
				else
				{
					try
					{
						int price = Integer.parseInt(signText[2].substring(1));
						if (price >= 0)
						{
							data[2] = price;
						}
						else return new OperationResult(3, "Price can not be negative");
					}
					catch (Exception e)
					{
						return new OperationResult(2, "Could not parse integer from price line");
					}
				}
				if (signText[3].compareTo("MOD") == 0) data[3] = new ShopOwner();
				else
				{
					UUID id = OfflineNameTracker.getID(signText[3]);
					if (id == null) return new OperationResult(4, "Could not find player with that name");
					else data[3] = new ShopOwner(Bukkit.getOfflinePlayer(id));
				}
				return new OperationResult(5, true, OperationResult.defaultShortReason, OperationResult.defaultLongReason, data);
			}
			else return new OperationResult(6, "Invalid material name");
		}
		else return new OperationResult(0, "Invalid shop type");
	}
	
	public static Material getMaterial(String name)
	{
		if (generatedNames.containsKey(name)) return generatedNames.get(name);
		else return Material.AIR;
	}
	
	public static Material parseName(String name)
	{
		ArrayList<String> parts = new ArrayList<String>();
		String part = "";
		boolean inNumber = false;
		for (int index = 0; index < name.length(); index++)
		{
			char ch = name.charAt(index);
			boolean cont = false;
			if (inNumber)
			{
				if (Character.isDigit(ch)) part += ch;
				else
				{
					if (part.length() > 0) parts.add(part);;
					part = "";
					cont = true;
					inNumber = false;
				}
			}
			else cont = true;
			if (cont)
			{
				if (Character.isDigit(ch))
				{
					if (part.length() > 0) parts.add(part);
					part = "" + ch;
					inNumber = true;
				}
				else if (Character.isUpperCase(ch))
				{
					if (part.length() > 0) parts.add(part);
					part = "" + Character.toLowerCase(ch);
				}
				else part += ch;
			}
		}
		if (part.length() > 0) parts.add(part);
		for (Iterator<Entry<String, String>> iterator = wordConversions.entrySet().iterator(); iterator.hasNext();)
		{
			Entry<String, String> entry = iterator.next();
			for (Iterator<String> partIterator = parts.iterator(); partIterator.hasNext();)
			{
				int index = parts.indexOf(partIterator.next());
				if (entry.getValue().compareTo(parts.get(index)) == 0) parts.set(index, entry.getKey());
			}
		}
		String properName = "";
		for (Iterator<String> iterator = parts.iterator(); iterator.hasNext();)
		{
			properName += iterator.next().toUpperCase();
			if (iterator.hasNext()) properName += "_";
		}
		Material material = Material.getMaterial(properName);
		if (material == null) material = Material.AIR;
		return material;
	}
	
	public static String getName(Material material, boolean shorten)
	{
		if (!shorten) return generateName(material, false);
		else
		{
			for (Iterator<Entry<String, Material>> iterator = generatedNames.entrySet().iterator(); iterator.hasNext();)
			{
				Entry<String, Material> entry = iterator.next();
				if (entry.getValue().equals(material)) return entry.getKey();
			}
			return "air";
		}
	}
	
	public static String generateName(Material material, boolean shorten)
	{
		String name = material.name();
		String newName = "";
		ArrayList<String> parts = new ArrayList<String>();
		String part = "";
		int totalLength = 0;
		for (int index = 0; index < name.length(); index++)
		{
			char ch = name.charAt(index);
			if (ch == '_')
			{
				if (part.length() > 0)
				{
					parts.add(part);
					totalLength += part.length();
				}
				part = "";
			}
			else
			{
				part += Character.toLowerCase(ch);
			}
		}
		if (part.length() > 0)
		{
			parts.add(part);
			totalLength += part.length();
		}
		boolean capitalize = false;
		for (Iterator<String> iterator = parts.iterator(); iterator.hasNext();)
		{
			part = iterator.next();
			if (totalLength > 15 && shorten && wordConversions.containsKey(part)) part = wordConversions.get(part);
			if (capitalize)
			{
				if (part.length() > 1) part = Character.toUpperCase(part.charAt(0)) + part.substring(1);
				else part = part.toUpperCase();
			}
			else capitalize = true;
			newName += part;
		}
		return newName;
	}
	
	private static void populateConversions()
	{
		wordConversions.put("zombified", "zmbd");
		wordConversions.put("twisting", "twsd");
		wordConversions.put("weeping", "wpn");
		wordConversions.put("cauldron", "cldrn");
		wordConversions.put("dripstone", "drstn");
		wordConversions.put("piglin", "pign");
		wordConversions.put("fungus", "fng");
		wordConversions.put("amethyst", "ame");
		wordConversions.put("cut", "ct");
		wordConversions.put("copper", "cu");
		wordConversions.put("crimson", "cmsn");
		wordConversions.put("flowering", "flwn");
		wordConversions.put("plate", "plt");
		wordConversions.put("pickaxe", "pick");
		wordConversions.put("deepslate", "dslt");
		wordConversions.put("blackstone", "bkn");
		wordConversions.put("waxed", "wxd");
		wordConversions.put("exposed", "expd");
		wordConversions.put("weathered", "wthd");
		wordConversions.put("oxidized", "ox");
		wordConversions.put("warped", "wrp");
		wordConversions.put("azalea", "azl");
		wordConversions.put("cobbled", "cbld");
		wordConversions.put("acacia", "ac");
		wordConversions.put("black", "blck");
		wordConversions.put("brown", "brwn");
		wordConversions.put("green", "gren");
		wordConversions.put("light", "lt");
		wordConversions.put("magenta", "mgnt");
		wordConversions.put("orange", "orng");
		wordConversions.put("purple", "prpl");
		wordConversions.put("white", "whte");
		wordConversions.put("yellow", "yllw");
		wordConversions.put("stained", "stnd");
		wordConversions.put("glazed", "glzd");
		wordConversions.put("terracotta", "trrcta");
		wordConversions.put("glass", "gls");
		wordConversions.put("powder", "powd");
		wordConversions.put("pressure", "prs");
		wordConversions.put("pane", "pn");
		wordConversions.put("concrete", "concrt");
		wordConversions.put("coral", "crl");
		wordConversions.put("wall", "wl");
		wordConversions.put("fan", "fn");
		wordConversions.put("sandstone", "sdstn");
		wordConversions.put("gray", "gry");
		wordConversions.put("mossy", "msy");
		wordConversions.put("cobblestone", "cblstn");
		wordConversions.put("spawn", "spn");
		wordConversions.put("melon", "mln");
		wordConversions.put("glistering", "glsting");
		wordConversions.put("golden", "gldn");
		wordConversions.put("weighted", "wtd");
		wordConversions.put("infested", "ifsd");
		wordConversions.put("chestplate", "chstpt");
		wordConversions.put("leggings", "lgngs");
		wordConversions.put("potted", "ptd");
		wordConversions.put("stripped", "strpd");
		wordConversions.put("zombie", "zmb");
		wordConversions.put("trader", "trdr");
		wordConversions.put("smooth", "sth");
		wordConversions.put("dark", "dk");
		wordConversions.put("chiseled", "chsd");
		wordConversions.put("spider", "spdr");
		wordConversions.put("cartography", "crtgrphy");
		wordConversions.put("table", "tbl");
		wordConversions.put("banner", "bnr");
		wordConversions.put("pattern", "ptrn");
		wordConversions.put("block", "blk");
		wordConversions.put("daylight", "dylt");
		wordConversions.put("detector", "dtctr");
		wordConversions.put("dead", "dd");
		wordConversions.put("bubble", "bb");
		wordConversions.put("prismarine", "prsmrn");
		wordConversions.put("command", "cmd");
		wordConversions.put("mushroom", "mshrm");
		wordConversions.put("pumpkin", "pmpkn");
		wordConversions.put("blue", "bl");
		wordConversions.put("nether", "nthr");
		wordConversions.put("brick", "brk");
		wordConversions.put("polished", "plshd");
		wordConversions.put("sapling", "splng");
		wordConversions.put("chorus", "chrs");
		wordConversions.put("valley", "vly");
		wordConversions.put("lily", "lly");
		wordConversions.put("bucket", "bkt");
		wordConversions.put("silverfish", "slvrfsh");
		wordConversions.put("red", "rd");
		wordConversions.put("stairs", "str");
		wordConversions.put("wither", "wthr");
		wordConversions.put("stem", "stm");
		wordConversions.put("attached", "atchd");
		wordConversions.put("bricks", "brk");
		wordConversions.put("fermented", "frmtd");
		wordConversions.put("heavy", "hvy");
		wordConversions.put("stone", "stn");
		wordConversions.put("petrified", "ptrfd");
		wordConversions.put("leather", "lthr");
		wordConversions.put("music", "msc");
		wordConversions.put("andesite", "andst");
		wordConversions.put("diorite", "drit");
		wordConversions.put("granite", "grnit");
		wordConversions.put("pufferfish", "pfrfsh");
		wordConversions.put("skeleton", "skltn");
		wordConversions.put("slab", "slb");
		wordConversions.put("tropical", "trpcl");
		wordConversions.put("vindicator", "vndctr");
		wordConversions.put("wandering", "wndrg");
		wordConversions.put("villager", "vlgr");
		wordConversions.put("horse", "hrs");
		wordConversions.put("armor", "armr");
		wordConversions.put("guardian", "grdn");
		wordConversions.put("enchanted", "enchtd");
		wordConversions.put("experience", "xp");
		wordConversions.put("skull", "skl");
		wordConversions.put("cracked", "crkd");
	}
}