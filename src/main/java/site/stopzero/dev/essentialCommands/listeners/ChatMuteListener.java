package site.stopzero.dev.essentialCommands.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import site.stopzero.dev.essentialCommands.EssentialCommands;
import site.stopzero.dev.essentialCommands.managers.MuteManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMuteListener implements Listener {

    private final EssentialCommands plugin;

    public ChatMuteListener(EssentialCommands plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            event.setCancelled(true);

            MuteManager.Muteinfo info = plugin.getMuteManager().getMuteInfo(player.getUniqueId());
            if (info == null) return;

            String reason = info.getReason();
            long expirationTime = info.getExpirationTime();

            player.sendMessage(ChatColor.RED + "당신은 현재 채팅이 차단되어있습니다!");
            player.sendMessage(ChatColor.RED + "사유: " + reason);

            if (expirationTime == -1) {
                player.sendMessage(ChatColor.RED + "이 차단은 영구적입니다.");

            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                player.sendMessage(ChatColor.RED + "만료 일시: " + sdf.format(new Date(expirationTime)));
            }
        }
    }
}
