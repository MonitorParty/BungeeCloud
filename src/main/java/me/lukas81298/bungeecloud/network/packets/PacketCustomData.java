package me.lukas81298.bungeecloud.network.packets;

import java.io.IOException;

import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;

public class PacketCustomData implements NetworkPacket {

    public String channel;
    public byte[] data;

    public PacketCustomData() {

    }

    public PacketCustomData(String channel, byte[] data) {
	super();
	this.channel = channel;
	this.data = data;
    }

    @Override
    public void writePacketData(PacketDataWriter w) throws IOException {
	w.writeString(channel);
	w.writeInt(data.length);
	for(byte d : data) {
	    w.writeByte(d);
	}
    }

    @Override
    public void readPacketData(PacketDataReader r) throws IOException {
	channel = r.readString();
	data = new byte[r.readInt()];
	for(int i = 0; i < data.length; i++) {
	    data[i] = r.readByte();
	}
    }

    @Override
    public void handle() {
    }

    @Override
    public int getPacketId() {
	return 0x07;
    }

}
