package preMangler.itemNameIndex;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import preMangler.shop.ShopManager;

public class Index implements InventoryHolder
{
	Inventory screen;
	Player player;
	int page;
	int pageCount;
	ArrayList<Material> itemMaterials = new ArrayList<Material>();
	
	public Index(Player player, String search)
	{
		if (search != null) search = search.toLowerCase();
		screen = Bukkit.createInventory(this, 54, "Item Name Index");
		this.player = player;
		itemMaterials = new ArrayList<Material>();
		Material[] mats = Material.values();
		for (int index = 0; index < mats.length; index++) if (mats[index].isItem())
		{
			String name = ShopManager.getName(mats[index], false);
			if (search == null || name.toLowerCase().contains(search)) itemMaterials.add(mats[index]);
		}
		double rawPages = itemMaterials.size() / 45;
		pageCount = (int)rawPages;
		if (pageCount < rawPages) pageCount++;
		setPage(0);
		player.openInventory(screen);
	}
	
	@Override
	public Inventory getInventory()
	{
		return screen;
	}
	
	public int getPage()
	{
		return page;
	}
	
	public void setPage(int page)
	{
		this.page = page;
		screen.clear();
		int amount = 0;
		for (int index = 45 * page; index < itemMaterials.size() && amount <= 45; index++)
		{
			amount++;
			screen.addItem(createItem(itemMaterials.get(index), ShopManager.getName(itemMaterials.get(index), true)));
		}
		if (page > 0) screen.setItem(45, createItem(Material.LIME_STAINED_GLASS_PANE, "Previous Page"));
		else screen.setItem(45, createItem(Material.GRAY_STAINED_GLASS_PANE, "Previous Page"));
		double step = pageCount / 7.0;
		double current = 0.0;
		for (int index = 46; index < 53; index++)
		{
			Material material = Material.BLACK_STAINED_GLASS_PANE;
			if (index == 46)
			{
				if (page == 0) material = Material.YELLOW_STAINED_GLASS_PANE;
				else material = Material.WHITE_STAINED_GLASS_PANE;
			}
			else if (index == 47 && page > 0 && page < step) material = Material.YELLOW_STAINED_GLASS_PANE;
			else if (index == 51 && page < pageCount && page >= ((double)pageCount - step)) material = Material.YELLOW_STAINED_GLASS_PANE;
			else if (index == 52)
			{
				if (page == pageCount) material = Material.YELLOW_STAINED_GLASS_PANE;
				else material = Material.WHITE_STAINED_GLASS_PANE;
			}
			else
			{
				if (page >= current && page < current + step) material = Material.YELLOW_STAINED_GLASS_PANE;
				else material = Material.WHITE_STAINED_GLASS_PANE;
			}
			screen.setItem(index, createItem(material, "Page " + (page + 1)));
			current += step;
		}
		if (page < pageCount) screen.setItem(53, createItem(Material.LIME_STAINED_GLASS_PANE, "Next Page"));
		else screen.setItem(53, createItem(Material.GRAY_STAINED_GLASS_PANE, "Next Page"));
	}
	
	static ItemStack createItem(Material material, String name)
	{
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(material);
		if (meta != null)
		{
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}
		return item;
	}
	
	public int getPageCount()
	{
		return pageCount;
	}
}