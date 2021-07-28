package preMangler.person;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import globalResources.utilities.FIO;
import jogLib.core.JogPlugin;
import preMangler.GeneralData;
import preMangler.PreMangler;

public class PeopleManager implements Runnable, Listener
{
	private static boolean initialized = false;
	static HashMap<Player, Person> onlinePeople;
	
	public static void init(JogPlugin plugin)
	{
		if (!initialized)
		{
			FIO.ensureDirectory(getPeopleDirectory());
			onlinePeople = new HashMap<Player, Person>();
			for (Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator(); iterator.hasNext();) getPerson(iterator.next());
			Bukkit.getPluginManager().registerEvents(new PeopleManager(), plugin);
			Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new PeopleManager(), 5, 5);
			initialized = true;
		}
	}
	
	static File getPeopleDirectory()
	{
		return new File(PreMangler.getDataDirectory().getPath() + "/People");
	}
	
	public static Person getPerson(Player player)
	{
		if (onlinePeople.containsKey(player)) return onlinePeople.get(player);
		else
		{
			return new Person(player);
		}
	}
	
	public static OfflinePerson getOfflinePerson(OfflinePlayer player)
	{
		return new OfflinePerson(player);
	}
	
	public static Iterator<Entry<Player, Person>> getIterator()
	{
		return onlinePeople.entrySet().iterator();
	}
	
	@Override
	public void run()
	{
		Stack<Player> offlinePeople = new Stack<Player>();
		Location worldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Location setSpawn = GeneralData.getWorldSpawn();
		double spawnDistance;
		if (setSpawn.getWorld().getUID().equals(worldSpawn.getWorld().getUID())) spawnDistance = setSpawn.distance(worldSpawn);
		else spawnDistance = Double.MAX_VALUE;
		for (Iterator<Entry<Player, Person>> iterator = onlinePeople.entrySet().iterator(); iterator.hasNext();)
		{
			Entry<Player, Person> entry = iterator.next();
			entry.getValue().save();
			if (spawnDistance > 40 && entry.getKey().getLocation().getWorld().getUID().equals(worldSpawn.getWorld().getUID()) && entry.getKey().getLocation().distance(worldSpawn) < 40)
			{
				entry.getKey().teleport(setSpawn);
			}
			if (!entry.getValue().isOnline()) offlinePeople.push(entry.getKey());
		}
		while(!offlinePeople.isEmpty()) onlinePeople.remove(offlinePeople.pop());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Person person = getPerson(event.getPlayer());
		event.setJoinMessage(person.getColoredName() + " §ehas joined the game");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Person person = getPerson(event.getPlayer());
		event.setQuitMessage(person.getColoredName() + " §ehas left the game");
	}
}