package de.dytanic.cloudnet.ext.bridge.bungee.command;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.BridgeConfigurationProvider;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.bukkit.command.CommandExecutor;

public class CommandBroadcast extends Command {

    public CommandBroadcast() {
        super("broadcast", "cloud.command.broadcast", "bc");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', BridgeConfigurationProvider.load().getPrefix()) + "/broadcast <message>"));
            return;
        }

        String commandLine = String.join(" ", args);

        CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getOnlinePlayers().forEach(iCloudPlayer -> {
            iCloudPlayer.getPlayerExecutor().sendChatMessage(ChatColor.translateAlternateColorCodes('&', BridgeConfigurationProvider.load().getBroadcastPrefix()) + commandLine.replaceAll("&", "§"));
        });
    }
}
