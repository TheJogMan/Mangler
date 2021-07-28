package preMangler.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HatCommand implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3)
	{
		if (arg0 instanceof Player)
		{
			Player player = (Player)arg0;
			ItemStack helmet = player.getEquipment().getHelmet();
			ItemStack item = player.getEquipment().getItemInMainHand();
			boolean mainHand = true;
			if (item == null)
			{
				item = player.getEquipment().getItemInOffHand();
				mainHand = false;
			}
			if (item != null)
			{
				if (mainHand) player.getEquipment().setItemInMainHand(helmet);
				else player.getEquipment().setItemInOffHand(helmet);
				player.getEquipment().setHelmet(item);
			}
			else player.sendMessage("You must be holding an item to put on your head.");
		}
		else arg0.sendMessage("Only a player can use this command.");
		return true;
	}
}