package me.lukas81298.bungeecloud.proxy;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class StartServerCommand extends Command {

    public StartServerCommand() {
	super("start-server");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
	final ProxiedPlayer p = (ProxiedPlayer) cs;
	cs.sendMessage(new TextComponent("§aStarting a server for you..."));
	try {
	    BungeeCloud b = BungeeCloud.instance;
	    final UUID uuid = b.startNewServer(new ServerProperties(512, 5, "testgamemode"));
	    cs.sendMessage(new TextComponent("§aStarting server §f" + uuid));
	    b.waitingForServer.put(p, uuid);
	} catch (Exception e) {
	    e.printStackTrace();
	    cs.sendMessage(new TextComponent("§cCould not start server: §f" + e + ": §e" + e.getMessage()));
	}
    }

}
