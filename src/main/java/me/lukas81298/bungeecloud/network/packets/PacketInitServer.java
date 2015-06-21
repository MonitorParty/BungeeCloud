package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;
import java.util.UUID;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketInitServer implements NetworkPacket {

    public UUID serverUUID;
    public int port;
    public String address;
    public String gameMode;
    
    
    public PacketInitServer(UUID serverUUID, int port, String address, String gameMode) {
	super();
	this.serverUUID = serverUUID;
	this.port = port;
	this.address = address;
	this.gameMode = gameMode;
    }
    
    public PacketInitServer() {
	
    }

    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeUUID(this.serverUUID);
	w.writeInt(this.port);
	w.writeString(this.address);
	w.writeString(this.gameMode);
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	this.serverUUID = r.readUUID();
	this.port = r.readInt();
	this.address = r.readString();
	this.gameMode = r.readString();
    }

    @Override
    public void handle() {
    }

    @Override
    public int getPacketId() {
	return 0x05;
    }

}
