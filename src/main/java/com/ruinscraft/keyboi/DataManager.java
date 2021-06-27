package com.ruinscraft.keyboi;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class DataManager {
	public KeyBoi plugin;
	
	public static final String KEY_IS_LOCKED = "keyboi-islocked";
	public static final String KEY_KEYNAME = "keyboi-keyname";
	public static final String KEY_KEYMATERIAL = "keyboi-keymaterial";
	public static final String KEY_KEYCREATOR = "keyboi-keycreator";
	public static final String KEY_HASH = "keyboi-hash";
	public static final String KEY_LOCK_OWNER = "keyboi-lockowner";
	
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
			else if(meta instanceof BookMeta) {
				BookMeta bm = (BookMeta) meta;
				
				if(bm.hasTitle()) {
					keyName = bm.getTitle();
				}
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
		if(key == null || key.getType().equals(Material.AIR)) {
			return false;
		}
		
		if(key.hasItemMeta()) {
			ItemMeta keyMeta = key.getItemMeta();
			PersistentDataContainer keyData = keyMeta.getPersistentDataContainer();
			
			String keyName = null;
			String keyCreator = keyData.get(new NamespacedKey(plugin, "keyboi-creator"), PersistentDataType.STRING);
			String hash = keyData.get(new NamespacedKey(plugin, "keyboi-hash"), PersistentDataType.STRING);
			
			if(keyCreator == null || hash == null) {
				return false;
			}
			
			if(keyMeta.hasDisplayName()) {
				keyName = keyMeta.getDisplayName();
			}
			else if(keyMeta instanceof BookMeta) {
				BookMeta bm = (BookMeta) keyMeta;
				
				if(bm.hasTitle()) {
					keyName = bm.getTitle();
				}
			}
			else {
				keyName = key.getType().toString();
			}
			
			return keyName.equals(lock.get(new NamespacedKey(plugin, KEY_KEYNAME), PersistentDataType.STRING))
				&& key.getType().name().equals(lock.get(new NamespacedKey(plugin, KEY_KEYMATERIAL), PersistentDataType.STRING))
				&& keyCreator.equals(lock.get(new NamespacedKey(plugin, KEY_KEYCREATOR), PersistentDataType.STRING))
				&& hash.equals(lock.get(new NamespacedKey(plugin, KEY_HASH), PersistentDataType.STRING));
		}
		else {
			return false;
		}
	}
	
	public HashMap<String, String> getLockKeyData(PersistentDataContainer lock) {
		HashMap<String, String> data = new HashMap<String, String>(0);
		
		String keyCreator = lock.get(new NamespacedKey(plugin, KEY_KEYCREATOR), PersistentDataType.STRING);
		
		if(!keyCreator.equalsIgnoreCase("N/A")){
			keyCreator = Bukkit.getOfflinePlayer(UUID.fromString(keyCreator)).getName();
		}
		
		data.put(KEY_KEYNAME, lock.get(new NamespacedKey(plugin, KEY_KEYNAME), PersistentDataType.STRING));
		data.put(KEY_KEYMATERIAL, lock.get(new NamespacedKey(plugin, KEY_KEYMATERIAL), PersistentDataType.STRING));
		data.put(KEY_KEYCREATOR, keyCreator);
		data.put(KEY_HASH, lock.get(new NamespacedKey(plugin, KEY_HASH), PersistentDataType.STRING));
		data.put(KEY_LOCK_OWNER, lock.get(new NamespacedKey(plugin, KEY_LOCK_OWNER), PersistentDataType.STRING));
		
		return data;
	}
	
	public static String computeMD5Hash(ItemStack key) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(itemTo64(key).getBytes());
		    byte[] digest = md.digest();
		    char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	        char[] hexChars = new char[digest.length * 2];
	        for (int j = 0; j < digest.length; j++) {
	            int v = digest[j] & 0xFF;
	            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	        }
	        return new String(hexChars);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static String itemTo64(ItemStack stack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);

            // Serialize that array
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }
}
