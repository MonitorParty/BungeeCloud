package me.lukas81298.bungeecloud.proxy;

import java.util.Map;

import com.google.common.collect.Maps;

public class ServerProperties {

    private int memory;
    private int slots;
    private String gamemode;
    private Map<String, String> properties = Maps.newConcurrentMap();

    public ServerProperties(int memory, int slots, String gamemode) {
	super();
	this.memory = memory;
	this.slots = slots;
	this.gamemode = gamemode;
    }

    public int getMemory() {
	return memory;
    }

    public void setMemory(int memory) {
	this.memory = memory;
    }

    public int getSlots() {
	return slots;
    }

    public void setSlots(int slots) {
	this.slots = slots;
    }

    public String getGamemode() {
	return gamemode;
    }

    public void setGamemode(String gamemode) {
	this.gamemode = gamemode;
    }

    public Map<String, String> getProperties() {
	return properties;
    }

}
