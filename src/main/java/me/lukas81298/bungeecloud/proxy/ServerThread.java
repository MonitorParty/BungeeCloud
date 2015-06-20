package me.lukas81298.bungeecloud.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Vector;

import me.lukas81298.bungeecloud.PacketRegistry;

public class ServerThread extends Thread {

    private ServerSocket server;
    private BungeeCloud instance;
    private Vector<Client> clients = new Vector<>();

    private PacketRegistry packetRegistry;
    
    public ServerThread(ServerSocket socket, BungeeCloud instance) {
	this.server = socket;
	this.instance = instance;
	this.packetRegistry = new PacketRegistry();
    }
    
    public PacketRegistry getPacketRegistry() {
	return this.packetRegistry;
    }

    public BungeeCloud getBungeeCloud() {
	return this.instance;
    }

    public ServerSocket getSocket() {
	return this.server;
    }

    @Override
    public void run() {
	try {
	    while (!server.isClosed() && !isInterrupted()) {
		Socket socket = server.accept();
		if(instance.getIPWhitelist().size() == 0 || instance.getIPWhitelist().contains(socket.getInetAddress().getHostAddress())) {
		    System.out.println(socket.getRemoteSocketAddress() + " has connected. Waiting for authentication!");
		}else {
		    socket.close();
		    System.out.println("Disconnected " + socket.getRemoteSocketAddress() + ", because it is not whitelisted.");
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    try {
		if (!this.server.isClosed()) {
		    this.server.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    System.out.println("BungeeCloud has crashed. Please report the problem to the developer!!!");
	}
    }
    
    public Collection<Client> getClients() {
	return this.clients;
    }
    
}
