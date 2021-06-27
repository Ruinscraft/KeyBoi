package com.ruinscraft.keyboi;

public class OutputMessage {
	
	private String message;
	private long timestamp;
	
	public OutputMessage(String message) {
		this(message, System.currentTimeMillis());
	}
	
	public OutputMessage(String message, long timestamp) {
		this.message = message;
		this.timestamp = timestamp;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setCurrentTimestamp() {
		this.timestamp = System.currentTimeMillis();
	}
	/**
	 * Checks if the difference between the current time and the
	 * output message's timestamp is >= some length of time, in milliseconds
	 * @param length Span of time, in milliseconds
	 * @return true if difference is >= length
	 */
	public boolean hasExpired(long length) {
		long currentTime = System.currentTimeMillis();
		long difference = currentTime - this.timestamp;
		
		return difference >= length;
	}
}
