package com.ruinscraft.keyboi;

import org.bukkit.inventory.ItemStack;

public class KeyRemovalHelper {
	private final String ID;
	private final ItemStack KEY;
	private final int MAX_COUNT;
	private int count;
	
	public KeyRemovalHelper(String id, ItemStack key, int max) {
		this.ID = id;
		this.KEY = key;
		this.MAX_COUNT = max;
		this.count = 0;
	}
	
	public void increment() {
		this.count++;
	}
	
	public boolean reachedMax() {
		return count >= MAX_COUNT;
	}
	
	public boolean isKey(ItemStack key) {
		return this.KEY.equals(key);
	}
}
