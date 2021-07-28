package preMangler;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

import jogLib.core.JogPlugin;

public class StairSitting implements Runnable, Listener
{
	private static boolean initialized = false;
	static NamespacedKey dataKey;
	private static JogPlugin plugin;
	
	public static void init(JogPlugin plugin)
	{
		if (!initialized)
		{
			StairSitting.plugin = plugin;
			dataKey = new NamespacedKey(plugin, "ArrowSeatTag");
			Bukkit.getPluginManager().registerEvents(new StairSitting(), plugin);
			Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new StairSitting(), 0, 1);
			initialized = true;
		}
	}
	
	@Override
	public void run()
	{
		for (Iterator<World> iterator = Bukkit.getWorlds().iterator(); iterator.hasNext();)
		{
			for (Iterator<? extends Entity> cloudIterator = iterator.next().getEntitiesByClass(ArmorStand.class).iterator(); cloudIterator.hasNext();)
			{
				ArmorStand cloud = (ArmorStand)cloudIterator.next();
				Boolean val = cloud.getPersistentDataContainer().get(dataKey, new TagValue());
				if (val != null && val.booleanValue() && cloud.getPassengers().size() == 0) cloud.remove();
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getHand().equals(EquipmentSlot.HAND) && isStairs(block.getType()) && !event.getPlayer().isSneaking())
		{
			Stairs stairs = (Stairs)block.getBlockData();
			if (validShape(stairs.getShape()) && stairs.getHalf().equals(Half.BOTTOM))
			{
				event.setCancelled(true);
				Location location = block.getLocation();
				location.setX(location.getX() + .5);
				location.setY(location.getY() - .5);
				location.setZ(location.getZ() + .5);
				ArmorStand cloud = (ArmorStand)location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
				cloud.setGravity(false);
				cloud.setInvulnerable(true);
				cloud.setSilent(true);
				cloud.setVisible(false);
				cloud.setAI(false);
				cloud.setSmall(true);
				cloud.setCollidable(false);
				cloud.addPassenger(event.getPlayer());
				cloud.getPersistentDataContainer().set(dataKey, new TagValue(), true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Player)
		{
			Player player = (Player)event.getRightClicked();
			ItemStack helmet = player.getEquipment().getHelmet();
			if (helmet != null && isStairs(helmet.getType()))
			{
				player.addPassenger(event.getPlayer());
			}
		}
		else
		{
			Boolean val = event.getRightClicked().getPersistentDataContainer().get(dataKey, new TagValue());
			if (val != null && val.booleanValue()) event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDismount(EntityDismountEvent event)
	{
		if (isChair(event.getDismounted()))
		{
			Location location = event.getEntity().getLocation();
			location.setY(location.getY() + .5);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> event.getEntity().teleport(location));
		}
	}
	
	static boolean isChair(PersistentDataHolder holder)
	{
		Boolean val = holder.getPersistentDataContainer().get(dataKey, new TagValue());
		if (val != null && val.booleanValue()) return true;
		else return false;
	}
	
	static final Material[] stairs = {
			Material.OAK_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS, Material.DARK_OAK_STAIRS, Material.JUNGLE_STAIRS, Material.ACACIA_STAIRS,
			Material.COBBLESTONE_STAIRS, Material.STONE_STAIRS, Material.STONE_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.POLISHED_DIORITE_STAIRS, Material.POLISHED_GRANITE_STAIRS,
			Material.ANDESITE_STAIRS, Material.ANDESITE_STAIRS, Material.GRANITE_STAIRS, Material.END_STONE_BRICK_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS, Material.DIORITE_STAIRS,
			Material.NETHER_BRICK_STAIRS, Material.RED_NETHER_BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.RED_SANDSTONE_STAIRS, Material.QUARTZ_STAIRS, Material.PURPUR_STAIRS, Material.BRICK_STAIRS,
			Material.PRISMARINE_STAIRS, Material.PRISMARINE_BRICK_STAIRS, Material.DARK_PRISMARINE_STAIRS, Material.SMOOTH_RED_SANDSTONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS, Material.SMOOTH_QUARTZ_STAIRS,
			Material.CUT_COPPER_STAIRS, Material.EXPOSED_CUT_COPPER_STAIRS, Material.WEATHERED_CUT_COPPER_STAIRS, Material.OXIDIZED_CUT_COPPER_STAIRS, Material.WAXED_CUT_COPPER_STAIRS, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS,
			Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Material.CRIMSON_STAIRS, Material.WARPED_STAIRS, Material.COBBLED_DEEPSLATE_STAIRS, Material.POLISHED_DEEPSLATE_STAIRS,
			Material.DEEPSLATE_BRICK_STAIRS, Material.BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, Material.POLISHED_BLACKSTONE_STAIRS
	};
	
	static boolean isStairs(Material material)
	{
		for (int index = 0; index < stairs.length; index++) if (material.equals(stairs[index])) return true;
		return false;
	}
	
	static boolean validShape(Shape shape)
	{
		return shape.equals(Shape.STRAIGHT) || shape.equals(Shape.INNER_LEFT) || shape.equals(Shape.INNER_RIGHT);
	}
	
	static class TagValue implements PersistentDataType<byte[], Boolean>
	{
		@Override
		public Boolean fromPrimitive(byte[] arg0, PersistentDataAdapterContext arg1)
		{
			return arg0[0] == 1;
		}

		@Override
		public Class<Boolean> getComplexType()
		{
			return Boolean.class;
		}

		@Override
		public Class<byte[]> getPrimitiveType()
		{
			return byte[].class;
		}

		@Override
		public byte[] toPrimitive(Boolean arg0, PersistentDataAdapterContext arg1)
		{
			if (arg0.booleanValue()) return new byte[] {1};
			else return new byte[] {1};
		}
	}
}