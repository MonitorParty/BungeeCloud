package me.lukas81298.bungeecloud.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class PacketDataReader {
    
    private DataInputStream dataIn;

    public PacketDataReader(DataInputStream dataIn) {
	super();
	this.dataIn = dataIn;
    }

    public DataInputStream getStream() {
	return dataIn;
    }
    
    public byte[] readByteArray() throws IOException {
	int i = readInt();
	byte[] array = new byte[i];
	for(int c = 0; c < array.length; c++) {
	    array[c] = this.readByte();
	}
	return array;
    }

    public short readShort() throws IOException {
	return dataIn.readShort();
    }

    public byte readByte() throws IOException {
	return dataIn.readByte();
    }
    public String readString() throws IOException {
	int length = this.readInt();
	String s = this.dataIn.readUTF();
	if (s.length() != length) {
	    throw new IllegalStateException("Invalid string length");
	}
	return s;
    }

    public UUID readUUID() throws IOException {
	return new UUID(this.readLong(), this.readLong());
    }

    public long readLong() throws IOException {
	return this.dataIn.readLong();
    }

    public boolean readBoolean() throws IOException {
	return this.dataIn.readBoolean();
    }

    public int readUnsignedByte() throws IOException {
	return this.dataIn.readUnsignedByte();
    }

    public double readDouble() throws IOException {
	return this.dataIn.readDouble();
    }

    public int readInt() throws IOException {
	return this.dataIn.readInt();
    }

    public float readFloat() throws IOException {
	return this.dataIn.readFloat();
    }
}
