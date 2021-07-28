package preMangler.shop;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import preMangler.person.Person;

public class Shop
{
	Chest chest;
	Sign sign;
	int price;
	int amount;
	Material itemMaterial;
	ShopOwner owner;
	boolean buyingFromCustomer;
	
	Shop(Chest chest, Sign sign, Object[] signData)
	{
		this.chest = chest;
		this.sign = sign;
		buyingFromCustomer = (boolean)signData[0];
		itemMaterial = (Material)signData[1];
		price = (int)signData[2];
		owner = (ShopOwner)signData[3];
		amount = (int)signData[4];
	}
	
	public int getStock()
	{
		if (buyingFromCustomer) return 0;
		else
		{
			if (owner.isModShop()) return Integer.MAX_VALUE;
			else
			{
				ItemStack[] items = chest.getBlockInventory().getContents();
				int amount = 0;
				for (int index = 0; index < items.length; index++) if (items[index] != null && items[index].getType().equals(itemMaterial)) amount += items[index].getAmount();
				return amount;
			}
		}
	}
	
	public String getInfo()
	{
		String info = "";
		if (buyingFromCustomer)
			info += "Buying";
		else
			info += "Selling";
		info += " " + ShopManager.getName(itemMaterial, false);
		info += " at " + amount + " for $" + price + ", by " + owner.getName();
		if (!buyingFromCustomer)
			info += "\n" + getStock() + " items in stock.";
		return info;
	}
	
	public void transact(Person person)
	{
		if (isOwner(person.getPlayer()))
			person.sendMessage("That is your own shop.");
		else
		{
			if (buyingFromCustomer)
			{
				if (owner.isModShop() || owner.getPerson().getCashBalance() >= price)
				{
					int slot = person.getPlayer().getInventory().first(itemMaterial);
					int supply = ((slot == -1) ? 0 : person.getPlayer().getInventory().getItem(slot).getAmount());
					if (supply < amount)
						person.sendMessage("You do not have any of that item to sell.");
					else
					{
						ItemStack stack = person.getPlayer().getInventory().getItem(slot);
						ItemStack check = stack.clone();
						check.setAmount(amount);
						slot = getValidSlot(check, chest.getBlockInventory());
						if (!owner.isModShop() && slot == -1)
							person.sendMessage("The shop is full and can not buy any more items.");
						else
						{
							person.setCashBalance(person.getCashBalance() + price);
							stack.setAmount(stack.getAmount() - amount);
							if (!owner.isModShop())
							{
								owner.getPerson().setCashBalance(owner.getPerson().getCashBalance() - price);
								chest.getBlockInventory().addItem(check);
							}
						}
					}
				}
				else
					person.sendMessage("The shop owner's balance is too low to buy this item from you.");
			}
			else
			{
				if (person.getCashBalance() >= price)
				{
					int slot = chest.getBlockInventory().first(itemMaterial);
					int supply = ((slot == -1) ? 0 : chest.getBlockInventory().getItem(slot).getAmount());
					if (supply < amount)
						person.sendMessage("The shop is out of stock and can not sell any items.");
					else
					{
						ItemStack item = chest.getBlockInventory().getItem(slot);
						ItemStack sold = item.clone();
						sold.setAmount(amount);
						if (!owner.isModShop())
							item.setAmount(item.getAmount() - 1);
						int playerSlot = getValidSlot(sold, person.getPlayer().getInventory());
						if (playerSlot != -1)
							person.getPlayer().getInventory().addItem(sold);
						else
							person.getPlayer().getWorld().dropItem(person.getPlayer().getLocation(), sold);
						person.setCashBalance(person.getCashBalance() - price);
						if (!owner.isModShop())
						{
							owner.getPerson().setCashBalance(owner.getPerson().getCashBalance() + price);
						}
					}
				}
				else person.sendMessage("Your balance is too low to buy this item.");
			}
		}
	}
	
	public boolean isOwner(OfflinePlayer player)
	{
		return owner.isOwner(player);
	}
	
	public static int getValidSlot(ItemStack item, Inventory inventory)
	{
		ItemStack[] contents = inventory.getContents();
		for (int index = 0; index < contents.length; index++)
		{
			if (item.isSimilar(contents[index]) && contents[index].getAmount() + item.getAmount() <= contents[index].getType().getMaxStackSize())
				return index;
		}
		return inventory.firstEmpty();
	}
}