package preMangler.death;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import jogLib.core.JogPlugin;

public class Corpse implements Listener, Runnable
{
	static HashMap<Entity, CorpseInventory> corpseInventories;
	static NamespacedKey dataKey;
	
	static void init(JogPlugin plugin)
	{
		corpseInventories = new HashMap<Entity, CorpseInventory>();
		dataKey = new NamespacedKey(plugin, "CorpseDataKey");
		Corpse corpse = new Corpse();
		Bukkit.getPluginManager().registerEvents(corpse, plugin);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, corpse, 0, 5);
		
		for (Iterator<World> worldIterator = Bukkit.getWorlds().iterator(); worldIterator.hasNext();)
		{
			World world = worldIterator.next();
			for (Iterator<Entity> entityIterator = world.getEntities().iterator(); entityIterator.hasNext();)
			{
				Entity entity = entityIterator.next();
				if (CorpseInventory.validCorpse(entity)) corpseInventories.put(entity, new CorpseInventory(entity));
			}
		}
	}
	
	static void disable()
	{
		for (Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator(); iterator.hasNext();)
		{
			Player player = iterator.next();
			InventoryView view = player.getOpenInventory();
			if (view != null && getFromInventory(view.getTopInventory()) != null)
			{
				view.close();
				player.sendMessage("You have been kicked out of that screen because the plugin has been disabled, if this is due to a reload then you should be able to open it again momentarily.");
			}
		}
	}
	
	@Override
	public void run()
	{
		Stack<Entity> removed = new Stack<Entity>();
		for (Iterator<Entry<Entity, CorpseInventory>> iterator = corpseInventories.entrySet().iterator(); iterator.hasNext();)
		{
			CorpseInventory inventory = iterator.next().getValue();
			inventory.update();
			if (!inventory.keep) removed.push(inventory.corpse);
		}
		while (!removed.isEmpty())
		{
			Entity entity = removed.pop();
			corpseInventories.get(entity).closeAll();
			corpseInventories.remove(entity);
			entity.remove();
		}
	}
	
	public static void spawnCorpse(ItemStack[] items, Player owner, Location location)
	{
		Skeleton corpse = (Skeleton)location.getWorld().spawnEntity(location, EntityType.SKELETON);
		corpse.setAI(false);
		corpse.setRemoveWhenFarAway(false);
		corpse.setCanPickupItems(false);
		corpse.setCollidable(false);
		corpse.setGravity(false);
		corpse.setInvulnerable(true);
		corpse.setSilent(true);
		corpse.setCustomName(owner.getName() + "'s Corpse");
		corpse.setCustomNameVisible(true);
		corpse.setCanPickupItems(false);
		
		EntityEquipment equipment = corpse.getEquipment();
		EntityEquipment ownerEquipment = owner.getEquipment();
		equipment.clear();
		if (ownerEquipment.getBoots() != null) equipment.setBoots(ownerEquipment.getBoots());
		if (ownerEquipment.getLeggings() != null) equipment.setLeggings(ownerEquipment.getLeggings());
		if (ownerEquipment.getChestplate() != null) equipment.setChestplate(ownerEquipment.getChestplate());
		if (ownerEquipment.getHelmet() != null) equipment.setHelmet(ownerEquipment.getHelmet());
		if (ownerEquipment.getItemInMainHand() != null) equipment.setItemInMainHand(ownerEquipment.getItemInMainHand());
		if (ownerEquipment.getItemInOffHand() != null) equipment.setItemInOffHand(ownerEquipment.getItemInOffHand());
		
		corpseInventories.put(corpse, new CorpseInventory(corpse, items, owner));
	}
	
	public static CorpseInventory getFromInventory(Inventory inventory)
	{
		for (Iterator<Entry<Entity, CorpseInventory>> iterator = corpseInventories.entrySet().iterator(); iterator.hasNext();)
		{
			CorpseInventory inv = iterator.next().getValue();
			if (inv.inventory.equals(inventory)) return inv;
		}
		return null;
	}
	
	public static boolean isCorpse(Entity entity)
	{
		CorpseInventory inventory = getInventory(entity);
		if (inventory != null) return true;
		else return false;
	}
	
	public static boolean isCorpseOwner(Entity entity, OfflinePlayer player)
	{
		CorpseInventory inventory = getInventory(entity);
		if (inventory != null) return inventory.isOwner(player);
		else return false;
	}
	
	public static CorpseInventory getInventory(Entity entity)
	{
		if (corpseInventories.containsKey(entity))
		{
			return corpseInventories.get(entity);
		}
		else return null;
	}
	
	@EventHandler
	public void onEntityCombust(EntityCombustEvent event)
	{
		if (isCorpse(event.getEntity())) event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (isCorpse(event.getEntity())) event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		CorpseInventory inventory = getInventory(event.getEntity());
		if (inventory != null)
		{
			if (event.getDamager() instanceof Player && ((Player)event.getDamager()).getGameMode().equals(GameMode.CREATIVE))
			{
				ItemStack[] items = inventory.getInventory().getContents();
				for (Iterator<HumanEntity> iterator = inventory.getInventory().getViewers().iterator(); iterator.hasNext();) iterator.next().closeInventory();
				Location location = event.getEntity().getLocation();
				event.getEntity().remove();
				for (int index = 0; index < items.length; index++) if (items[index] != null) location.getWorld().dropItem(location, items[index]);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		CorpseInventory inventory = getInventory(event.getRightClicked());
		if (inventory != null)
		{
			if (inventory.isOwner(event.getPlayer()))
			{
				event.getPlayer().openInventory(inventory.getInventory());
			}
			else
			{
				if (event.getHand().equals(EquipmentSlot.HAND)) event.getPlayer().sendMessage("That isn't your corpse.");
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		Entity[] entities = event.getChunk().getEntities();
		for (int index = 0; index < entities.length; index++)
		{
			if (CorpseInventory.validCorpse(entities[index])) corpseInventories.put(entities[index], new CorpseInventory(entities[index]));
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		Entity[] entities = event.getChunk().getEntities();
		for (int index = 0; index < entities.length; index++)
		{
			CorpseInventory inventory = getInventory(entities[index]);
			if (inventory != null)
			{
				inventory.unload();
				corpseInventories.remove(entities[index]);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		CorpseInventory inventory = getFromInventory(event.getClickedInventory());
		if (inventory != null)
		{
			if (event.getView().getCursor() != null && event.getView().getCursor().getAmount() > 0) event.setCancelled(true);
			else
			{
				ItemStack item = event.getCurrentItem();
				EntityEquipment equipment = ((Skeleton)inventory.corpse).getEquipment();
				if (item == null);
				else if (item.equals(equipment.getBoots())) equipment.setBoots(null);
				else if (item.equals(equipment.getLeggings())) equipment.setLeggings(null);
				else if (item.equals(equipment.getChestplate())) equipment.setChestplate(null);
				else if (item.equals(equipment.getHelmet())) equipment.setHelmet(new ItemStack(Material.AIR));
				else if (item.equals(equipment.getItemInMainHand())) equipment.setItemInMainHand(null);
				else if (item.equals(equipment.getItemInOffHand())) equipment.setItemInOffHand(null);
			}
		}
		else if (event.isShiftClick() && getFromInventory(event.getView().getTopInventory()) != null) event.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event)
	{
		Set<Integer> rawSlots = event.getRawSlots();
		for (Iterator<Integer> iterator = rawSlots.iterator(); iterator.hasNext();) if (getFromInventory(event.getView().getInventory(iterator.next())) != null)
		{
			event.setCancelled(true);
			return;
		}
	}
}