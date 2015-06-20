package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;

import me.lukas81298.bungeecloud.InstanceType;
import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketAuth implements NetworkPacket {

    private InstanceType type;
    private byte[] credentials;
    
    public PacketAuth(InstanceType type, byte[] credentials) {
	this.type = type;
	this.credentials = credentials;
    }
    
    public PacketAuth() {
	
    }
    
    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeInt(type.ordinal());
	w.writeInt(credentials.length);
	for(byte b : credentials) {
	    w.writeByte(b);
	}
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	this.type = InstanceType.values()[r.readInt() - 1];
	this.credentials = r.readByteArray();
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

    
}
