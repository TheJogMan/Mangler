package preMangler.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import globalResources.utilities.ByteConverter;
import globalResources.utilities.dataSet.DataSet;
import preMangler.roles.Role;

public class Person extends OfflinePerson
{
	Player player;
	DataSet data;
	ArrayList<Role> roles;
	HashMap<UUID, Integer> bounties;
	Location home;
	
	Person(Player player)
	{
		super(player);
		this.player = player;
		if (PeopleManager.onlinePeople.containsKey(player)) throw new IllegalStateException("Player already loaded into people manager!");
		else
		{
			PeopleManager.onlinePeople.put(player, this);
			data = super.loadData();
			roles = getRoles();
			updateNames();
		}
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	void save()
	{
		setRoles(roles);
		super.saveData(data);
	}
	
	void sayRankAndRole()
	{
		sendMessage("Your rank is " + getRank().getColoredName() + " and your authority level is " + getAuthority().getColoredName() + ".");
	}
	
	void updateNames()
	{
		player.setDisplayName(getColoredName());
		player.setPlayerListName(getColoredName());
	}
	
	public boolean isSender(CommandSender sender)
	{
		return (sender instanceof Player && ((Player)sender).equals(player));
	}
	
	@Override
	public void sendMessage(String message)
	{
		player.sendMessage(message);
	}
	
	@Override
	public int getCashBalance()
	{
		return data.getInteger("CashBalance", 0);
	}
	
	@Override
	public void setCashBalance(int balance)
	{
		data.set("CashBalance", balance);
		sendMessage("Your balance is now $" + balance);
	}
	
	@Override
	ArrayList<Role> getRoles()
	{
		String[] roleIDs = ByteConverter.toStringArray(data.getByteArray("RoleData", new byte[0]));
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
	
	@SuppressWarnings("unchecked")
	@Override
	void setRoles(ArrayList<Role> roles)
	{
		this.roles = (ArrayList<Role>)roles.clone();
		String[] roleIDs = new String[roles.size()];
		for (int index = 0; index < roleIDs.length; index++)
		{
			roleIDs[index] = roles.get(index).name();
		}
		data.set("RoleData", ByteConverter.fromStringArray(roleIDs));
		updateNames();
	}
	
	@Override
	public void addRole(Role role)
	{
		if (!roles.contains(role))
		{
			roles.add(role);
			sendMessage("You have been given the " + role.getColoredName() + " role.");
			sayRankAndRole();
			updateNames();
		}
	}
	
	@Override
	public void removeRole(Role role)
	{
		if (roles.contains(role))
		{
			roles.remove(role);
			sendMessage("The " + role.getColoredName() + " has been taken from you.");
			sayRankAndRole();
			updateNames();
		}
	}
	
	@Override
	public boolean hasRole(Role role)
	{
		return roles.contains(role);
	}
	
	@Override
	public Role getRank()
	{
		if (roles.isEmpty()) return Role.getDefaultRole();
		Role highestRole = Role.getDefaultRole();
		for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();)
		{
			Role role = iterator.next();
			if (role.visible() && role.superior(highestRole)) highestRole = role;
		}
		return highestRole;
	}
	
	@Override
	public Role getAuthority()
	{
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