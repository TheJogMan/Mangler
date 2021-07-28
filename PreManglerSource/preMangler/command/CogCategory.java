package preMangler.command;

import globalResources.commander.AbstractCategory;
import globalResources.commander.AbstractCommand;
import globalResources.commander.AbstractExecutor;
import globalResources.commander.CategoryParent;
import globalResources.commander.CommandParent;
import globalResources.discorse.argument.MultiListInterpretationResult;
import globalResources.discorse.arguments.BooleanArgument;
import jogLib.commander.executor.ConsoleExecutor;
import jogLib.commander.executor.PlayerExecutor;
import preMangler.GeneralData;
import preMangler.command.bank.AddFunds;
import preMangler.command.bank.RemoveFunds;
import preMangler.command.bank.ResetAccounts;
import preMangler.command.bank.TransferFunds;
import preMangler.command.bank.ViewBalance;
import preMangler.command.roles.GiveRole;
import preMangler.command.roles.RemoveRole;
import preMangler.command.roles.ViewPerson;
import preMangler.command.roles.ViewRole;
import preMangler.itemNameIndex.IndexCommand;
import preMangler.person.PeopleManager;

public class CogCategory extends AbstractCategory
{
	public CogCategory(CategoryParent parent)
	{
		super(parent, "Cog", "Mangler commands.");
		
		new SetWorldSpawn(this);
		new Inv(this);
		new GiveRole(this);
		new RemoveRole(this);
		new ViewRole(this);
		new ViewPerson(this);
		new IndexCommand(this);
		
		new AddFunds(this);
		new RemoveFunds(this);
		new ResetAccounts(this);
		new TransferFunds(this);
		new ViewBalance(this);
		new LockedMode(this);
	}
	
	public class LockedMode extends AbstractCommand
	{
		public LockedMode(CommandParent parent)
		{
			super(parent, "SetLockedMode", "Sets whether the server is locked to just administrators.");
			argumentList.addArgument(BooleanArgument.class);
		}
		
		@Override
		public void executeCommand(MultiListInterpretationResult result, AbstractExecutor executor)
		{
			if (executor instanceof ConsoleExecutor || (executor instanceof PlayerExecutor && PeopleManager.getPerson(((PlayerExecutor)executor).getPlayer()).isAdmin()))
			{
				GeneralData.setLockedMode((boolean)result.getValues()[0]);
			}
			else
			{
				executor.respond("You need to be an admin to use this command.");
			}
		}
	}
}