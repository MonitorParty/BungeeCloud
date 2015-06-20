package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;
import java.util.UUID;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketSetServerOffline implements NetworkPacket {

    public UUID uuid;
    
    public PacketSetServerOffline() {

    }
   
    public PacketSetServerOffline(UUID server) {
	this.uuid = server;
    }
    
    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeUUID(uuid);
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	uuid = r.readUUID();
    }

    @Override
    public void handle() {
    }

    @Override
    public int getPacketId() {
	return 0x04;
    }

}
