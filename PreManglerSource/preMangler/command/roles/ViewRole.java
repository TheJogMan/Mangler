package preMangler.command.roles;

import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.MultiListInterpretationResult;
import preMangler.roles.Role;
import preMangler.roles.RoleArgument;

public class ViewRole extends AbstractCommand
{
	public ViewRole(CommandParent parent)
	{
		super(parent, "ViewRole", "Shows more information about a role.");
		argumentList.addArgument(RoleArgument.class);
		argumentList.lock();
	}
	
	@Override
	public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
	{
		Role role = (Role)result.getValues()[0];
		executor.respond("Name: " + role.getColoredName() + "\nAuthority Level: " + role.getPower() + "\nVisible: " + role.visible() + "\nDescription: " + role.getDescription());
	}
}