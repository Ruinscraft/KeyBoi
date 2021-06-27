package com.ruinscraft.keyboi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;

public class KeyListener implements Listener{
	private KeyBoi plugin;
	
	private final String LOCK_SIGN_IDENTIFIER = ChatColor.DARK_BLUE + "[Key]";
	private final String LOCK_SIGN_IDENTIFIER_NO_COLOR = "[Key]";
	
	private final String MSG_KEY_SIGN_PLACED = ChatColor.GREEN + "Key sign placed! Use a key on the sign to set the lock.";
	private final String MSG_BLOCK_SUCCESSFULLY_LOCKED = ChatColor.GREEN + "Block successfully locked with key!";
	private final String MSG_DISPLAY_KEY_INFO = ChatColor.GREEN + "This block is locked with a key called %s (%s) created by %s.";
	private final String MSG_ERROR_NEED_KEY = ChatColor.YELLOW + "You need a " + ChatColor.BOLD + "key" + ChatColor.RESET + ChatColor.YELLOW + " to open this.";
	private final String MSG_ERROR_UNABLE_TO_LOCK = ChatColor.RED + "Unable to set key values for lock.";
	private final String MSG_ERROR_WRONG_KEY = ChatColor.YELLOW + "This key doesn't seem to fit the lock...";
	private final String MSG_ERROR_CANT_PLACE_KEY_BLOCK = ChatColor.YELLOW + "Can't place a block with key information";
	
	// used for messages that may output several times, to prevent spamming
	HashMap<UUID, OutputMessage> playerOutputMessages;
	
    public KeyListener(KeyBoi plugin) {
    	this.plugin = plugin;
    	this.playerOutputMessages = new HashMap<UUID, OutputMessage>();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
    	Player player = evt.getPlayer();
    	
    	if(playerOutputMessages.containsKey(player.getUniqueId())) {
    		playerOutputMessages.remove(player.getUniqueId());
    	}
    }
    
    @EventHandler
    public void onSignChangeEvent(SignChangeEvent evt) {
    	Player player = evt.getPlayer();
    	Block block = evt.getBlock();
    	
    	if(block.getState() instanceof Sign) {
	    	if(validateKeySignEntry(evt.getLines())) {
	    		evt.setLine(0, LOCK_SIGN_IDENTIFIER);
	    		
	    		Sign s = (Sign) block.getState();
	    		
	    		DataManager dm = new DataManager(plugin);
	    		
	    		if(dm.setNewSignKeyTags(player, s)){
		    		if(player.isOnline()) {
		    			player.sendMessage(MSG_KEY_SIGN_PLACED);
		    		}
	    		}
	    		else {
	    			if(player.isOnline()) {
	    				player.sendMessage(MSG_ERROR_UNABLE_TO_LOCK);
	    			}
	    		}
	    	}
    	}
    }
    
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent evt) {
    	Player player = evt.getPlayer();
    	ItemStack placed = evt.getItemInHand();

    	if(itemIsKey(placed)) {
    		evt.setCancelled(true);
    		setOutputMessage(player, MSG_ERROR_CANT_PLACE_KEY_BLOCK);
    	}
    }
    /**
     * This function handles all Player interactions with locked blocks.
     * @param evt - called Player interact event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Player player = evt.getPlayer();

        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	Block clickedBlock = evt.getClickedBlock();
        	BlockState state = clickedBlock.getState();
        	
        	if (blockIsLockable(clickedBlock)) {
        		Sign signInfo = blockHasKeySign(clickedBlock);
        		
        		if(blockIsDoor(clickedBlock) && signInfo == null) {
        			Door d = (Door) clickedBlock.getBlockData();
        			
        			if(d.getHalf() == Half.TOP) {
        				Block bottomHalf = clickedBlock.getRelative(BlockFace.DOWN);
        				signInfo = blockHasKeySign(bottomHalf);
        			}
        			else {
        				Block topHalf = clickedBlock.getRelative(BlockFace.UP);
        				signInfo = blockHasKeySign(topHalf);
        			}
        		}
        		
        		
        		if(signInfo != null) {
        			DataManager dm = new DataManager(plugin);
        			
        			PersistentDataContainer pdc = signInfo.getPersistentDataContainer();
        			
        			if(dm.containerHasKeyTags(pdc)) {
            			if(dm.isLocked(pdc)) {
            				evt.setCancelled(true);
            				
            				// partial fix for iron doors
            				if(evt.getHand() == EquipmentSlot.OFF_HAND){
            					evt.setCancelled(false);
            					return;
            				}
            				
            				if(player.isSneaking()) {
            					evt.setCancelled(false);
            					return;
            				}
            				
            				if(playerHoldingKey(player) || playerIsAdmin(player)) {
            					ItemStack key = player.getInventory().getItemInMainHand();
            					
            					if(dm.playerKeyMatchesLock(key, pdc) || playerIsAdmin(player)) {
            						if(blockIsDoor(clickedBlock) || blockIsTrapdoor(clickedBlock) || blockIsGate(clickedBlock)) {
            							Openable open = (Openable) state.getBlockData();
            							if(open.isOpen()) {
            								open.setOpen(false);
            							}
            							else {
            								open.setOpen(true);
            							}
            							
            							state.setBlockData(open);
            						}
            						else if(blockIsStorage(clickedBlock)) {
            							Container container = (Container) state;
            							
            							player.openInventory(container.getInventory());
            						}
            						
            						state.update();
            					}
            					else {
            						setOutputMessage(player, MSG_ERROR_WRONG_KEY);
            					}
            				}
            				else {
            					setOutputMessage(player, MSG_ERROR_NEED_KEY);
            				}
            			}
        			}
        		}
        	}
        	else if(blockIsSign(clickedBlock)){
        		Sign sign = (Sign) state;
        		if(signIsKeySign(sign)) {
        			DataManager dm = new DataManager(plugin);
        			
        			PersistentDataContainer pdc = sign.getPersistentDataContainer();
        			
        			if(dm.playerOwnsLock(player, pdc) || playerIsAdmin(player)) {
	        			if(dm.isLocked(pdc)) {
	        				if(player.isOnline()) {
	        					HashMap<String, String> keyData = dm.getLockKeyData(pdc);
	        					player.sendMessage(String.format(MSG_DISPLAY_KEY_INFO, prettyPrint(keyData.get(DataManager.KEY_KEYNAME)), prettyPrint(keyData.get(DataManager.KEY_KEYMATERIAL)), keyData.get(DataManager.KEY_KEYCREATOR)));
	        				}
	        			}
	        			else if(playerHoldingKey(player)){
	        				ItemStack key = player.getInventory().getItemInMainHand();
	        				
	        				if(!itemIsAir(key)) {
		        				dm.setKeyTags(player, key, sign);
		        				
		        				if(player.isOnline()) {
		        					player.sendMessage(MSG_BLOCK_SUCCESSFULLY_LOCKED);
		        				}
	        				}
	        			}
        			}
        		}
        	}
        }
    }
    
    private void setOutputMessage(Player player, String message) {
    	if(player.isOnline()) {
    		if(playerOutputMessages.containsKey(player.getUniqueId())) {
    			OutputMessage current = playerOutputMessages.get(player.getUniqueId());
    			
    			if(current.getMessage().equalsIgnoreCase(message)) {
    				if(current.hasExpired(10000)) {
    					current.setCurrentTimestamp();
    					player.sendMessage(message);
    				}
    			}
    			else {
    				current.setMessage(message);
    				current.setCurrentTimestamp();
    				player.sendMessage(message);
    			}
    		}
    		else {
    			OutputMessage current = new OutputMessage(message);
    			playerOutputMessages.put(player.getUniqueId(), current);
    			
    			player.sendMessage(message);
    		}
    	}
    }
    
    private boolean playerIsAdmin(Player player) {
    	return player.hasPermission("keyboi.admin");
    }
    
    private boolean playerHoldingKey(Player player) {
    	ItemStack inHand = player.getInventory().getItemInMainHand();
    	
    	return itemIsKey(inHand);
    }
    
    private boolean itemIsKey(ItemStack item) {
    	if(itemIsAir(item)) {
    		return false;
    	}
    	else {
    		if(item.hasItemMeta()) {
		    	ItemMeta meta = item.getItemMeta();
		    	PersistentDataContainer pdc = meta.getPersistentDataContainer();
		    	
		    	// TODO: clean this up
		    	return pdc.has(new NamespacedKey(plugin, "keyboi-creator"), PersistentDataType.STRING)
		    		&& meta.hasLore()
		    		&& meta.getLore().get(0).equals(ChatColor.GOLD + "-- Key --");
    		}
    		else {
    			return false;
    		}
    	}
    }
    
    private Sign blockHasKeySign(Block block) {
    	List<Block> surroundingBlocks = new ArrayList<Block>();
    	
    	surroundingBlocks.add(block.getRelative(BlockFace.NORTH));
    	surroundingBlocks.add(block.getRelative(BlockFace.SOUTH));
    	surroundingBlocks.add(block.getRelative(BlockFace.EAST));
    	surroundingBlocks.add(block.getRelative(BlockFace.WEST));
    	surroundingBlocks.add(block.getRelative(BlockFace.UP));
    	surroundingBlocks.add(block.getRelative(BlockFace.DOWN));
    	
    	for(Block b : surroundingBlocks) {
    		if(blockIsSign(b)) {
    			Sign s = (Sign) b.getState();
    			
    			if(signIsKeySign(s)) {
    				return s;
    			}
    		}
    	}
    	
    	return null;
    }
    
    private boolean blockIsLockable(Block block) {
    	return blockIsDoor(block) || blockIsTrapdoor(block) || blockIsGate(block) || blockIsStorage(block);
    }
    
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
    
	private boolean blockIsDoor(Block block) {
		return block != null && (
    		    block.getType().equals(Material.ACACIA_DOOR)
    		 || block.getType().equals(Material.BIRCH_DOOR)
    		 || block.getType().equals(Material.CRIMSON_DOOR)
    		 || block.getType().equals(Material.DARK_OAK_DOOR)
    		 || block.getType().equals(Material.IRON_DOOR)
    		 || block.getType().equals(Material.JUNGLE_DOOR)
    		 || block.getType().equals(Material.OAK_DOOR)
    		 || block.getType().equals(Material.SPRUCE_DOOR)
    		 || block.getType().equals(Material.WARPED_DOOR)
    		 );
	}
    
	private boolean blockIsTrapdoor(Block block) {
		return block != null && (
    		    block.getType().equals(Material.ACACIA_TRAPDOOR)
    		 || block.getType().equals(Material.BIRCH_TRAPDOOR)
    		 || block.getType().equals(Material.CRIMSON_TRAPDOOR)
    		 || block.getType().equals(Material.DARK_OAK_TRAPDOOR)
    		 || block.getType().equals(Material.IRON_TRAPDOOR)
    		 || block.getType().equals(Material.JUNGLE_TRAPDOOR)
    		 || block.getType().equals(Material.OAK_TRAPDOOR)
    		 || block.getType().equals(Material.SPRUCE_TRAPDOOR)
    		 || block.getType().equals(Material.WARPED_TRAPDOOR)
    		 );
	}
	
	private boolean blockIsGate(Block block) {
		return block != null && (
    		    block.getType().equals(Material.ACACIA_FENCE_GATE)
    		 || block.getType().equals(Material.BIRCH_FENCE_GATE)
    		 || block.getType().equals(Material.CRIMSON_FENCE_GATE)
    		 || block.getType().equals(Material.DARK_OAK_FENCE_GATE)
    		 || block.getType().equals(Material.JUNGLE_FENCE_GATE)
    		 || block.getType().equals(Material.OAK_FENCE_GATE)
    		 || block.getType().equals(Material.SPRUCE_FENCE_GATE)
    		 || block.getType().equals(Material.WARPED_FENCE_GATE)
    		 );
	}
    /**
     * Checks whether a block is a chest, double chest, or barrel
     * @param block - Block to check
     * @return True if block is chest, False otherwise
     */
    private boolean blockIsStorage(Block block) {
    	return block != null && (
    			block.getState() instanceof Chest
    		 || block.getState() instanceof DoubleChest
    		 || block.getState() instanceof Barrel
    		 );
    }
    
    private boolean signIsKeySign(Sign s) {
    	return s.getLine(0).equalsIgnoreCase(LOCK_SIGN_IDENTIFIER);
    }
    private boolean validateKeySignEntry(String[] lines) {
    	return lines[0].equalsIgnoreCase(LOCK_SIGN_IDENTIFIER_NO_COLOR);
    }
    
    private String prettyPrint(String str) {
    	String[] words = str.split("_");
    	String output = "";
    	
    	for( String word : words) {
    		output += word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() + " ";
    	}
    	output = output.trim();
    	return output;
    }
    
    private boolean itemIsAir(ItemStack item) {
    	return item != null && item.getType().equals(Material.AIR);
    }
}