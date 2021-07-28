package preMangler.command;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.commander.PickyCommand;
import globalResources.discorse.AIU;
import globalResources.discorse.ArgumentConsumptionResult;
import globalResources.discorse.argument.Argument;
import globalResources.discorse.argument.MultiListInterpretationResult;
import jogLib.OnlinePlayerArgument;
import jogLib.commander.executor.PlayerExecutor;
import preMangler.person.PeopleManager;
import preMangler.person.Person;

public class Inv extends PickyCommand<PlayerExecutor>
{
	public Inv(CommandParent parent)
	{
		super(parent, "Inv", "Views another player's inventory");
		argumentList.addArgument(OnlinePlayerArgument.class);
		argumentList.addArgument(InvArgument.class);
		argumentList.lock();
	}
	
	@Override
	public void executePickyCommand(MultiListInterpretationResult result, PlayerExecutor executor)
	{
		Person person = PeopleManager.getPerson(executor.getPlayer());
		if (person.isAdmin())
		{
			person.getPlayer().openInventory(((InvType)result.getValues()[1]).getInventory((Player)result.getValues()[0]));
		}
		else
		{
			person.sendMessage("You must be an admin to use this command.");
		}
	}
	
	enum InvType
	{
		INVENTORY	("PlayerInventory",	(player) -> player.getInventory()),
		ENDER_CHEST	("EnderChest",		(player) -> player.getEnderChest());
		
		String name;
		InventoryFetcher fetcher;
		
		InvType(String name, InventoryFetcher fetcher)
		{
			this.name = name;
			this.fetcher = fetcher;
		}
		
		String getName()
		{
			return name;
		}
		
		Inventory getInventory(Player player)
		{
			return fetcher.get(player);
		}
		
		interface InventoryFetcher
		{
			Inventory get(Player player);
		}
		
		static InvType getFromName(String name)
		{
			InvType[] options = InvType.values();
			for (int index = 0; index < options.length; index++) if (name.equals(options[index].getName())) return options[index];
			return null;
		}
	}
	
	public static class InvArgument implements Argument<InvType>
	{
		@Override
		public void init(Object[] data)
		{
			
		}
		
		@Override
		public String getName()
		{
			return "Inventory Type";
		}
		
		@Override
		public void getCompletions(String argumentString, ArrayList<String> completions, AbstractExecutor executor)
		{
			InvType[] options = InvType.values();
			for (int index = 0; index < options.length; index++) completions.add(options[index].getName());
		}
		
		@Override
		public ArgumentConsumptionResult<InvType> consume(String string, AbstractExecutor executor)
		{
			String token = AIU.quickConsume(string).getConsumed();
			InvType type = InvType.getFromName(token);
			if (type == null)
			{
				return new ArgumentConsumptionResult<>(false, InvType.INVENTORY, token, "Not a valid inventory type.", executor, this);
			}
			else
			{
				return new ArgumentConsumptionResult<>(true, type, token, "Valid", executor, this);
			}
		}
	}
}