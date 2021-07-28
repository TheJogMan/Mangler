package preMangler.command.roles;

import org.bukkit.OfflinePlayer;

import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.MultiListInterpretationResult;
import jogLib.OfflinePlayerArgument;
import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;

public class ViewPerson extends AbstractCommand
{
	public ViewPerson(CommandParent parent)
	{
		super(parent, "ViewPerson", "Shows information about a player.");
		argumentList.addArgument(OfflinePlayerArgument.class);
		argumentList.lock();
	}
	
	@Override
	public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
	{
		OfflinePerson person = PeopleManager.getOfflinePerson((OfflinePlayer)result.getValues()[0]);
		executor.respond(person.getColoredName() + "'s rank is " + person.getRank().getColoredName() + " and their authority level is " + person.getAuthority().getColoredName());
	}
}