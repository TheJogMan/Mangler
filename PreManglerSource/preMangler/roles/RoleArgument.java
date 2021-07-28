package preMangler.roles;

import java.util.ArrayList;

import globalResources.commander.AbstractExecutor;
import globalResources.discorse.AIU;
import globalResources.discorse.ArgumentConsumptionResult;
import globalResources.discorse.argument.Argument;

public class RoleArgument implements Argument<Role>
{
	String[] roleNames;
	
	@Override
	public void init(Object[] data)
	{
		Role[] roles = Role.values();
		roleNames = new String[roles.length];
		for (int index = 0; index < roles.length; index++)
		{
			String name = roles[index].getName();
			String newName = "";
			for (int charNum = 0; charNum < name.length(); charNum++)
			{
				char ch = name.charAt(charNum);
				if (ch != ' ') newName += ch;
			}
			roleNames[index] = newName;
		}
	}
	
	@Override
	public String getName()
	{
		return "Role";
	}
	
	@Override
	public void getCompletions(String argumentString, ArrayList<String> completions, AbstractExecutor executor)
	{
		for (int index = 0; index < roleNames.length; index++)
		{
			completions.add(roleNames[index]);
		}
	}
	
	int indexOf(String arg)
	{
		arg = arg.toLowerCase();
		for (int index = 0; index < roleNames.length; index++) if (arg.compareTo(roleNames[index].toLowerCase()) == 0) return index;
		return -1;
	}
	
	@Override
	public ArgumentConsumptionResult<Role> consume(String string, AbstractExecutor executor)
	{
		String token = AIU.quickConsume(string).getConsumed();
		
		int index = indexOf(string);
		if (index == -1)
			return new ArgumentConsumptionResult<>(false, Role.getDefaultRole(), token, "There is no role by that name.", executor, this);
		else
			return new ArgumentConsumptionResult<>(true, Role.values()[index], token, "Valid", executor, this);
	}
}