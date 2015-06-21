package me.lukas81298.bungeecloud.spigot;

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
import java.util.UUID;

import me.lukas81298.bungeecloud.Credentials;
import me.lukas81298.bungeecloud.InstanceType;
import me.lukas81298.bungeecloud.PacketRegistry;
import me.lukas81298.bungeecloud.host.HostInstance;
import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;
import me.lukas81298.bungeecloud.network.packets.PacketAuth;
import me.lukas81298.bungeecloud.network.packets.PacketConsoleCommand;
import me.lukas81298.bungeecloud.network.packets.PacketServerStatus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotCloud extends JavaPlugin implements me.lukas81298.bungeecloud.Instance, Runnable, Listener {

    private Properties properties = new Properties();
    private File configFile = new File("config.properties");
    private byte[] credentials;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private static HostInstance instance;
    private PacketRegistry packetRegistry = new PacketRegistry();
    private Thread networkThread;
    private UUID uuid;
    
    public Properties getProperties() {
	return properties;
    }

    public File getConfigFile() {
	return configFile;
    }

    public byte[] getCredentials() {
	return credentials;
    }

    public DataInputStream getInput() {
	return input;
    }

    public DataOutputStream getOutput() {
	return output;
    }

    public static HostInstance getInstance() {
	return instance;
    }

    public PacketRegistry getPacketRegistry() {
	return packetRegistry;
    }

    public Socket getSocket() {
	return this.socket;
    }

    @Override
    public void onDisable() {
	super.onDisable();
    }

    @Override
    public void onEnable() {
	super.onEnable();
	String getenv = System.getenv("server-uuid");
	if(getenv == null) {
	    System.out.println("Please start the server using BungeeCloud.");
	    Bukkit.shutdown();
	    return;
	}
	this.uuid = UUID.fromString(getenv);
	try {
	    this.loadSettings();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	try {
	    this.connect();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	Bukkit.getPluginManager().registerEvents(this, this);
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
	this.networkThread = new Thread(this);
	this.networkThread.start();
	System.out.println("Connected!");
	this.sendPacket(new PacketAuth(InstanceType.SPIGOT, this.credentials));
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

    @Override
    public InstanceType getType() {
	return InstanceType.SPIGOT;
    }

    @Override
    public void shutdown() {
	Bukkit.shutdown();
    }

    @Override
    public boolean isServer() {
	return false;
    }

    @Override
    public boolean isClient() {
	return true;
    }

    @Override
    public boolean isConnected() {
	return this.socket.isConnected();
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
		switch(packetId) {
		case 0x01:
		    this.sendPacket(new PacketServerStatus(uuid,1,0));
		    break;
		case 0x06:
		    PacketConsoleCommand p = (PacketConsoleCommand) packet;
		    System.out.println("Dispatching command: " + p.command);
		    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), p.command);
		    break;
		default:
		    System.out.println("Illegal Packet ID: " + packetId);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
	updateServerData(Bukkit.getOnlinePlayers().size());
	Player p = e.getPlayer();
	if(System.getenv("server-uuid") != null) {
	    p.sendMessage("§aYou are currently on server §f" + System.getenv("server-uuid"));
	}
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
	if(Bukkit.getOnlinePlayers().size() == 1) {
	    Bukkit.shutdown();
	}else {
	    updateServerData(Bukkit.getOnlinePlayers().size() - 1);
	}
    }
    
    @EventHandler
    public void onKick(PlayerKickEvent e) {
	if(Bukkit.getOnlinePlayers().size() == 1) {
	    Bukkit.shutdown();
	}else {
	    updateServerData(Bukkit.getOnlinePlayers().size() - 1);
	}
    }
    
    public void updateServerData(int playerCount) {
	PacketServerStatus packet = new PacketServerStatus(uuid, 1, playerCount);
	try {
	    sendPacket(packet);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
