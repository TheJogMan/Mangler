package preMangler.itemNameIndex;

import java.util.ArrayList;

import globalResources.commander.AbstractExecutor;
import globalResources.commander.CommandParent;
import globalResources.commander.PickyCommand;
import globalResources.discorse.ArgumentConsumptionResult;
import globalResources.discorse.argument.Argument;
import globalResources.discorse.argument.MultiListInterpretationResult;
import jogLib.commander.executor.PlayerExecutor;

public class IndexCommand extends PickyCommand<PlayerExecutor>
{
	public IndexCommand(CommandParent parent)
	{
		super(parent, "ItemIndex", "Opens an index of all item names for shops.");
		argumentList.addArgument(SearchArgument.class);
		argumentList.lock();
	}
	
	@Override
	protected void executePickyCommand(MultiListInterpretationResult result, PlayerExecutor executor)
	{
		new Index(executor.getPlayer(), (String)result.getValues()[0]);
	}
	
	public static class SearchArgument implements Argument<String>
	{
		@Override
		public void init(Object[] data)
		{
			
		}
		
		@Override
		public String getName()
		{
			return "Filter";
		}
		
		@Override
		public void getCompletions(String argumentString, ArrayList<String> completions, AbstractExecutor executor)
		{
			
		}
		
		@Override
		public ArgumentConsumptionResult<String> consume(String string, AbstractExecutor executor)
		{
			return new ArgumentConsumptionResult<>(true, string, string, "Valid", executor, this);
		}
	}
}