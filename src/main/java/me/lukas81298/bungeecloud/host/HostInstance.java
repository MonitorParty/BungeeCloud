package me.lukas81298.bungeecloud.host;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import me.lukas81298.bungeecloud.Credentials;
import me.lukas81298.bungeecloud.InstanceType;
import me.lukas81298.bungeecloud.PacketRegistry;
import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;
import me.lukas81298.bungeecloud.network.packets.PacketAuth;
import me.lukas81298.bungeecloud.network.packets.PacketLoginSuccess;

public class HostInstance extends Thread {

    private Properties properties = new Properties();
    private File configFile = new File("config.properties");
    private byte[] credentials;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private static HostInstance instance;
    private PacketRegistry packetRegistry = new PacketRegistry();

    public static void main(String[] args) {
	instance = new HostInstance();
    }

    public static HostInstance getInstance() {
	return instance;
    }

    public HostInstance() {
	try {
	    this.loadSettings();
	    this.connect();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public byte[] getCredentials() {
	return this.credentials;
    }

    public Properties getProperties() {
	return this.properties;
    }

    public File getConfigFile() {
	return configFile;
    }

    public Socket getSocket() {
	return socket;
    }

    public DataInputStream getInput() {
	return input;
    }

    public DataOutputStream getOutput() {
	return output;
    }

    public void loadSettings() throws FileNotFoundException, IOException {
	File file = new File("credentials.dat");
	if (!file.exists()) {
	    System.out.println("Error: No credentials found. Please place a credentials.dat file in the root directory.");
	    System.exit(0);
	    return;
	}
	this.credentials = Credentials.readFromInputStream(new FileInputStream(file), 1024);
	if (this.configFile.exists()) {
	    this.properties.load(new FileInputStream(this.configFile));
	} else {
	    this.properties.setProperty("host", "127.0.0.1");
	    this.properties.setProperty("port", "22567");
	    this.configFile.createNewFile();
	    this.properties.store(new FileOutputStream(this.configFile), "Config File. This file can be edited!");
	}
    }

    public void connect() throws IOException {
	String property = this.properties.getProperty("host", "127.0.0.1");
	int port = Integer.parseInt(this.properties.getProperty("port"));
	System.out.println("Connecting to " + property + ":" + port);
	this.socket = new Socket(property, port);
	this.input = new DataInputStream(this.socket.getInputStream());
	this.output = new DataOutputStream(this.socket.getOutputStream());
	this.start();
	System.out.println("Connected!");
	this.sendPacket(new PacketAuth(InstanceType.HOST, this.credentials));
    }

    @Override
    public void run() {
	try {
	    while (!socket.isClosed() && socket.isConnected()) {
		if(this.input.available() == 0) continue;
		int rawLength = this.input.readInt();
		int packetId = this.input.readByte();
		int packetDataLength = rawLength - 1;
		if (packetDataLength <= 0)
		    return;
		byte[] packetData = new byte[packetDataLength];
		this.input.readFully(packetData, 0, packetDataLength);
		PacketDataReader reader = new PacketDataReader(new DataInputStream(new ByteArrayInputStream(packetData)));
		NetworkPacket packet = this.packetRegistry.getPacket(packetId, reader);
		if (packet == null) {
		    System.out.println("[BungeeCloud] Invalid packet id " + packetId);
		    continue;
		}
		if (packet.getPacketId() == 0x00) {
		    throw new IllegalStateException("Wrong packet direction");
		}
		if(packet.getPacketId() == 0x01) {
		    PacketLoginSuccess p = (PacketLoginSuccess) packet;
		    System.out.println("Authentification success: " + (System.currentTimeMillis() - p.systemTime) + "ms");
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public synchronized void sendPacket(NetworkPacket packet) throws IOException {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	DataOutputStream packetOutputStream = new DataOutputStream(buffer);
	PacketDataWriter packetDataWriter = new PacketDataWriter(packetOutputStream, buffer);
	packetDataWriter.writeByte(packet.getPacketId()); // write the packet id
	packet.writePacketData(packetDataWriter); // write the packet data to
						  // the buffer
	this.output.writeInt(packetDataWriter.getPacketSize()); // write
								// packet
								// length
	this.output.write(buffer.toByteArray()); // write packet id + data
						 // to the stream
    }
}
