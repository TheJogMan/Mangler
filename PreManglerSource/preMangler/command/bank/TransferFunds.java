package preMangler.command.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.ArgumentList;
import globalResources.discorse.argument.MultiListInterpretationResult;
import globalResources.discorse.arguments.PositiveIntegerArgument;
import jogLib.OfflinePlayerArgument;
import jogLib.commander.executor.ConsoleExecutor;
import jogLib.commander.executor.PlayerExecutor;
import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;

public class TransferFunds extends AbstractCommand
{
	OfflinePlayerArgument argument;
	
	public TransferFunds(CommandParent parent)
	{
		super(parent, "TransferFunds", "Transfers funds from one player to another.");
		argumentList.addArgument(OfflinePlayerArgument.class, "Recipient");
		argumentList.addArgument(PositiveIntegerArgument.class, "Amount");
		ArgumentList list2 = new ArgumentList();
		list2.addArgument(OfflinePlayerArgument.class, "Recipient");
		list2.addArgument(PositiveIntegerArgument.class, "Amount");
		list2.addArgument(OfflinePlayerArgument.class, "Sender");
		list2.lock();
		argumentList.addList(list2);
		argumentList.lock();
	}
	
	boolean hasAuthority(AbstractExecutor executor)
	{
		if (executor instanceof ConsoleExecutor)
		{
			return true;
		}
		else if (executor instanceof PlayerExecutor)
		{
			return PeopleManager.getOfflinePerson(((Player)executor).getPlayer()).isAdmin();
		}
		else
			return false;
	}
	
	@Override
	public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
	{
		OfflinePerson recipient;
		OfflinePerson sender;
		int amount;
		
		if (result.listNumber() == 0)
		{
			if (executor instanceof PlayerExecutor)
			{
				sender = PeopleManager.getOfflinePerson(((PlayerExecutor)executor).getPlayer());
				recipient = PeopleManager.getOfflinePerson((OfflinePlayer)result.getValues()[0]);
				amount = (int)result.getValues()[1];
			}
			else
			{
				executor.respond("You must specify which player will be sending money.");
				return;
			}
		}
		else
		{
			if (hasAuthority(executor))
			{
				sender = PeopleManager.getOfflinePerson((OfflinePlayer)result.getValues()[2]);
				recipient = PeopleManager.getOfflinePerson((OfflinePlayer)result.getValues()[0]);
				amount = (int)result.getValues()[1];
			}
			else
			{
				executor.respond("You must be an admin to transfer funds from another player's balance.");
				return;
			}
		}
		
		int senderBalance = sender.getCashBalance();
		int recipientBalance = recipient.getCashBalance();
		if (senderBalance >= amount)
		{
			senderBalance -= amount;
			recipientBalance += amount;
			if (result.listNumber() == 0) sender.sendMessage("$" + amount + " has been transfered to " + recipient.getColoredName());
			else sender.sendMessage("$" + amount + " has been transfered from " + sender.getColoredName() + " to " + recipient.getColoredName() + ", their balances are now $" + senderBalance + " and $" + recipientBalance);
			recipient.sendMessage(sender.getColoredName() + " has transfered $" + amount + " to you.");
			sender.setCashBalance(senderBalance);
			recipient.setCashBalance(recipientBalance);
		}
		else
		{
			if (result.listNumber() == 0) sender.sendMessage("Transaction failed! " + sender.getColoredName() + "'s balance is too low to transfer $" + amount);
			else sender.sendMessage("Transaction failed! Your balance is to low to transfer $" + amount);
		}
	}
}