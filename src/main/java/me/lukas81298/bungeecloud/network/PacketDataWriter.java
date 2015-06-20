package me.lukas81298.bungeecloud.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PacketDataWriter {
    private DataOutputStream out;
    private ByteArrayOutputStream byteArrayOutputStream;

    public PacketDataWriter(DataOutputStream out, ByteArrayOutputStream b) {
	this.out = out;
	this.byteArrayOutputStream = b;
    }

    public DataOutputStream getStream() {
	return out;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
	return byteArrayOutputStream;
    }

    public void writeByte(int b) throws IOException {
	this.out.writeByte(b);
    }
    
    public void writeInt(int i) throws IOException {
	this.out.writeInt(i);
    }

    public void writeLong(long l) throws IOException {
	this.out.writeLong(l);
    }
    
    public void writeUUID(UUID uuid) throws IOException {
	this.writeLong(uuid.getMostSignificantBits());
	this.writeLong(uuid.getLeastSignificantBits());
    }
    
    public void writeString(String string) throws IOException {
	this.out.writeInt(string.length());
	this.out.writeUTF(string);
    }

    public void writeShort(short s) throws IOException {
	this.out.writeShort((int) s);
    }

    public DataOutputStream getOutputStream() {
	return this.out;
    }

    public int getPacketSize() {
	return this.byteArrayOutputStream.size();
    }
}
