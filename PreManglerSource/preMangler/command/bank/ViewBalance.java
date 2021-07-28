package preMangler.command.bank;

import org.bukkit.OfflinePlayer;

import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.ArgumentList;
import globalResources.discorse.argument.MultiListInterpretationResult;
import jogLib.OfflinePlayerArgument;
import jogLib.commander.executor.PlayerExecutor;
import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;

public class ViewBalance extends AbstractCommand
{
	OfflinePlayerArgument argument;
	
	public ViewBalance(CommandParent parent)
	{
		super(parent, "ViewBalance", "Displays your current balance.");
		argumentList.addArgument(OfflinePlayerArgument.class);
		argumentList.addList(new ArgumentList());
		argumentList.lock();
	}
	
	@Override
	public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
	{
		OfflinePerson person = null;
		if (result.listNumber() == 1)
		{
			person = PeopleManager.getOfflinePerson((OfflinePlayer)result.getValues()[0]);
		}
		else if (executor instanceof PlayerExecutor)
		{
			person = PeopleManager.getPerson(((PlayerExecutor)executor).getPlayer());
		}
		else
		{
			executor.respond("You must specify the player whose balance you want to view.");
			return;
		}
		
		executor.respond(person.getColoredName() + "'s balance is $" + person.getCashBalance());
	}
}