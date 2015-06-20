package me.lukas81298.bungeecloud;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class Credentials {

    static Random random = new Random();
    
    public static byte[] generateCredentials(int length) {
	byte[] bytes = new byte[length];
	random.nextBytes(bytes);
	return bytes;
    }
    
    public static byte[] readFromInputStream(InputStream in, int length) throws IOException {
	byte[] bytes = new byte[length];
	DataInput data = new DataInputStream(in);
	for(int i = 0; i < bytes.length; i++) {
	    bytes[i] = data.readByte();
	}
	in.close();
	return bytes;
    }
    
    public static void writeToOutputStream(OutputStream out, byte[] bytes) throws IOException {
	DataOutput data = new DataOutputStream(out);
	data.write(bytes);
	out.flush();
	out.close();
    }
     
}
 