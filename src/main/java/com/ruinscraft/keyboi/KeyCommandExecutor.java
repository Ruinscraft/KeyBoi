package com.ruinscraft.keyboi;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class KeyCommandExecutor implements CommandExecutor, TabCompleter{
	private final KeyBoi plugin;
	
	private final String PLUGIN_BANNER = ChatColor.GOLD + "---------------[ KeyBoi ]---------------";
	
	private final String MSG_SUCCESSFULLY_CREATED_KEY = ChatColor.GREEN + "Successfully created %d key(s)!";
	//private final String MSG_ERROR_NO_PERMISSION = ChatColor.RED + "You do not have permission to use that command.";
	private final String MSG_ERROR_NO_ITEM_IN_HAND = ChatColor.RED + "You need to hold an item before creating a key.";
	
	private final List<String> tabOptions;
	private final List<String> adminTabOptions;
	private final List<String> tutorialText;

	public KeyCommandExecutor(KeyBoi plugin) {
		this.plugin = plugin;
		this.tabOptions = new ArrayList<String>();
		this.adminTabOptions = new ArrayList<String>();
		this.tutorialText = new ArrayList<String>();

		tabOptions.add("create");
		tabOptions.add("tutorial");
		
		tutorialText.add(ChatColor.GOLD + "--- How to create a key ---");
		tutorialText.add(ChatColor.AQUA + " 1. Find an item or block you want to make into a key");
		tutorialText.add(ChatColor.AQUA + " 2. With the item or block in hand, type \"/key create\"");
		tutorialText.add(ChatColor.AQUA + " 3. Your item(s) are now keys");
		tutorialText.add(ChatColor.AQUA + " Note: this will create keys out of every item in the stack.");
		tutorialText.add(ChatColor.AQUA + " ");
		tutorialText.add(ChatColor.GOLD + "--- How to create a padlock ---");
		tutorialText.add(ChatColor.AQUA + " 1. Place a sign on a door, trapdoor, gate, chest, or barrel");
		tutorialText.add(ChatColor.AQUA + " 2. On the first line, put \"[Key]\" (case-insensitive)");
		tutorialText.add(ChatColor.AQUA + " 3. Right-click on the lock sign with a key");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if(args.length >= 1) {
			switch(args[0].toLowerCase()){
				case "create":
					createKey(player);
					break;
				case "tutorial":
					showTutorial(player);
					break;
				default:
					showHelp(player);
					break;
			}
		}
		else {
			showHelp(player);
		}
		
		return true;
	}
	

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String option : tabOptions) {
                if (option.startsWith(args[0].toLowerCase())) {
                    completions.add(option);
                }
            }
            
            // TODO: I can't think of any admin commands, so this section will likely be removed
            if(sender.hasPermission("keyboi.admin")) {
            	for (String option : adminTabOptions) {
                    if (option.startsWith(args[0].toLowerCase())) {
                        completions.add(option);
                    }
                }
            }
        }
        
        return completions;
	}
	
	private void showHelp(Player player) {
		String[] commandHelpBase = {
			ChatColor.DARK_AQUA + "  /key" + ChatColor.AQUA + " create" + ChatColor.GRAY + ": Creates key(s) from a stack of items",
			ChatColor.DARK_AQUA + "  /key" + ChatColor.AQUA + " tutorial" + ChatColor.GRAY + ": How to create a KeyBoi lock",
		};
		
		String[] commandHelpAdmin = {
		};
			
		if(player.isOnline()) {
			player.sendMessage(this.PLUGIN_BANNER);
			player.sendMessage(commandHelpBase);
			
			// TODO: I can't think of any admin commands, so this section will likely be removed
			if(player.hasPermission("keyboi.admin")) {
				player.sendMessage(commandHelpAdmin);
			}
		}
	}

	private void createKey(Player caller) {
		PlayerInventory callerInventory = caller.getInventory();
		ItemStack itemInHand = callerInventory.getItemInMainHand();
		if(itemInHand != null && !itemInHand.getType().equals(Material.AIR)) {
			
			ItemMeta meta = itemInHand.getItemMeta();
			PersistentDataContainer pdc = meta.getPersistentDataContainer();
			
			NamespacedKey keycreatorKey = new NamespacedKey(plugin, "keyboi-creator");
    		NamespacedKey hashKey = new NamespacedKey(plugin, "keyboi-hash");
    		
			List<String> loreList = new ArrayList<String>();
			loreList.add(ChatColor.GOLD + "-- Key --");
			loreList.add(ChatColor.GRAY + "This item may open");
			loreList.add(ChatColor.GRAY + "a locked door or chest");
			loreList.add(ChatColor.GRAY + "somewhere in the world...");
			loreList.add("");
			loreList.add(ChatColor.GRAY + "Creator: " + caller.getName());
			meta.setLore(loreList);
			
			if(itemIsFinishedBook(itemInHand)) {
				BookMeta bookmeta = (BookMeta) meta;
				bookmeta.setGeneration(Generation.TATTERED);
				itemInHand.setItemMeta(bookmeta);
			}
			
			pdc.set(keycreatorKey, PersistentDataType.STRING, caller.getUniqueId().toString());
			pdc.set(hashKey, PersistentDataType.STRING, DataManager.computeMD5Hash(itemInHand));
			
			itemInHand.setItemMeta(meta);
			
			if(caller.isOnline()) {
				caller.sendMessage(String.format(MSG_SUCCESSFULLY_CREATED_KEY, itemInHand.getAmount()));
			}
		}
		else {
			if(caller.isOnline()) {
				caller.sendMessage(MSG_ERROR_NO_ITEM_IN_HAND);
			}
		}
	}
	
	private void showTutorial(Player caller) {
		if(caller.isOnline()) {
			for(String line : this.tutorialText) {
				caller.sendMessage(line);
			}
		}
	}
	
	private boolean itemIsFinishedBook(ItemStack item) {
    	return item != null && item.getType().equals(Material.WRITTEN_BOOK);
    }
}