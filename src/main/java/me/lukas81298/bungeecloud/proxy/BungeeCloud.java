package me.lukas81298.bungeecloud.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import me.lukas81298.bungeecloud.Credentials;
import me.lukas81298.bungeecloud.Instance;
import me.lukas81298.bungeecloud.InstanceType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCloud extends Plugin implements Instance {

    private byte[] credentials;
    private Properties properties;
    private File configFile = new File("cloud.properties");
    private File ipWhitelistFile = new File("ip-whitelist.txt");
    private List<String> ipwhitelist = new ArrayList<>();
    private ServerSocket serverSocket;
    private ServerThread serverThread;
    
    @Override
    public void onDisable() {
	super.onDisable();
    }

    @Override
    public void onEnable() {
	super.onEnable();
	log("Loading credentials...");
	try {
	    loadCredentials();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	this.properties = new Properties();
	this.loadSettings();
	this.loadWhitelist();
	int port = Integer.parseInt(this.getProperties().getProperty("server-port", "22567"));
	try {
	    startServer(port);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void startServer(int port) throws IOException {
	this.serverSocket = new ServerSocket(port);
	String bindAddress = this.getProperties().getProperty("bind-address");
	if (!bindAddress.equals("0.0.0.0")) {
	    log("Bound to " + bindAddress);
	    this.serverSocket.bind(new InetSocketAddress(bindAddress, port));
	}
	this.serverThread = new ServerThread(this.serverSocket, this);
	this.serverThread.start();
    }

    private void loadWhitelist() {
	try {
	    log("Loading ip whitelist...");
	    if (this.ipWhitelistFile.exists()) {
		Scanner scanner = new Scanner(this.ipWhitelistFile);
		while (scanner.hasNextLine()) {
		    String ip = scanner.nextLine();
		    this.ipwhitelist.add(ip);
		    System.out.println("Added " + ip + " to the whitelist.");
		}
		scanner.close();
	    } else {
		this.ipWhitelistFile.createNewFile();
	    }
	    if (this.ipwhitelist.size() == 0) {
		log("--------------------------------------------------");
		log("IP whitelist is empty. WARNING: EVERY IP ADDRESS WILL BE ACCEPTED!");
		log("--------------------------------------------------");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void loadSettings() {
	log("Loading settings");
	try {
	    if (!this.configFile.exists()) {
		this.configFile.createNewFile();
		log("Creating default config.");
		this.properties.setProperty("server-port", "22567");
		this.properties.setProperty("bind-address", "0.0.0.0");
		this.properties.store(new FileOutputStream(this.configFile), "BungeeCloud Config File");
	    } else {
		this.properties.load(new FileInputStream(this.configFile));
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void loadCredentials() throws IOException {
	File file = new File("credentials.dat");
	if (file.exists()) {
	    this.credentials = Credentials.readFromInputStream(new FileInputStream(file), 1024);
	} else {
	    file.createNewFile();
	    log("Creating new credentials with 1204 bytes length");
	    this.credentials = Credentials.generateCredentials(1024);
	    Credentials.writeToOutputStream(new FileOutputStream(file), this.credentials);
	}
    }

    public byte[] getCredentials() {
	return this.credentials;
    }

    public Properties getProperties() {
	return this.properties;
    }

    public Collection<String> getIPWhitelist() {
	return this.ipwhitelist;
    }

    @Override
    public InstanceType getType() {
	return InstanceType.PROXY;
    }

    @Override
    public void shutdown() {
	BungeeCord.getInstance().stop();
    }

    public void log(String s) {
	System.out.println("[BungeeCloud] " + s);
    }

    @Override
    public boolean isServer() {
	return true;
    }

    @Override
    public boolean isClient() {
	return false;
    }

    @Override
    public boolean isConnected() {
	return true;
    }

    public ServerSocket getServerSocket() {
	return this.serverSocket;
    }
    
    public ServerThread getServerThread() {
	return this.serverThread;
    }
    
}
