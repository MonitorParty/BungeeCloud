package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketLoginSuccess implements NetworkPacket {

    public long systemTime;
    
    public PacketLoginSuccess(long time) {
	this.systemTime = time;
    }
    
    public PacketLoginSuccess() {
	
    }
    
    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeLong(systemTime);
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	this.systemTime = r.readLong();
    }

    @Override
    public void handle() {
    }

    @Override
    public int getPacketId() {
	return 0x01;
    }

}
