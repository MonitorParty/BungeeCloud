package me.lukas81298.bungeecloud.proxy;

import java.io.IOException;
import java.util.UUID;

import me.lukas81298.bungeecloud.network.packets.PacketConsoleCommand;
import me.lukas81298.bungeecloud.network.packets.PacketCustomData;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GameServer {

    private int playerCount;
    private int slots;
    private UUID serverUUID;
    private String gamemode;
    private Client client;
    private ServerInfo serverInfo;
    
    public GameServer(int playerCount, int slots, UUID serverUUID, String gamemode, ServerInfo serverInfo) {
	super();
	this.playerCount = playerCount;
	this.slots = slots;
	this.serverUUID = serverUUID;
	this.gamemode = gamemode;
	this.serverInfo = serverInfo;
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

    public String getGamemode() {
	return gamemode;
    }

    public void setGamemode(String gamemode) {
	this.gamemode = gamemode;
    }

    public void sendData(String channel, byte[] data) throws IOException {
	if(client == null) {
	    throw new IllegalStateException("Client is not connected.");
	}
	client.sendPacket(new PacketCustomData(channel, data));
    }
    
    public void executeConsoleCommand(String command) throws IOException  {
	if(client == null) {
	    throw new IllegalStateException("Client is not connected.");
	}
	client.sendPacket(new PacketConsoleCommand(command));
    }
    
    public void connect(ProxiedPlayer player) {
	if(serverInfo == null) {
	    throw new IllegalStateException("Client is not connected.");
	}
	player.connect(serverInfo);
    }

    public Client getClient() {
	return client;
    }

    public void setClient(Client client) {
	this.client = client;
    }

    public void setServerUUID(UUID serverUUID) {
	this.serverUUID = serverUUID;
    }
    
    public ServerInfo getServerInfo() {
	return this.serverInfo;
    }

}
