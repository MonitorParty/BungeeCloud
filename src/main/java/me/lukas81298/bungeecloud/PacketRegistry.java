package me.lukas81298.bungeecloud;

import java.util.Map;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.packets.PacketAuth;
import me.lukas81298.bungeecloud.network.packets.PacketLoginSuccess;

import com.google.common.collect.Maps;

public class PacketRegistry {

    private Map<Integer, Class<? extends NetworkPacket>> registeredPackets = Maps.newConcurrentMap();
    
    public PacketRegistry() {
	this.registerPacket(0x00, PacketAuth.class);
	this.registerPacket(0x01, PacketLoginSuccess.class);
    }
    
    public void registerPacket(int packetId, Class<? extends NetworkPacket> clazz) {
	this.registeredPackets.put(packetId, clazz);
    }
    
    public NetworkPacket getPacket(int packetId, PacketDataReader reader) throws Exception {
	if(this.registeredPackets.containsKey(packetId)) {
	    NetworkPacket packet = this.registeredPackets.get(packetId).newInstance();
	    packet.readPacketData(reader);
	    return packet;
	}
	return null;
    }
}
