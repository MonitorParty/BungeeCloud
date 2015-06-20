package me.lukas81298.bungeecloud.network;

import java.io.IOException;

public interface NetworkPacket {

    public void writePacketData(PacketDataWriter w) throws IOException;

    public void readPacketData(PacketDataReader r) throws IOException;

    public void handle();
    
    public int getPacketId();
    
}
