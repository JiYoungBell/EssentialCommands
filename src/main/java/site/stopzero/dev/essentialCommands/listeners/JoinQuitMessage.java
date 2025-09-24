package site.stopzero.dev.essentialCommands.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import site.stopzero.dev.essentialCommands.EssentialCommands;

public class JoinQuitMessage implements Listener {

    private final EssentialCommands plugin;

    public JoinQuitMessage(EssentialCommands plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String formattedMessage;

        if (!player.hasPlayedBefore()) {
            String rawMessage = plugin.getConfig().getString("join-quit.first-join-message");
            formattedMessage = formatAndColorize(rawMessage, player);

            logToConsole(player, "join-quit.first-join-log-message");

            if (plugin.getConfig().getBoolean("join-quit.send-private-message-on-first-join", false)) {
                String privateWelcomeRaw = plugin.getConfig().getString("join-quit.private-message-on-first-join");
                String formattedPrivateMessage = formatAndColorize(privateWelcomeRaw, player);

                if (formattedPrivateMessage != null) player.sendMessage(formattedPrivateMessage);
            }

        } else {

            String rawMessage = plugin.getConfig().getString("join-quit.join-message");
            formattedMessage = formatAndColorize(rawMessage, player);

            logToConsole(player, "join-quit.join-log-message");
        }

        if (formattedMessage != null) event.setJoinMessage(formattedMessage);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String rawMessage = plugin.getConfig().getString("join-quit.quit-message");
        String formattedMessage = formatAndColorize(rawMessage, event.getPlayer());

        if (formattedMessage != null) event.setQuitMessage(formattedMessage);

        logToConsole(event.getPlayer(), "join-quit.quit-log-message");
    }


    private String formatAndColorize(String message, Player player) {
        if (message == null || message.isEmpty()) return null;

        message = message.replace("%player%", player.getName());
        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    private void logToConsole(Player player, String configKey) {
        String logMessage = plugin.getConfig().getString(configKey);

        if (logMessage != null && !logMessage.isEmpty()) {
            logMessage = logMessage.replace("%player%", player.getName());

            plugin.getLogger().info(logMessage);
        }
    }
}
