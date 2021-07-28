package preMangler;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;

public class GeneralListener implements Listener
{
	HashMap<Material, Material> harvestables;
	
	public GeneralListener()
	{
		harvestables = new HashMap<Material, Material>();
		harvestables.put(Material.WHEAT, Material.WHEAT);
		harvestables.put(Material.POTATOES, Material.POTATO);
		harvestables.put(Material.CARROTS, Material.CARROT);
		harvestables.put(Material.BEETROOTS, Material.BEETROOT);
		harvestables.put(Material.NETHER_WART, Material.NETHER_WART);
		harvestables.put(Material.COCOA, Material.COCOA_BEANS);
	}
	
	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if (harvestables.containsKey(block.getType()) && ((Ageable)block.getBlockData()).getAge() == ((Ageable)block.getBlockData()).getMaximumAge())
			{
				block.getWorld().dropItem(block.getLocation(), new ItemStack(harvestables.get(block.getType())));
				Ageable ageable = (Ageable)block.getBlockData();
				ageable.setAge(0);
				block.setBlockData(ageable);
			}
		}
	}
	
	String reminder = "Please keep in mind that neither Bentham or Rick are responsible for administrating or moderating this server, if you have any questions you can contact TheJogMan.";
	
	@EventHandler
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event)
	{
		if (GeneralData.lockedMode())
		{
			OfflinePerson person = PeopleManager.getOfflinePerson(Bukkit.getOfflinePlayer(event.getUniqueId()));
			if (!person.isAdmin())
			{
				event.disallow(Result.KICK_OTHER, "The server is currently locked to just administrators.");
			}
			else
			{
				event.allow();
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (GeneralData.lockedMode())
			event.getPlayer().kickPlayer("The server is currently locked to just administrators.");
		else
			event.getPlayer().sendMessage("Welcome to the server! " + reminder);
	}
	
	@EventHandler
	public void onPlayerPortalEvent(PlayerPortalEvent event)
	{
		if (event.getTo().getWorld().getEnvironment().equals(Environment.THE_END))
		{
			event.setCancelled(true);
			Player player = event.getPlayer();
			player.sendMessage("The end is currently locked until we are ready to do a server event. " + reminder);
			player.teleport(GeneralData.getWorldSpawn());
		}
	}
}