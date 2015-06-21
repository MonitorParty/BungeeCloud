package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketConsoleCommand implements NetworkPacket {
    
    public String command;
    
    public PacketConsoleCommand() {
	
    }
    
    public PacketConsoleCommand(String command) {
	this.command = command;
    }

    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeString(command);
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	command = r.readString();
    }

    @Override
    public void handle() {
    }

    @Override
    public int getPacketId() {
	return 0x06;
    }
    
    
}
