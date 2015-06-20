package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;
import java.util.UUID;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketServerStatus implements NetworkPacket {
    
    public UUID uuid;
    
    public int state;
    
    public int playerCount;

    public PacketServerStatus() {
	
    }
    
    public PacketServerStatus(UUID uuid, int state, int playerCount) {
	super();
	this.uuid = uuid;
	this.state = state;
	this.playerCount = playerCount;
    }


    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeUUID(uuid);
	w.writeInt(state);
	w.writeInt(playerCount);
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	uuid = r.readUUID();
	state = r.readInt();
	playerCount = r.readInt();
    }

    @Override
    public void handle() {
    }

    @Override
    public int getPacketId() {
	return 0x03;
    }
    
   
}
