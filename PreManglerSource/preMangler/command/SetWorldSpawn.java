package preMangler.command;

import org.bukkit.Location;

import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.MultiListInterpretationResult;
import jogLib.commander.arguments.LocationArgument;
import preMangler.GeneralData;

public class SetWorldSpawn extends AbstractCommand
{
	public SetWorldSpawn(CommandParent parent)
	{
		super(parent, "SetWorldSpawn", "Sets the worlds spawnpoint.");
		argumentList.addArgument(LocationArgument.class);
		argumentList.lock();
	}
	
	@Override
	public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
	{
		GeneralData.setWorldSpawn((Location)result.getValues()[0]);
	}
}