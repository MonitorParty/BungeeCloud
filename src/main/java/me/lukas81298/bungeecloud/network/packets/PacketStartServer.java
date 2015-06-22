package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;


public class PacketStartServer implements NetworkPacket {

    public String gamemode;
    public int slots;
    public int memory;
    public UUID uuid;
    public Map<String, String> properties = new HashMap<>();
    public String world = "world";
    
    public PacketStartServer() {
	
    }

    public PacketStartServer(String gamemode, int slots, int memory, UUID uuid) {
	super();
	this.gamemode = gamemode;
	this.slots = slots;
	this.memory = memory;
	this.uuid = uuid;
    }

    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeString(gamemode);
	w.writeInt(slots);
	w.writeInt(memory);
	w.writeUUID(uuid);
	w.writeInt(properties.size());
	for(Entry<String, String> entry : properties.entrySet()) {
	    w.writeString(entry.getKey());
	    w.writeString(entry.getValue());
	}
	w.writeString(world);
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	gamemode = r.readString();
	slots = r.readInt();
	memory = r.readInt();
	uuid = r.readUUID();
	int size = r.readInt();
	properties.clear();
	for(int i = 0; i < size; i++) {
	    properties.put(r.readString(), r.readString());
	}
	world = r.readString();
    }

    @Override
    public void handle() {
    }

    @Override
    public int getPacketId() {
	return 0x02;
    }
    
    
}
