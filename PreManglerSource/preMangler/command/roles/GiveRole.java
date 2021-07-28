package preMangler.command.roles;

import org.bukkit.OfflinePlayer;

import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.MultiListInterpretationResult;
import jogLib.OfflinePlayerArgument;
import jogLib.commander.executor.ConsoleExecutor;
import jogLib.commander.executor.PlayerExecutor;
import preMangler.person.OfflinePerson;
import preMangler.person.PeopleManager;
import preMangler.roles.Role;
import preMangler.roles.RoleArgument;

public class GiveRole extends AbstractCommand
{
	public GiveRole(CommandParent parent)
	{
		super(parent, "GiveRole", "Gives a role to a person.");
		argumentList.addArgument(OfflinePlayerArgument.class);
		argumentList.addArgument(RoleArgument.class);
		argumentList.lock();
	}
	
	@Override
	public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
	{
		OfflinePerson person = PeopleManager.getOfflinePerson((OfflinePlayer)result.getValues()[0]);
		Role role = (Role)result.getValues()[1];
		if (hasAuthority(executor, role))
		{
			if (!person.hasRole(role))
			{
				person.addRole(role);
				executor.respond("Role given.");
			}
			else executor.respond("That person already has that role.");
		}
		else executor.respond("Your authority level is too low to give this role.");
	}
	
	boolean hasAuthority(AbstractExecutor executor, Role role)
	{
		if (executor instanceof ConsoleExecutor)
			return true;
		else if (executor instanceof PlayerExecutor)
			return Role.getAuthority(((PlayerExecutor)executor).getPlayer()).sufficient(role);
		else
			return false;
	}
}