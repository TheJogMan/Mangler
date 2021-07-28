package preMangler.roles;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import preMangler.person.PeopleManager;

public enum Role
{
	CONSOLE					(false,	Integer.MAX_VALUE,		ChatColor.GRAY,			"Console",					"Server console."),
	OWNER					(false,	Integer.MAX_VALUE - 1,	ChatColor.GRAY,			"Owner",					"Server Owner."),
	DEVELOPER				(false,	Integer.MAX_VALUE - 2,	ChatColor.GRAY,			"Developer",				"Plugin Developer."),
	RAILMASTER				(true,	Integer.MAX_VALUE - 3,	ChatColor.DARK_RED,		"Rail Master",				"Master of the rails."),
	CONDUCTOR				(true,	Integer.MAX_VALUE - 4,	ChatColor.DARK_RED,		"Conductor",				"Traffic cop."),
	ADMINISTRATOR			(false,	Integer.MAX_VALUE - 5,	ChatColor.GRAY,			"Administrator",			"Server administrator."),
	ENGINE_OF_PRODUCTION	(true,	1,						ChatColor.GOLD,			"Engine of Production",		"Powering the production line!"),
	SPANNERS_IN_THE_WORKS	(true,	1,						ChatColor.YELLOW,		"Spanners in the Works",	"A spanner."),
	COGS_IN_THE_MACHINE		(true,	1,						ChatColor.DARK_GREEN,	"Cogs in the Machine",		"Meshing with teeth."),
	GEAR_HEADS				(true,	1,						ChatColor.BLUE,			"Gear Heads",				"Gear for a head."),
	YOUTUBE_MEMBER			(true,	1,						ChatColor.RED,			"Youtube Member",			"Watching a youtube video."),
	TWITCH_SUBSCRIBER		(true,	1,						ChatColor.DARK_PURPLE,	"Twitch Subscriber",		"Watching a twitch stream."),
	ADBLOCK_ABSOLUTION		(true,	1,						ChatColor.GREEN,		"Adblock Absolution",		"Blocking the ads!"),
	FACTORY_WORKER			(true,	0,						ChatColor.DARK_GRAY,	"Factory Worker",			"Awaiting a promotion."),
	
	;
	
	public static Role getDefaultRole()
	{
		return FACTORY_WORKER;
	}
	
	public static Role getMinAdminAuthority()
	{
		return ADMINISTRATOR;
	}
	
	public static Role getAuthority(CommandSender sender)
	{
		while (sender instanceof ProxiedCommandSender) sender = ((ProxiedCommandSender)sender).getCaller();
		if (sender instanceof BlockCommandSender || sender instanceof CommandMinecart || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) return CONSOLE;
		else if (sender instanceof Player) return PeopleManager.getPerson((Player)sender).getAuthority();
		else return getDefaultRole();
	}
	
	int authorityLevel;
	String name;
	String description;
	ChatColor chatColor;
	boolean visible;
	
	Role(boolean visible, int authorityLevel, ChatColor chatColor, String name, String description)
	{
		this.visible = visible;
		this.authorityLevel = authorityLevel;
		this.chatColor = chatColor;
		this.name = name;
		this.description = description;
	}
	
	public int getPower()
	{
		return authorityLevel;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getTextModifier()
	{
		return "§" + chatColor.getChar();
	}
	
	public String getColoredName()
	{
		return getTextModifier() + getName() + "§r";
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public boolean visible()
	{
		return visible;
	}
	
	public boolean superior(Role otherRole)
	{
		return getPower() > otherRole.getPower();
	}
	
	public boolean sufficient(Role otherRole)
	{
		return getPower() >= otherRole.getPower();
	}
}