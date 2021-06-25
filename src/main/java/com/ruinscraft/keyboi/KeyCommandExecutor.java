package com.ruinscraft.keyboi;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KeyCommandExecutor implements CommandExecutor, TabCompleter{
	private final KeyBoi plugin;
	
	private final String PLUGIN_BANNER = ChatColor.GOLD + "---------------[ KeyBoi ]---------------";
	private final String MSG_ERROR_NO_PERMISSION = ChatColor.RED + "You do not have permission to use that command.";
	
	private final List<String> tabOptions;
	private final List<String> adminTabOptions;
	private final List<String> tutorialText;

	public KeyCommandExecutor(KeyBoi plugin) {
		this.plugin = plugin;
		this.tabOptions = new ArrayList<String>();
		this.adminTabOptions = new ArrayList<String>();
		this.tutorialText = new ArrayList<String>();

		tabOptions.add("balance");
		tabOptions.add("top");
		tabOptions.add("tutorial");
		tabOptions.add("view");
		tabOptions.add("withdraw");
		
		
		adminTabOptions.add("history");
		
		tutorialText.add(ChatColor.GOLD + "--- How to create a chest shop ---");
		tutorialText.add(ChatColor.AQUA + " 1. Place a sign on or above a chest.");
		tutorialText.add(ChatColor.AQUA + " 2. On the first line, put \"[Buy]\" (case-insensitive)");
		tutorialText.add(ChatColor.AQUA + " 3. On the third line, put \"x for $y\", where x is the quantity of the item, and y is the price in gold ingots.");
		tutorialText.add(ChatColor.AQUA + " 4. Finally, right-click the sign with the item to sell in your hand.");
		tutorialText.add(ChatColor.AQUA + " 5. Place all items for sale in the chest.");
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if(args.length >= 1) {
			switch(args[0].toLowerCase()){
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
            
            if(sender.hasPermission("keyboi.admin")) {
            	for (String option : adminTabOptions) {
                    if (option.startsWith(args[0].toLowerCase())) {
                        completions.add(option);
                    }
                }
            }
        }
        
        if (args.length == 2) {
            if(args[0].equalsIgnoreCase("view")) {
            	completions.add("recent");
            }
            else if( (args[0].equalsIgnoreCase("history") || args[0].equalsIgnoreCase("balance") )&& sender.hasPermission("dukesmart.shop.admin")) {
            	for(Player p : Bukkit.getOnlinePlayers()) {
            		completions.add(p.getName());
            	}
            }
        }

        return completions;
	}
	
	private void showHelp(Player player) {
		String[] commandHelpBase = {
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " balance" + ChatColor.GRAY + ": Check your ledger balance",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " top" + ChatColor.GRAY + ": View top 10 earners",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " tutorial" + ChatColor.GRAY + ": How to create a chest shop",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " withdraw ($)" + ChatColor.GRAY + ": Removes money from your ledger",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " view recent" + ChatColor.GRAY + ": View ten most recent transactions for a shop"
		};
		
		String[] commandHelpAdmin = {
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " balance (player)" + ChatColor.GRAY + ": Check a player's ledger balance", 
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " history (player)" + ChatColor.GRAY + ": View ten most recent transactions made by a player"
		};
			
		if(player.isOnline()) {
			player.sendMessage(this.PLUGIN_BANNER);
			player.sendMessage(commandHelpBase);
			
			if(player.hasPermission("dukesmart.shop.admin")) {
				player.sendMessage(commandHelpAdmin);
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
	/**
	 * Checks if a given string is a number
	 * 
	 * @param str - String to check
	 * @return True if the string consists of only numbers, False otherwise
	 */
	private boolean stringIsNumeric(String str) {
		for(char c : str.toCharArray()) {
			if(!Character.isDigit(c)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Safely converts a string consisting of numeric values
	 * into an integer. If the value of the number is greater than
	 * an integer's max value, it will truncate the value to it.
	 * 
	 * @param str - String to check for numeric value
	 * @return int value of the string, or -1 on error
	 */
	private int safeStringToInt(String str) {
		if(stringIsNumeric(str)) {
			if(str.length() > 10) {
				str = str.substring(0, 10);
			}

			if(Double.parseDouble(str) > Integer.MAX_VALUE) {
				return Integer.MAX_VALUE - 1;
			}
			else {
				return Integer.parseInt(str);
			}
		}
		return -1;
	}
}