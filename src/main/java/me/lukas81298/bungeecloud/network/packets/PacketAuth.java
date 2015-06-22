package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;
import java.util.UUID;

import me.lukas81298.bungeecloud.InstanceType;
import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketAuth implements NetworkPacket {

    private InstanceType type;
    private byte[] credentials;
    private UUID uuid;

    public PacketAuth(byte[] credentials) {
	this.type = InstanceType.HOST;
	this.credentials = credentials;
    }

    public PacketAuth(byte[] credentials, UUID uuid) {
	this.type = InstanceType.SPIGOT;
	this.credentials = credentials;
	this.uuid = uuid;
    }

    public PacketAuth() {

    }

    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeInt(type.ordinal());
	w.writeInt(credentials.length);
	for (byte b : credentials) {
	    w.writeByte(b);
	}
	if(type == InstanceType.SPIGOT) {
	    w.writeUUID(uuid);
	}
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	this.type = InstanceType.values()[r.readInt()];
	this.credentials = r.readByteArray();
	if(type == InstanceType.SPIGOT) {
	    this.uuid = r.readUUID();
	}
    }

    @Override
    public void handle() {

    }

    @Override
    public int getPacketId() {
	return 0x00;
    }

    public InstanceType getType() {
	return type;
    }

    public void setType(InstanceType type) {
	this.type = type;
    }

    public byte[] getCredentials() {
	return credentials;
    }

    public void setCredentials(byte[] credentials) {
	this.credentials = credentials;
    }

    public UUID getUuid() {
	return uuid;
    }

    public void setUuid(UUID uuid) {
	this.uuid = uuid;
    }

}
