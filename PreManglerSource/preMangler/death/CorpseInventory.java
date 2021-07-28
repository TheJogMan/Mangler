package preMangler.death;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import globalResources.utilities.dataSet.DataSet;
import jogLib.core.ItemHandler;

public class CorpseInventory implements InventoryHolder
{
	Entity corpse;
	Inventory inventory;
	OfflinePlayer owner;
	boolean keep = false;
	
	public CorpseInventory(Entity corpse, ItemStack[] items, OfflinePlayer owner)
	{
		this.corpse = corpse;
		this.owner = owner;
		inventory = Bukkit.createInventory(this, 54, corpse.getCustomName());
		inventory.addItem(items);
		keep = true;
		save();
	}
	
	public CorpseInventory(Entity corpse)
	{
		this.corpse = corpse;
		inventory = Bukkit.createInventory(this, 54, corpse.getCustomName());
		keep = true;
		load();
	}
	
	static boolean validCorpse(Entity entity)
	{
		return entity.getPersistentDataContainer().has(Corpse.dataKey, new PersistentDataSetType());
	}
	
	@Override
	public Inventory getInventory()
	{
		return inventory;
	}
	
	public int getRemainingItems()
	{
		int count = 0;
		ItemStack[] contents = inventory.getContents();
		for (int index = 0; index < contents.length; index++) if (contents[index] != null) count += contents[index].getAmount();
		return count;
	}
	
	public boolean isOwner(OfflinePlayer player)
	{
		return owner.getUniqueId().compareTo(player.getUniqueId()) == 0;
	}
	
	void save()
	{
		DataSet data = new DataSet();
		data.set("Contents", ItemHandler.exportItemStacks(inventory.getContents()));
		data.set("OwnerID", owner.getUniqueId());
		PersistentDataContainer container = corpse.getPersistentDataContainer();
		container.set(Corpse.dataKey, new PersistentDataSetType(), data);
	}
	
	void load()
	{
		DataSet data = corpse.getPersistentDataContainer().getOrDefault(Corpse.dataKey, new PersistentDataSetType(), new DataSet());
		inventory.setContents(ItemHandler.importItemStacks(data.getByteArray("Contents", new byte[0])));
		owner = Bukkit.getOfflinePlayer(data.getUUID("OwnerID", UUID.randomUUID()));
	}
	
	void closeAll()
	{
		List<HumanEntity> viewers = inventory.getViewers();
		Stack<HumanEntity> closed = new Stack<HumanEntity>();
		for (Iterator<HumanEntity> iterator = viewers.iterator(); iterator.hasNext();) closed.push(iterator.next());
		while (!closed.isEmpty()) closed.pop().closeInventory();
	}
	
	void unload()
	{
		closeAll();
		save();
	}
	
	void update()
	{
		List<HumanEntity> viewers = inventory.getViewers();
		boolean itemHeld = false;
		Stack<HumanEntity> closed = new Stack<HumanEntity>();
		for (Iterator<HumanEntity> iterator = viewers.iterator(); iterator.hasNext();)
		{
			HumanEntity viewer = iterator.next();
			InventoryView view = viewer.getOpenInventory();
			if (viewer.getLocation().distance(corpse.getLocation()) > 5.5) closed.push(viewer);
			else if (view.getCursor() != null && view.getCursor().getAmount() > 0) itemHeld = true;
		}
		while (!closed.isEmpty()) closed.pop().closeInventory();
		if (getRemainingItems() == 0 && !itemHeld) keep = false;
		save();
	}
	
	static class PersistentDataSetType implements PersistentDataType<byte[], DataSet>
	{
		@Override
		public DataSet fromPrimitive(byte[] primitive, PersistentDataAdapterContext context)
		{
			return new DataSet(primitive);
		}
		
		@Override
		public Class<DataSet> getComplexType()
		{
			return DataSet.class;
		}
		
		@Override
		public Class<byte[]> getPrimitiveType()
		{
			return byte[].class;
		}
		
		@Override
		public byte[] toPrimitive(DataSet complex, PersistentDataAdapterContext context)
		{
			return complex.getAsBytes();
		}
	}
}