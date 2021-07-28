package preMangler.itemNameIndex;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

import jogLib.core.JogPlugin;

public class IndexManager implements Listener
{
	private static boolean initialized = false;
	
	public static void init(JogPlugin plugin)
	{
		if (!initialized)
		{
			Bukkit.getPluginManager().registerEvents(new IndexManager(), plugin);
			initialized = true;
		}
	}
	
	public static void disable()
	{
		for (Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator(); iterator.hasNext();)
		{
			Player player = iterator.next();
			InventoryView view = player.getOpenInventory();
			if (view != null && view.getTopInventory().getHolder() instanceof Index)
			{
				view.close();
				player.sendMessage("You have been kicked out of that screen because the plugin has been disabled, if this is due to a reload then you should be able to open it again in a few moments.");
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() != null)
		{
			if (event.getClickedInventory().getHolder() instanceof Index)
			{
				Index index = (Index)event.getClickedInventory().getHolder();
				event.setCancelled(true);
				int page = index.getPage();
				int previousPage = page;
				if (event.getRawSlot() == 45) page--;
				else if (event.getRawSlot() == 53) page++;
				if (page != previousPage)
				{
					if (page >= 0 && page <= index.getPageCount())
					{
						index.setPage(page);
					}
				}
			}
			else if (event.isShiftClick() && event.getView().getTopInventory().getHolder() instanceof Index) event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event)
	{
		Set<Integer> rawSlots = event.getRawSlots();
		for (Iterator<Integer> iterator = rawSlots.iterator(); iterator.hasNext();) if (event.getView().getInventory(iterator.next()).getHolder() instanceof Index)
		{
			event.setCancelled(true);
			return;
		}
	}
}