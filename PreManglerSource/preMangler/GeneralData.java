package preMangler;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import globalResources.utilities.FIO;
import globalResources.utilities.dataSet.DataSet;
import jogLib.core.JogPlugin;
import preMangler.person.PeopleManager;
import preMangler.person.Person;

public class GeneralData
{
	private static DataSet data;
	
	public static void init(JogPlugin plugin)
	{
		if (data == null)
		{
			load();
		}
	}
	
	public static void setWorldSpawn(Location location)
	{
		DataSet spawnData = new DataSet();
		spawnData.set("WorldID", location.getWorld().getUID());
		spawnData.set("X", location.getBlockX());
		spawnData.set("Y", location.getBlockY());
		spawnData.set("Z", location.getBlockZ());
		data.set("WorldSpawn", spawnData);
		save();
	}
	
	public static Location getWorldSpawn()
	{
		DataSet spawnData = data.getSubSet("WorldSpawn");
		UUID id = spawnData.getUUID("WorldID", null);
		if (id != null)
		{
			World world = Bukkit.getWorld(id);
			if (world != null)
			{
				return new Location(world, spawnData.getInteger("X", 0), spawnData.getInteger("Y", 0), spawnData.getInteger("Z", 0));
			}
			else return Bukkit.getWorlds().get(0).getSpawnLocation();
		}
		else return Bukkit.getWorlds().get(0).getSpawnLocation();
	}
	
	private static void save()
	{
		FIO.writeBytes(getFile(), data.getAsBytes());
	}
	
	private static File getFile()
	{
		return new File(PreMangler.getDataDirectory().getPath() + "/GeneralData");
	}
	
	private static void load()
	{
		data = new DataSet(FIO.readBytes(getFile()));
	}
	
	public static boolean lockedMode()
	{
		return data.getBoolean("lockedMode", false);
	}
	
	public static void setLockedMode(boolean lockedMode)
	{
		data.set("lockedMode", lockedMode);
		save();
		if (lockedMode)
		{
			for (Iterator<Entry<Player, Person>> iterator = PeopleManager.getIterator(); iterator.hasNext();)
			{
				Entry<Player, Person> entry = iterator.next();
				if (!entry.getValue().isAdmin())
					entry.getKey().kickPlayer("The server has been closed to just administrators.");
			}
			Bukkit.broadcastMessage("The server has been closed to just administrators.");
		}
		else
			Bukkit.broadcastMessage("The server is now unlocked.");
	}
}