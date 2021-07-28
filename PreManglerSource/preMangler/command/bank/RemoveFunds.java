package preMangler.command.bank;

import org.bukkit.OfflinePlayer;

import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.MultiListInterpretationResult;
import globalResources.discorse.arguments.PositiveIntegerArgument;
import jogLib.OfflinePlayerArgument;
import jogLib.commander.executor.ConsoleExecutor;
import jogLib.commander.executor.PlayerExecutor;
import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;

public class RemoveFunds extends AbstractCommand
{
	public RemoveFunds(CommandParent parent)
	{
		super(parent, "RemoveFunds", "Removes funds from a player's current cash balance.");
		argumentList.addArgument(OfflinePlayerArgument.class);
		argumentList.addArgument(PositiveIntegerArgument.class, "Amount");
		argumentList.lock();
	}
	
	@Override
	public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
	{
		if (hasAuthority(executor))
		{
			OfflinePerson person = PeopleManager.getOfflinePerson((OfflinePlayer)result.getValues()[0]);
			int amount = (int)result.getValues()[1];
			int balance = person.getCashBalance();
			if (balance >= amount)
			{
				balance += amount;
				person.setCashBalance(balance);
				executor.respond("$" + amount + " has been removed from " + person.getColoredName() + "'s balance, their balance is now $" + balance + ".");
			}
			else
			{
				executor.respond("Could not remove $" + amount + " from " + person.getColoredName() + "'s balance, their balance is only $" + balance + ".");
			}
		}
		else
			executor.respond("You have to be an admin to use this command.");
	}
	
	boolean hasAuthority(AbstractExecutor executor)
	{
		if (executor instanceof ConsoleExecutor)
		{
			return true;
		}
		else if (executor instanceof PlayerExecutor)
		{
			return PeopleManager.getOfflinePerson(((PlayerExecutor)executor).getPlayer()).isAdmin();
		}
		else
			return false;
	}
}