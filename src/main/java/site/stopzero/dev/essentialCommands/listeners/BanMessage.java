package site.stopzero.dev.essentialCommands.listeners;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import site.stopzero.dev.essentialCommands.EssentialCommands;

import java.text.SimpleDateFormat;

public class BanMessage implements Listener {

    private final EssentialCommands plugin;

    public BanMessage(EssentialCommands plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {

            BanList<?> banList = Bukkit.getBanList(BanList.Type.PROFILE);
            BanEntry<?> banEntry =
                    banList.getBanEntry(event.getPlayer().getUniqueId().toString());

            if (banEntry == null) {
                return;
            }

            String reason = banEntry.getReason();
            if (reason == null) {
                reason = "사유 없음";
            }

            String expiration;
            if (banEntry.getExpiration() == null) {
                expiration = "영구";
            } else {
                SimpleDateFormat sdf
                        = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
                expiration = sdf.format(banEntry.getExpiration());
            }

            String customKickMessage =
                    ChatColor.RED + "\n당신은 이 서버에서 차단되었습니다."
                            + "\n\n" + ChatColor.WHITE + "사유: " + ChatColor.YELLOW + reason
                            + "\n" + ChatColor.WHITE + "차단 만료일: " + ChatColor.YELLOW + expiration;

            String logMessage = String.format("%s님의 접속시도가 차단되었습니다. (사유: %s, 만료일: %s)"
                    ,event.getPlayer().getName(), reason, expiration);

            event.setKickMessage(customKickMessage);
            plugin.getLogger().info(logMessage);
        }
    }
}
