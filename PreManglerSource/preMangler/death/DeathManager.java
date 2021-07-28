package preMangler.death;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import jogLib.core.JogPlugin;

public class DeathManager implements Listener
{
	private static boolean initialized = false;
	
	public static void init(JogPlugin plugin)
	{
		if (!initialized)
		{
			Corpse.init(plugin);
			Bukkit.getPluginManager().registerEvents(new DeathManager(), plugin);
			initialized = true;
		}
	}
	
	public static void disable()
	{
		Corpse.disable();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Location location = event.getEntity().getLocation();
		event.getEntity().sendMessage("You died at X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ() + " World: " + location.getWorld().getName());
		
		if (location.getY() >= 0)
		{
			List<ItemStack> drops = event.getDrops();
			ItemStack[] items = new ItemStack[drops.size()];
			int index = 0;
			for (Iterator<ItemStack> iterator = drops.iterator(); iterator.hasNext();)
			{
				items[index] = iterator.next().clone();
				index++;
			}
			drops.clear();
			
			Corpse.spawnCorpse(items, event.getEntity(), location);
		}
	}
}