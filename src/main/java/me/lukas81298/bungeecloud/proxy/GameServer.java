package me.lukas81298.bungeecloud.proxy;

import java.util.UUID;

public class GameServer {

    private int playerCount;
    private int slots;
    private UUID serverUUID;
    private String gamemode;

    public GameServer(int playerCount, int slots, UUID serverUUID, String gamemode) {
	super();
	this.playerCount = playerCount;
	this.slots = slots;
	this.serverUUID = serverUUID;
	this.gamemode = gamemode;
    }

    public int getPlayerCount() {
	return playerCount;
    }

    public void setPlayerCount(int playerCount) {
	this.playerCount = playerCount;
    }

    public int getSlots() {
	return slots;
    }

    public void setSlots(int slots) {
	this.slots = slots;
    }

    public UUID getServerUUID() {
	return serverUUID;
    }

    public void setServerUUID(UUID serverUUID) {
	this.serverUUID = serverUUID;
    }

    public String getGamemode() {
	return gamemode;
    }

    public void setGamemode(String gamemode) {
	this.gamemode = gamemode;
    }

}
