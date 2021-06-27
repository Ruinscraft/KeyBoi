// current: v1.16
package com.ruinscraft.keyboi;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public class KeyBoi extends JavaPlugin {
	private KeyListener key;

    @Override
    public void onEnable() {

    	this.key = new KeyListener(this);

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