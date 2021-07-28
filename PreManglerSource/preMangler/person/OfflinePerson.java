package preMangler.person;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import globalResources.utilities.ByteConverter;
import globalResources.utilities.FIO;
import globalResources.utilities.dataSet.DataSet;
import jogLib.core.OfflineNameTracker;
import preMangler.roles.Role;

public class OfflinePerson
{
	OfflinePlayer player;
	
	OfflinePerson(OfflinePlayer player)
	{
		this.player = player;
		FIO.ensureDirectory(getDirectory());
	}
	
	public OfflinePlayer getOfflinePlayer()
	{
		return player;
	}
	
	public Person getPerson()
	{
		if (player.isOnline()) return PeopleManager.getPerson(player.getPlayer());
		return null;
	}
	
	public boolean isAdmin()
	{
		return getAuthority().sufficient(Role.getMinAdminAuthority());
	}
	
	public boolean isPlayer(OfflinePlayer player)
	{
		return player.getUniqueId().compareTo(this.player.getUniqueId()) == 0;
	}
	
	public boolean isPerson(OfflinePerson person)
	{
		return person.player.getUniqueId().compareTo(this.player.getUniqueId()) == 0;
	}
	
	public boolean isOnline()
	{
		return player.isOnline();
	}
	
	File getGeneralDataFile()
	{
		return new File(getDirectory() + "/generalData");
	}
	
	File getDirectory()
	{
		return new File(PeopleManager.getPeopleDirectory().getPath() + "/" + player.getUniqueId().toString());
	}
	
	void saveData(DataSet data)
	{
		FIO.writeBytes(getGeneralDataFile(), data.getAsBytes());
	}
	
	DataSet loadData()
	{
		File file = getGeneralDataFile();
		if (FIO.canReadBytes(file)) return new DataSet(FIO.readBytes(file));
		else return new DataSet();
	}
	
	public boolean isSender(CommandSender sender)
	{
		if (isOnline()) return getPerson().isSender(sender);
		else return false;
	}
	
	public String getNameRaw()
	{
		String name = player.getName();
		if (name == null) name = OfflineNameTracker.getName(player.getUniqueId());
		return name;
	}
	
	public String getColoredName()
	{
		return getRank().getTextModifier() + getNameRaw() + "§r";
	}
	
	public void sendMessage(String message)
	{
		if (isOnline()) getPerson().sendMessage(message);
	}
	
	public int getCashBalance()
	{
		if (isOnline()) return getPerson().getCashBalance();
		else return loadData().getInteger("CashBalance", 0);
	}
	
	public void setCashBalance(int balance)
	{
		if (isOnline()) getPerson().setCashBalance(balance);
		else
		{
			DataSet data = loadData();
			data.set("CashBalance", balance);
			saveData(data);
		}
	}
	
	ArrayList<Role> getRoles()
	{
		if (isOnline()) return getPerson().getRoles();
		else
		{
			String[] roleIDs = ByteConverter.toStringArray(loadData().getByteArray("RoleData", new byte[0]));
			ArrayList<Role> roles = new ArrayList<Role>();
			for (int index = 0; index < roleIDs.length; index++)
			{
				try
				{
					Role role = Role.valueOf(roleIDs[index]);
					if (role != null) roles.add(role);
				}
				catch (Exception e)
				{
					
				}
			}
			return roles;
		}
	}
	
	void setRoles(ArrayList<Role> roles)
	{
		if (isOnline()) getPerson().setRoles(roles);
		else
		{
			String[] roleIDs = new String[roles.size()];
			for (int index = 0; index < roleIDs.length; index++)
			{
				roleIDs[index] = roles.get(index).name();
			}
			DataSet data = loadData();
			data.set("RoleData", ByteConverter.fromStringArray(roleIDs));
			saveData(data);
		}
	}
	
	public void addRole(Role role)
	{
		if (isOnline()) getPerson().addRole(role);
		else
		{
			ArrayList<Role> roles = getRoles();
			if (!roles.contains(role))
			{
				roles.add(role);
				setRoles(roles);
			}
		}
	}
	
	public void removeRole(Role role)
	{
		if (isOnline()) getPerson().removeRole(role);
		else
		{
			ArrayList<Role> roles = getRoles();
			if (roles.contains(role))
			{
				roles.remove(role);
				setRoles(roles);
			}
		}
	}
	
	public boolean hasRole(Role role)
	{
		if (isOnline()) return getPerson().hasRole(role);
		else
		{
			return getRoles().contains(role);
		}
	}
	
	public Role getRank()
	{
		if (isOnline()) return getPerson().getRank();
		else
		{
			ArrayList<Role> roles = getRoles();
			if (roles.isEmpty()) return Role.getDefaultRole();
			Role highestRole = Role.getDefaultRole();
			for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();)
			{
				Role role = iterator.next();
				if (role.visible() && role.superior(highestRole)) highestRole = role;
			}
			return highestRole;
		}
	}
	
	public Role getAuthority()
	{
		if (isOnline()) return getPerson().getAuthority();
		else
		{
			ArrayList<Role> roles = getRoles();
			if (roles.isEmpty()) return Role.getDefaultRole();
			Role highestRole = Role.getDefaultRole();
			for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();)
			{
				Role role = iterator.next();
				if (role.superior(highestRole)) highestRole = role;
			}
			return highestRole;
		}
	}
}