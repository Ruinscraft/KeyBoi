package com.ruinscraft.keyboi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DataManager {
	public static KeyBoi plugin;
	
	private static final String KEY_IS_LOCKED = "keyboi-islocked";
	private static final String KEY_KEYNAME = "keyboi-keyname";
	private static final String KEY_KEYMATERIAL = "keyboi-keymaterial";
	private static final String KEY_KEYCREATOR = "keyboi-keycreator";
	private static final String KEY_HASH = "keyboi-hash";
	private static final String KEY_LOCK_OWNER = "keyboi-lockowner";
	
	public DataManager(KeyBoi plugin) {
		this.plugin = plugin;
	}
	
	public boolean setKeyTags(Player owner, ItemStack key, Sign s) {
		PersistentDataContainer pdc = s.getPersistentDataContainer();
		String keyName = null;
		String keyCreator = null;
		String hash = null;
		String keyOwner = null;
		
		if(owner != null) {
			keyOwner = owner.getUniqueId().toString();
		}
		else {
			return false;
		}
		
		if(key.hasItemMeta()) {
			ItemMeta meta = key.getItemMeta();
			PersistentDataContainer keyPdc = meta.getPersistentDataContainer();
			NamespacedKey keycreatorKey = new NamespacedKey(plugin, "keyboi-creator");
			NamespacedKey hashKey = new NamespacedKey(plugin, "keyboi-hash");
			
			if(meta.hasDisplayName()) {
				keyName = key.getItemMeta().getDisplayName();
			}
			else {
				keyName = key.getType().toString();
			}
			
			if(keyPdc.has(keycreatorKey, PersistentDataType.STRING)) {
				keyCreator = keyPdc.get(keycreatorKey, PersistentDataType.STRING);
			}
			else {
				return false;
			}
			
			if(keyPdc.has(hashKey, PersistentDataType.STRING)) {
				hash = keyPdc.get(hashKey, PersistentDataType.STRING);
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
		
		pdc.set(new NamespacedKey(plugin, KEY_IS_LOCKED), PersistentDataType.STRING, "true");
		pdc.set(new NamespacedKey(plugin, KEY_KEYNAME), PersistentDataType.STRING, keyName);
		pdc.set(new NamespacedKey(plugin, KEY_KEYMATERIAL), PersistentDataType.STRING, key.getType().name());
		pdc.set(new NamespacedKey(plugin, KEY_KEYCREATOR), PersistentDataType.STRING, keyCreator);
		pdc.set(new NamespacedKey(plugin, KEY_HASH), PersistentDataType.STRING, hash);
		if(keyOwner != null) {
			pdc.set(new NamespacedKey(plugin, KEY_LOCK_OWNER), PersistentDataType.STRING, keyOwner);
		}
		
		s.update();
		return true;
	}
	
	public boolean setNewSignKeyTags(Player owner, Sign s) {
		PersistentDataContainer pdc = s.getPersistentDataContainer();
		if(owner == null) {
			return false;
		}
		
		pdc.set(new NamespacedKey(plugin, KEY_IS_LOCKED), PersistentDataType.STRING, "false");
		pdc.set(new NamespacedKey(plugin, KEY_KEYNAME), PersistentDataType.STRING, "N/A");
		pdc.set(new NamespacedKey(plugin, KEY_KEYMATERIAL), PersistentDataType.STRING, "N/A");
		pdc.set(new NamespacedKey(plugin, KEY_KEYCREATOR), PersistentDataType.STRING, "N/A");
		pdc.set(new NamespacedKey(plugin, KEY_HASH), PersistentDataType.STRING, "N/A");
		pdc.set(new NamespacedKey(plugin, KEY_LOCK_OWNER), PersistentDataType.STRING, owner.getUniqueId().toString());
		s.update();
		return true;
	}
	
	public boolean containerHasKeyTags(PersistentDataContainer pdc) {
		return pdc.has(new NamespacedKey(plugin, KEY_IS_LOCKED), PersistentDataType.STRING)
			&& pdc.has(new NamespacedKey(plugin, KEY_KEYNAME), PersistentDataType.STRING)
			&& pdc.has(new NamespacedKey(plugin, KEY_KEYMATERIAL), PersistentDataType.STRING)
			&& pdc.has(new NamespacedKey(plugin, KEY_KEYCREATOR), PersistentDataType.STRING)
			&& pdc.has(new NamespacedKey(plugin, KEY_HASH), PersistentDataType.STRING)
			&& pdc.has(new NamespacedKey(plugin, KEY_LOCK_OWNER), PersistentDataType.STRING);
    }
	
	public String containerToString(PersistentDataContainer pdc) {
		String keyCreator = pdc.get(new NamespacedKey(plugin, KEY_KEYCREATOR), PersistentDataType.STRING);
		
		if(!keyCreator.equalsIgnoreCase("N/A")){
			keyCreator = Bukkit.getOfflinePlayer(UUID.fromString(keyCreator)).getName();
		}
		return "Is Locked: " + pdc.get(new NamespacedKey(plugin, KEY_IS_LOCKED), PersistentDataType.STRING) + "\n"
		     + "Key Name: " + pdc.get(new NamespacedKey(plugin, KEY_KEYNAME), PersistentDataType.STRING) + "\n"
		     + "Key Material: " + pdc.get(new NamespacedKey(plugin, KEY_KEYMATERIAL), PersistentDataType.STRING) + "\n"
		     + "Key Creator: " + keyCreator + "\n"
		     + "Hash: " + pdc.get(new NamespacedKey(plugin, KEY_HASH), PersistentDataType.STRING) + "\n"
		     + "Lock Owner: " + Bukkit.getOfflinePlayer(UUID.fromString(pdc.get(new NamespacedKey(plugin, KEY_LOCK_OWNER), PersistentDataType.STRING))).getName();
	}
	
	public boolean isLocked(PersistentDataContainer pdc) {
		return pdc.get(new NamespacedKey(plugin, KEY_IS_LOCKED), PersistentDataType.STRING).equalsIgnoreCase("true");
	}
	
	public boolean playerOwnsLock(Player player, PersistentDataContainer pdc) {
		String ownerUUID = pdc.get(new NamespacedKey(plugin, KEY_LOCK_OWNER), PersistentDataType.STRING);
		
		return player.getUniqueId().toString().equals(ownerUUID);
	}
	
	public boolean playerKeyMatchesLock(ItemStack key, PersistentDataContainer lock) {
		ItemMeta keyMeta = key.getItemMeta();
		PersistentDataContainer keyData = keyMeta.getPersistentDataContainer();
		
		String keyName = null;
		String keyCreator = keyData.get(new NamespacedKey(plugin, "keyboi-creator"), PersistentDataType.STRING);
		
		if(keyMeta.hasDisplayName()) {
			keyName = keyMeta.getDisplayName();
		}
		else {
			keyName = key.getType().toString();
		}
		
		return keyName.equals(lock.get(new NamespacedKey(plugin, KEY_KEYNAME), PersistentDataType.STRING))
			&& key.getType().name().equals(lock.get(new NamespacedKey(plugin, KEY_KEYMATERIAL), PersistentDataType.STRING))
			&& keyCreator.equals(lock.get(new NamespacedKey(plugin, KEY_KEYCREATOR), PersistentDataType.STRING));
	}
}
