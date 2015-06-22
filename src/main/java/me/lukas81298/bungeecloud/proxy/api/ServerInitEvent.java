package me.lukas81298.bungeecloud.proxy.api;

import java.util.UUID;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Event;

public class ServerInitEvent extends Event {

    private UUID serverId;
    
    private ServerInfo serverInfo;
    
    private String gamemode;

    public ServerInitEvent(UUID serverId, ServerInfo serverInfo, String map) {
	super();
	this.serverId = serverId;
	this.serverInfo = serverInfo;
	this.gamemode = map;
    }

    public UUID getServerId() {
        return serverId;
    }
    
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    
    public String getGameMode() {
        return gamemode;
    }

    public void setGameMode(String map) {
        this.gamemode = map;
    }
    
    
    
}
