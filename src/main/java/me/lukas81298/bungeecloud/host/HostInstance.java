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
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import me.lukas81298.bungeecloud.Credentials;
import me.lukas81298.bungeecloud.PacketRegistry;
import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;
import me.lukas81298.bungeecloud.network.packets.PacketAuth;
import me.lukas81298.bungeecloud.network.packets.PacketInitServer;
import me.lukas81298.bungeecloud.network.packets.PacketSetServerOffline;
import me.lukas81298.bungeecloud.network.packets.PacketStartServer;

import com.google.common.collect.Maps;

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
	} catch (IOException e) {
	    System.out.println("Unable to load the settings:");
	    e.printStackTrace();	    
	}
	try {
	    this.connect();
	}catch(IOException e) {
	    System.out.println("Could not connect:");
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
	    System.out.print("Loading setting ");
	    this.properties.load(new FileInputStream(this.configFile));
	    System.out.println(" done.");
	} else {
	    System.out.println("No configuration file was found. Creating one...");
	    this.properties.setProperty("host", "127.0.0.1");
	    this.properties.setProperty("port", "22567");
	    this.properties.setProperty("server-directory", "D:\\Projects\\BungeeCloud\\spigot-server\\");
	    this.configFile.createNewFile();
	    this.properties.store(new FileOutputStream(this.configFile), "");
	}
    }

    public void connect() throws IOException {
	System.out.println("Starting BungeeCloud Host Instance...");
	String property = this.properties.getProperty("host", "127.0.0.1");
	int port = Integer.parseInt(this.properties.getProperty("port"));
	System.out.println("Connecting to " + property + ":" + port);
	this.socket = new Socket(property, port);
	this.input = new DataInputStream(this.socket.getInputStream());
	this.output = new DataOutputStream(this.socket.getOutputStream());
	this.start();
	System.out.println("Connected to the server. Waiting for start requests.");
	this.sendPacket(new PacketAuth(this.credentials));
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
		if (packet.getPacketId() == 0x01) {
		    System.out.println("---------------------------");
		    System.out.println("Successfully authenticated.");
		    System.out.println("---------------------------");
		}
		else if (packet.getPacketId() == 0x02) {
		    PacketStartServer s = (PacketStartServer) packet;
		    System.out.println("[BungeeCloud] Starting server " + s.uuid + " with " + s.slots + " slots, " + s.memory + "Mib memory and gamemode " + s.gamemode);
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
		int port = portCounter++;
		commands.add(System.getProperty("java.home") + "\\bin\\java");
		commands.add("-Xmx" + memory + "M");
		commands.add("-Xms" + memory + "M");
		commands.add("-jar");
		commands.add(string + "\\" + gamemode + "\\spigot-1.8.7.jar");
		fillSettings(slots, commands, port);
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.environment().put("server-uuid", uuid.toString());
		pb.directory(directory);
		try {
		    Process process = pb.start();
		    sendPacket(new PacketInitServer(uuid, port, InetAddress.getLocalHost().getHostAddress(), gamemode));
		    Server server = new Server(uuid, process, memory, slots, gamemode);
		    servers.put(uuid, server);
		    process.waitFor();
		    PacketSetServerOffline packet = new PacketSetServerOffline(server.getUuid());
		    sendPacket(packet);
		    System.out.println("Server " + uuid + " went offline.");
		} catch (IOException e) {
		    System.out.println("Error while starting the server.");
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

    private void fillSettings(final int slots, List<String> commands, int port) {
	commands.add("-p");
	commands.add(Integer.toString(port));
	commands.add("-s");
	commands.add(Integer.toString(slots));
	commands.add("-o");
	commands.add("false");
	commands.add("--nojline");
    }
}
