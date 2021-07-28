package preMangler.command.bank;

import java.util.Iterator;

import org.bukkit.Bukkit;

import globalResources.commander.CommandParent;
import globalResources.commander.PickyCommand;
import globalResources.discorse.argument.MultiListInterpretationResult;
import globalResources.utilities.SecureList;
import jogLib.commander.executor.ConsoleExecutor;
import jogLib.core.OfflineNameTracker;
import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;

public class ResetAccounts extends PickyCommand<ConsoleExecutor>
{
	public ResetAccounts(CommandParent parent)
	{
		super(parent, "ResetAccounts", "Resets everyone's cash balances to $0.");
	}
	
	@Override
	public void executePickyCommand(MultiListInterpretationResult result, ConsoleExecutor executor)
	{
		SecureList<String> names = OfflineNameTracker.getOfflineNames();
		for (Iterator<String> iterator = names.iterator(); iterator.hasNext();)
		{
			OfflinePerson person = PeopleManager.getOfflinePerson(Bukkit.getOfflinePlayer(OfflineNameTracker.getID(iterator.next())));
			person.setCashBalance(0);
		}
	}
}