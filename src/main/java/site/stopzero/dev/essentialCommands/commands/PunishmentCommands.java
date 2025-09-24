package site.stopzero.dev.essentialCommands.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import site.stopzero.dev.essentialCommands.EssentialCommands;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PunishmentCommands implements CommandExecutor {

    private final EssentialCommands plugin;

    public PunishmentCommands(EssentialCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {

        if (command.getName().equalsIgnoreCase("추방")) {
            return handleKickCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("밴")) {
            return handleBanCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("기간밴")) {
            return handleTempBanCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("밴해제")) {
            return handleUnbanCommand(sender, args);
        }

        return false;
    }

    /**
     *
     * @return target 명령어 대상 플레이어
     * @param sender 명령어 실행 주체
     * @param reason 사유
     * @return 포맷팅된 메세지
     *
     */
    private String getFormattedMessage(String path, CommandSender sender, OfflinePlayer target, String reason) {
        String message = plugin.getConfig().getString(path);

        if (message == null || message.isEmpty()) {
            return null;
        }

        if (target != null) message = message.replace("%player%", target.getName());
        if (reason != null) message = message.replace("%reason%", reason);
        message = message.replace("%sender%", sender.getName());

        return ChatColor.translateAlternateColorCodes('&', message);
    }


    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return false;
        }
        return true;
    }

    private String buildReason(String[] args, int startIndex, String defaultReason) {
        if (args.length > startIndex) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = startIndex; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            return reasonBuilder.toString().trim();
        }
        return defaultReason;
    }

    private Player findOnlinePlayer(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "플레이어 " + playerName + "님을 찾을 수 없습니다. (온라인 플레이어만 가능)");
        }

        return target;
    }

    private boolean isAlreadyBanned(CommandSender sender, OfflinePlayer target) {
        if (target.isBanned()) {
            sender.sendMessage(ChatColor.RED + target.getName() + " 님은 이미 밴 상태입니다.");
            return true;
        }
        return false;
    }

    private boolean isNotBanned(CommandSender sender, OfflinePlayer target) {
        if (!target.isBanned()) {
            sender.sendMessage(ChatColor.RED + target.getName() + " 님은 밴 상태가 아닙니다.");
            return true;
        }
        return false;
    }

    private long parseDuration(CommandSender sender, String durationString) {
        try {
            String numberString;
            long value;

            if (durationString.endsWith("일")) {
                numberString = durationString.replace("일", "");
                value = Long.parseLong(numberString);
                return TimeUnit.DAYS.toMillis(value);

            } else if (durationString.endsWith("시간")) {
                numberString = durationString.replace("시간", "");
                value = Long.parseLong(numberString);
                return TimeUnit.HOURS.toMillis(value);

            } else if (durationString.endsWith("분")) {
                numberString = durationString.replace("분", "");
                value = Long.parseLong(numberString);
                return TimeUnit.MINUTES.toMillis(value);

            } else if (durationString.endsWith("초")) {
                numberString = durationString.replace("초", "");
                value = Long.parseLong(numberString);
                return TimeUnit.SECONDS.toMillis(value);

            } else {
                sender.sendMessage(ChatColor.RED + "알 수 없는 시간 단위입니다. (일, 시간, 분, 초)");
                return -1;
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "알 수 없는 시간 단위입니다. (일, 시간, 분, 초)");
            return -1;
        }
    }

    private boolean handleKickCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ec.kick")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "사용법: /추방 <플레이어> [사유]");
            return true;
        }

        Player target = findOnlinePlayer(sender, args[0]);
        if (target == null) {
            return true;
        }

        String reason = buildReason(args, 1, "관리자에 의해 추방되었습니다.");
        String kickMessage = ChatColor.RED + "서버에서 추방 되었습니다!\n사유:" + reason + ")";

        target.kickPlayer(kickMessage);

        String broadcastMessage = getFormattedMessage("punishments.kick-broadcast-message", sender, target, reason);
        if (broadcastMessage != null) Bukkit.broadcastMessage(broadcastMessage);

        return true;
    }

    private boolean handleBanCommand(CommandSender sender, String [] args) {
        if (!checkPermission(sender, "ec.ban")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "사용법 : /밴 <플레이어> [사유]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "플레이어 " + args[0] + "님은 서버에 접속한 기록이 없습니다.");
            return true;
        }

        if (isAlreadyBanned(sender, target)) {
            return true;
        }

        String reason = buildReason(args, 1, "서버 관리자에 의해 차단 되었습니다.");

        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());

        if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                onlineTarget.kickPlayer(ChatColor.RED
                        + "관리자에 의해 서버에서 차단되었습니다.\n사유: " + reason);
            }
        }

        String broadcastMessage = getFormattedMessage("punishments.ban-broadcast-message", sender, target, reason);
        if (broadcastMessage != null) Bukkit.broadcastMessage(broadcastMessage);

        return true;
    }

    private boolean handleTempBanCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ec.tempban")) {
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "사용법 : /기간밴 <플레이어> <시간> [사유]");
            sender.sendMessage(ChatColor.GRAY + "시간 단위 : 일, 시간, 분, 초. 예시: 7일, 12시간, 30분");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "플레이어 " + args[0] + "님은 서버에 접속한 기록이 없습니다.");
            return true;
        }

        if (isAlreadyBanned(sender, target)) {
            return true;
        }

        long durationMillis = parseDuration(sender, args[1]);
        if (durationMillis <= 0) {
            if (durationMillis == 0) {
                sender.sendMessage("시간 값은 0보다 커야 합니다!");
            }
            return true;
        }

        Date expiration = new Date(System.currentTimeMillis() + durationMillis);
        String reason = buildReason(args, 2, "서버 관리자에 의해 임시 차단되었습니다.");

        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, expiration, sender.getName());

        if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                onlineTarget.kickPlayer(ChatColor.RED + "관리자에 의해 서버에서 임시 차단되었습니다."
                        + "\n\n사유: " + reason
                        + "\n" + "만료: " + expiration);
            }
        }

        String broadcastMessage = getFormattedMessage("punishments.tempban-broadcast-message", sender, target, reason);
        if (broadcastMessage != null)
            Bukkit.broadcastMessage(broadcastMessage + ChatColor.GRAY + " 만료: " + expiration);

        return true;
    }

    private boolean handleUnbanCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ec.unban")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "사용법 : /밴해제 <플레이어>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "플레이어 " + args[0] + "님은 서버에 접속한 기록이 없습니다.");
            return true;
        }

        if (isNotBanned(sender, target)) {
            return true;
        }

        BanList<?> banList = Bukkit.getBanList(BanList.Type.NAME);
        banList.pardon(target.getName());

        sender.sendMessage(ChatColor.GREEN + target.getName() + "님의 밴이 해제되었습니다.");

        String broadcastMessage = getFormattedMessage("punishments.unban-broadcast-message", sender, target, null);
        if (broadcastMessage != null) Bukkit.broadcastMessage(broadcastMessage);

        return true;
    }
}