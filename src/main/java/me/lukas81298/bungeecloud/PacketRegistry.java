package me.lukas81298.bungeecloud;

import java.util.Map;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.packets.PacketAuth;
import me.lukas81298.bungeecloud.network.packets.PacketConsoleCommand;
import me.lukas81298.bungeecloud.network.packets.PacketInitServer;
import me.lukas81298.bungeecloud.network.packets.PacketLoginSuccess;
import me.lukas81298.bungeecloud.network.packets.PacketServerStatus;
import me.lukas81298.bungeecloud.network.packets.PacketSetServerOffline;
import me.lukas81298.bungeecloud.network.packets.PacketStartServer;

import com.google.common.collect.Maps;

public class PacketRegistry {

    private Map<Integer, Class<? extends NetworkPacket>> registeredPackets = Maps.newConcurrentMap();
    
    public PacketRegistry() {
	this.registerPacket(0x00, PacketAuth.class);
	this.registerPacket(0x01, PacketLoginSuccess.class);
	this.registerPacket(0x02, PacketStartServer.class);
	this.registerPacket(0x03, PacketServerStatus.class);
	this.registerPacket(0x04, PacketSetServerOffline.class);
	this.registerPacket(0x05, PacketInitServer.class);
	this.registerPacket(0x06, PacketConsoleCommand.class);
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
