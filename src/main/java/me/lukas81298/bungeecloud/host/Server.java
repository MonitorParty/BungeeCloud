package me.lukas81298.bungeecloud.host;

import java.util.UUID;

public class Server {
    
    private UUID uuid;
    private Process process;
    private int memory;
    private int slot;
    private String gameMode;
    
    public Server(UUID uuid, Process process, int memory, int slot, String gameMode) {
	super();
	this.uuid = uuid;
	this.process = process;
	this.memory = memory;
	this.slot = slot;
	this.gameMode = gameMode;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
    
    
    
    
}
