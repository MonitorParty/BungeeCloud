package me.lukas81298.bungeecloud.spigot;

import java.net.Socket;

import me.lukas81298.bungeecloud.InstanceType;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotCloud extends JavaPlugin implements me.lukas81298.bungeecloud.Instance {

    private Socket socket;

    public Socket getSocket() {
	return this.socket;
    }

    @Override
    public void onDisable() {
	super.onDisable();
    }

    @Override
    public void onEnable() {
	super.onEnable();
    }

    @Override
    public InstanceType getType() {
	return InstanceType.SPIGOT;
    }

    @Override
    public void shutdown() {
	Bukkit.shutdown();
    }

    @Override
    public boolean isServer() {
	return false;
    }

    @Override
    public boolean isClient() {
	return true;
    }

    @Override
    public boolean isConnected() {
	return this.socket.isConnected();
    }

}
