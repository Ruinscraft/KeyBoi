// current: v1.16
package com.ruinscraft.keyboi;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;

public class KeyBoi extends JavaPlugin {
	private KeyListener key;
	public HashMap<UUID, KeyRemovalHelper> playerRemoveKeyDataMap;
	
    @Override
    public void onEnable() {

    	this.key = new KeyListener(this);
    	this.playerRemoveKeyDataMap = new HashMap<UUID, KeyRemovalHelper>();
    	
    	this.getCommand("key").setExecutor(new KeyCommandExecutor(this));
    	this.getCommand("key").setTabCompleter(this);
    	
    	Bukkit.getPluginManager().registerEvents(this.key, this);
    	
    	getLogger().info("KeyBoi has been enabled!");
    }

    @Override
    public void onDisable() {
    	getLogger().info("KeyBoi has been disabled!");
    }

}