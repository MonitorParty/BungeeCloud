package me.lukas81298.bungeecloud;

public interface Instance {

    public InstanceType getType();
    
    public void shutdown();
    
    public boolean isServer();
    
    public boolean isClient();
    
    public boolean isConnected();
}
