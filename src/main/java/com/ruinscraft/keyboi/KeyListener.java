package com.ruinscraft.keyboi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatColor;

public class KeyListener implements Listener{
	private KeyBoi plugin;
	
	private HashMap<Player, BukkitTask> hideDisplayTasks  = new HashMap<Player, BukkitTask>();
	
	private final int HIDE_SHOP_DISPLAY_SECONDS = 15;
	private final String SHOP_SIGN_NO_ITEM      = "" + ChatColor.WHITE + "?";
	private final String SHOP_SIGN_IDENTIFIER   = "" + ChatColor.DARK_PURPLE + "[Buy]";
	private final String SHOP_SIGN_OWNER_COLOR  = "" + ChatColor.DARK_BLUE;
	
	private final String PLUGIN_NAME = ChatColor.GOLD + "DukesMart";

    public KeyListener(KeyBoi plugin) {
    	this.plugin = plugin;
    }
 
    /*
     * This will handle shop creation if a player
     * places a sign with proper values.
     */
    @EventHandler
    public void onSignChangeEvent(SignChangeEvent evt) {
    	Player player = evt.getPlayer();
    	Block block = evt.getBlock();
    	
    	if(block.getState() instanceof Sign) {
    		evt.setLine(0, SHOP_SIGN_IDENTIFIER);
			evt.setLine(1, SHOP_SIGN_NO_ITEM);
    		evt.setLine(3, SHOP_SIGN_OWNER_COLOR + player.getName());

    		player.sendMessage(ChatColor.AQUA + "Hold an item you want to sell and right-click the sign to finish setup.");
    	}
    }
    
    /**
     * This function handles all Player interactions with a shop sign.
     * @param evt - called Player interact event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Player player = evt.getPlayer();

        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	Block clickedBlock = evt.getClickedBlock();
        	
            if (blockIsSign(clickedBlock)){
            	
            	
            	// a shop is defined as a sign (formatted)
            	// and a chest block immediately below it.
                Sign sign = (Sign) clickedBlock.getState();

                BlockData data = clickedBlock.getBlockData();
                Block block = null;
                // first, get the block that the sign is attached to

                if (data instanceof Directional)
                {
                    Directional directional = (Directional)data;
                    block = clickedBlock.getRelative(directional.getFacing().getOppositeFace());
                }
            }
        }
    }

	/**
     * Updates a sign's text to reflect any changes,
     * such as item or owner's name
     * 
     * @param sign Sign to update
     */
	private void updateSign(Sign sign) {
		Location location = sign.getLocation();
	}

	@EventHandler
    public void onBlockBreak(BlockBreakEvent evt) {
    	Player player = evt.getPlayer();
    	Block  block  = evt.getBlock();
    }
	
	/**
	 * Checks whether a block is a sign.
	 * @param block
	 * @return True if block is sign, False otherwise
	 */
	private boolean blockIsSign(Block block) {
    	return block != null && (
    		    block.getType().equals(Material.ACACIA_WALL_SIGN)
    		 || block.getType().equals(Material.BIRCH_WALL_SIGN)
    		 || block.getType().equals(Material.CRIMSON_WALL_SIGN)
    		 || block.getType().equals(Material.DARK_OAK_WALL_SIGN)
    		 || block.getType().equals(Material.JUNGLE_WALL_SIGN)
    		 || block.getType().equals(Material.OAK_WALL_SIGN)
    		 || block.getType().equals(Material.SPRUCE_WALL_SIGN)
    		 || block.getType().equals(Material.WARPED_WALL_SIGN)
    		 );
    }
    
    /**
     * Checks whether a block is a chest, double chest, or barrel
     * (note that Enderchests are not checked)
     * @param block - Block to check
     * @return True if block is chest, False otherwise
     */
    private boolean blockIsStorage(Block block) {
    	return block != null && (block.getState() instanceof Chest || block.getState() instanceof DoubleChest || block.getState() instanceof Barrel);
    }
    
    private String materialPrettyPrint(Material material) {
    	String[] words = material.toString().split("_");
    	String output = "";
    	
    	for( String word : words) {
    		output += word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() + " ";
    	}
    	output = output.trim();
    	return output;
    }
    
	private String truncateText(String message) {
    	if(message.length() >= 38) {
    		return message.substring(0, 34) + "...";
    	}
    	else {
    		return message;
    	}
    }
    
    private String prettyPrint(String message) {
    	String[] words = message.split("_");
    	String  output = "";
    	
    	for( String word : words) {
    		output += word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() + " ";
    	}
    	output = output.trim();
    	return output;
    }
    
    private String shopGuiPad(String message) {
    	message += ChatColor.RESET;
    	
    	while(message.length() < 32) {
    		message += " ";
    	}
    	
    	return message;
    }
    
	/**
     * Returns the Location data of a shop sign
     * @param s Sign representing a shop
     * @return Sign location data
     */
    private Location getShopLocation(Sign s) {
		return s.getLocation();
	}

    /**
     * Sends an error message to the player.
     * @param p Player to send message to
     * @param message Message to output
     */
    private void sendError(Player player, String message) {
    	if(player.isOnline()) {
    		player.sendMessage(ChatColor.RED + message);
    	}
    }

    private boolean itemIsFinishedBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.WRITTEN_BOOK.parseMaterial());
    }
    
    private boolean itemIsWritableBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.WRITABLE_BOOK.parseMaterial());
    }
    
    private boolean itemIsAir(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.AIR.parseMaterial());
    }
    
    private boolean itemIsBanner(ItemStack item) {
    	return item != null && item.getType().name().contains("BANNER");
    }
    
    private boolean itemIsShield(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.SHIELD.parseMaterial());
    }
    
    private boolean itemIsPotion(ItemStack item) {
		return item != null && item.getType().name().contains("POTION");
	}
    
    private boolean itemIsFilledMap(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.FILLED_MAP.parseMaterial());
    }
    
    private boolean itemIsShulkerBox(ItemStack item) {
    	return item != null && item.getType().name().contains("SHULKER_BOX");
    }
    
    private boolean itemIsEnchantedBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.ENCHANTED_BOOK.parseMaterial());
    }
    
    private boolean itemIsTippedArrow(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.TIPPED_ARROW.parseMaterial());
    }
    
    private boolean itemIsPolishedBlackstone(ItemStack item) {
    	return item != null && item.getType().name().contains("POLISHED_BLACKSTONE");
    }
    
    private boolean itemIsPlayerHead(ItemStack itemToBuy) {
		return itemToBuy != null && itemToBuy.getType().equals(Material.PLAYER_HEAD);
	}
}