package preMangler.shop;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;

import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;

public class ShopOwner
{
	OfflinePerson player;
	
	ShopOwner(OfflinePlayer player)
	{
		this.player = PeopleManager.getOfflinePerson(player);
	}
	
	ShopOwner() //MOD shop
	{
		this.player = null;
	}
	
	public OfflinePerson getPerson()
	{
		return player;
	}
	
	public boolean isModShop()
	{
		return player == null;
	}
	
	public boolean isOwner(OfflinePlayer player)
	{
		if (this.player != null && player.getUniqueId().compareTo(this.player.getOfflinePlayer().getUniqueId()) == 0) return true;
		else return PeopleManager.getOfflinePerson(player).isAdmin() && player.isOnline() && player.getPlayer().getGameMode().equals(GameMode.CREATIVE);
	}
	
	public String getName()
	{
		if (player == null) return "The moderators";
		else return player.getNameRaw();
	}
}