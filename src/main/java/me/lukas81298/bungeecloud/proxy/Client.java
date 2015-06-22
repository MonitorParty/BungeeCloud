package me.lukas81298.bungeecloud.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.UUID;

import me.lukas81298.bungeecloud.InstanceType;
import me.lukas81298.bungeecloud.network.NetworkPacket;
import me.lukas81298.bungeecloud.network.PacketDataReader;
import me.lukas81298.bungeecloud.network.PacketDataWriter;
import me.lukas81298.bungeecloud.network.packets.PacketAuth;
import me.lukas81298.bungeecloud.network.packets.PacketInitServer;
import me.lukas81298.bungeecloud.network.packets.PacketLoginSuccess;
import me.lukas81298.bungeecloud.network.packets.PacketServerStatus;
import me.lukas81298.bungeecloud.network.packets.PacketSetServerOffline;
import me.lukas81298.bungeecloud.proxy.api.ServerDataUpdateEvent;
import me.lukas81298.bungeecloud.proxy.api.ServerInitEvent;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Client extends Thread {

    private boolean authed = false;
    private Socket socket;
    private ServerThread server;
    private InstanceType instanceType;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Client(Socket socket, ServerThread server) throws IOException {
	this.server = server;
	this.socket = socket;
	this.inputStream = new DataInputStream(this.socket.getInputStream());
	this.outputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    public InstanceType getInstanceType() {
	return instanceType;
    }

    public DataInputStream getInputStream() {
	return inputStream;
    }

    public DataOutputStream getOutputStream() {
	return outputStream;
    }

    public boolean isAuthed() {
	return authed;
    }

    public Socket getSocket() {
	return socket;
    }

    public ServerThread getServer() {
	return server;
    }

    @Override
    public void run() {
	try {
	    while (!socket.isClosed() && socket.isConnected()) {
		if (this.inputStream.available() == 0)
		    continue;
		int rawLength = this.inputStream.readInt();
		int packetId = this.inputStream.readByte();
		int packetDataLength = rawLength - 1;
		if (packetDataLength <= 0)
		    return;
		byte[] packetData = new byte[packetDataLength];
		this.inputStream.readFully(packetData, 0, packetDataLength);
		PacketDataReader reader = new PacketDataReader(new DataInputStream(new ByteArrayInputStream(packetData)));
		NetworkPacket packet = this.getServer().getPacketRegistry().getPacket(packetId, reader);
		if (packet == null) {
		    System.out.println("[BungeeCloud] Invalid packet id " + packetId);
		    continue;
		}
		System.out.println(packet.getClass().getName());
		if (packet.getPacketId() == 0x00) {
		    this.handleAuthRequest(packet);
		} else if (packet.getPacketId() == 0x03) {
		    handleStatusUpdate(packet);
		} else if (packet.getPacketId() == 0x04) {
		    PacketSetServerOffline s = (PacketSetServerOffline) packet;
		    System.out.println("Server " + s.uuid + " went offline.");
		} else if (packet.getPacketId() == 0x05) {
		    handleServerInit(packet);

		}
	    }
	} catch (Exception ex) {
	    server.getClients().remove(this);
	    ex.printStackTrace();
	    try {
		this.socket.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public void handleServerInit(NetworkPacket packet) {
	PacketInitServer s = (PacketInitServer) packet;
	ServerInfo info = BungeeCord.getInstance().constructServerInfo(s.serverUUID.toString(), new InetSocketAddress(s.address, s.port), s.gameMode, false);
	getServer().getBungeeCloud().gameServers.put(s.serverUUID, new GameServer(0, 0, s.serverUUID, s.gameMode, info));
	BungeeCord.getInstance().getServers().put(s.serverUUID.toString(), info);
	BungeeCord.getInstance().getPluginManager().callEvent(new ServerInitEvent(s.serverUUID, info, s.gameMode));
    }

    public void handleStatusUpdate(NetworkPacket packet) {
	PacketServerStatus s = (PacketServerStatus) packet;
	System.out.println("Update Server Status " + s.uuid + ": " + s.playerCount + " " + s.state);
	for (Entry<ProxiedPlayer, UUID> entry : BungeeCloud.instance.waitingForServer.entrySet()) {
	    if (entry.getValue().equals(s.uuid)) {
		entry.getKey().connect(BungeeCord.getInstance().getServerInfo(s.uuid.toString()));
	    }
	}
	BungeeCord.getInstance().getPluginManager().callEvent(new ServerDataUpdateEvent(s.uuid, s.data, s.playerCount, s.state));
    }

    public void handleAuthRequest(NetworkPacket packet) throws IOException {
	PacketAuth auth = (PacketAuth) packet;
	if (checkCredentials(auth.getCredentials())) {
	    authed = true;
	    this.instanceType = auth.getType();
	    if (BungeeCloud.instance.gameServers.containsKey(auth.getUuid())) {
		BungeeCloud.instance.gameServers.get(auth.getUuid()).setClient(this);
	    }
	    this.sendPacket(new PacketLoginSuccess(System.currentTimeMillis()));
	} else {
	    socket.close();
	    return;
	}
    }

    public boolean checkCredentials(byte[] b) {
	byte[] c = this.server.getBungeeCloud().getCredentials();
	if (b.length != c.length) {
	    return false;
	}
	for (int i = 0; i < c.length; i++) {
	    if (b[i] != c[i]) {
		return false;
	    }
	}
	return true;
    }

    public synchronized void sendPacket(NetworkPacket packet) throws IOException {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	DataOutputStream packetOutputStream = new DataOutputStream(buffer);
	PacketDataWriter packetDataWriter = new PacketDataWriter(packetOutputStream, buffer);
	packetDataWriter.writeByte(packet.getPacketId()); // write the packet id
	packet.writePacketData(packetDataWriter); // write the packet data to
						  // the buffer
	this.outputStream.writeInt(packetDataWriter.getPacketSize()); // write
								      // packet
								      // length
	this.outputStream.write(buffer.toByteArray()); // write packet id + data
						       // to the stream
    }
}
