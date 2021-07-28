package preMangler;

import java.io.File;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import globalResources.utilities.Logger;
import jogLib.core.JogLib;
import jogLib.core.JogPlugin;
import preMangler.command.CogCategory;
import preMangler.command.HatCommand;
import preMangler.death.DeathManager;
import preMangler.itemNameIndex.IndexManager;
import preMangler.person.PeopleManager;
import preMangler.shop.ShopManager;

public class PreMangler extends JogPlugin
{
	static PreMangler plugin;
	
	@Override
	public void onEnable()
	{
		PreMangler.plugin = this;
		GeneralData.init(this);
		PeopleManager.init(this);
		DeathManager.init(this);
		IndexManager.init(this);
		ShopManager.init(this);
		StairSitting.init(this);
		Bukkit.getPluginManager().registerEvents(new GeneralListener(), this);
		getCommand("Hat").setExecutor(new HatCommand());
		
		new CogCategory(JogLib.commandConsole);
	}
	
	@Override
	public void onDisable()
	{
		DeathManager.disable();
	}
	
	public static File getDataDirectory()
	{
		return plugin.getDataFolder();
	}
	
	public static Logger getJogLogger()
	{
		return plugin.getJogPluginLogger();
	}
	
	public class EndWatch implements Runnable
	{
		@Override
		public void run()
		{
			World world = Bukkit.getWorld("world");
			Location spawn = world.getSpawnLocation();
			spawn.setY(world.getHighestBlockYAt(spawn) + 1);
			for (Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator(); iterator.hasNext();)
			{
				Player player = iterator.next();
				if (player.getWorld().getName().compareTo("world_the_end") == 0)
				{
					player.teleport(spawn);
					player.sendMessage("The dragon fight will be a server event.");
				}
			}
		}
	}
}