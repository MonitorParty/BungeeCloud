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
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.google.common.collect.Maps;

import me.lukas81298.bungeecloud.Credentials;
import me.lukas81298.bungeecloud.InstanceType;
import me.lukas81298.bungeecloud.PacketRegistry;
import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;
import me.lukas81298.bungeecloud.network.packets.PacketAuth;
import me.lukas81298.bungeecloud.network.packets.PacketLoginSuccess;
import me.lukas81298.bungeecloud.network.packets.PacketServerStatus;
import me.lukas81298.bungeecloud.network.packets.PacketSetServerOffline;
import me.lukas81298.bungeecloud.network.packets.PacketStartServer;

public class HostInstance extends Thread {

    private Properties properties = new Properties();
    private File configFile = new File("config.properties");
    private byte[] credentials;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private static HostInstance instance;
    private PacketRegistry packetRegistry = new PacketRegistry();
    private int portCounter = 25590;
    private Map<UUID, Server> servers = Maps.newConcurrentMap();

    public Map<UUID, Server> getServers() {
	return this.servers;
    }

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
	    this.properties.setProperty("server-directory", "D:\\Projects\\BungeeCloud\\spigot-server\\");
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
		if (this.input.available() == 0)
		    continue;
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
		if (packet.getPacketId() == 0x01) {
		    PacketLoginSuccess p = (PacketLoginSuccess) packet;
		    System.out.println("Authentification success: " + (System.currentTimeMillis() - p.systemTime) + "ms");
		}
		if (packet.getPacketId() == 0x02) {
		    PacketStartServer s = (PacketStartServer) packet;
		    System.out.println("Starting server " + s.uuid + " with " + s.slots + " slots, " + s.memory + "Mib memory and gamemode " + s.gamemode);
		    this.startServer(s.uuid, s.gamemode, s.slots, s.memory);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void startServer(final UUID uuid, final String gamemode, final int slots, final int memory) {
	Thread t = new Thread(new Runnable() {

	    @Override
	    public void run() {
		String string = properties.getProperty("server-directory").replace("/", "\\");
		File directory = new File(string + "\\" +  gamemode);
		List<String> commands = new LinkedList<>();
		commands.add(System.getProperty("java.home"));
		commands.add("\\bin\\java");
		commands.add("-Xmx" + memory + "M");
		commands.add("-Xms" + memory + "M");
		commands.add("-jar");
		commands.add(string + "\\" + gamemode + "\\spigot-1.8.7.jar");
		fillSettings(slots, commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.environment().put("server-uuid", uuid.toString());
		pb.directory(directory);
		try {
		    Process process = pb.start();
		    sendPacket(new PacketServerStatus(uuid, 0, 0));
		    Server server = new Server(uuid, process, memory, slots, gamemode);
		    servers.put(uuid, server);
		    process.waitFor();
		    PacketSetServerOffline packet = new PacketSetServerOffline(server.getUuid());
		    sendPacket(packet);
		    System.out.println("Server " + uuid + " went offline.");
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	});
	t.start();
    }

    public void sendConsoleCommand(UUID uuid, String command) {
	Server server = servers.get(uuid);
	if (server == null) {
	    return;
	}
	System.out.println("Executing command '" + command + "' on server " + uuid);
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

    private void fillSettings(final int slots, List<String> commands) {
	commands.add("-p");
	commands.add(Integer.toString(portCounter++));
	commands.add("-s");
	commands.add(Integer.toString(slots));
	commands.add("-o");
	commands.add("false");
	commands.add("--nojline");
    }
}
