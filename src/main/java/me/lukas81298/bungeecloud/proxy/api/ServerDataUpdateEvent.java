package me.lukas81298.bungeecloud.proxy.api;

import java.util.UUID;

import net.md_5.bungee.api.plugin.Event;

public class ServerDataUpdateEvent extends Event {

    private final UUID serverUUID;

    private String data;

    private int playerCount;

    private int state;

    public ServerDataUpdateEvent(UUID serverUUID, String data, int playerCount, int state) {
	super();
	this.serverUUID = serverUUID;
	this.data = data;
	this.playerCount = playerCount;
	this.state = state;
    }

    public UUID getServerUUID() {
	return serverUUID;
    }

    public String getData() {
	return data;
    }

    public void setData(String data) {
	this.data = data;
    }

    public int getPlayerCount() {
	return playerCount;
    }

    public void setPlayerCount(int playerCount) {
	this.playerCount = playerCount;
    }

    public int getState() {
	return state;
    }

    public void setState(int state) {
	this.state = state;
    }

}
