package site.stopzero.dev.essentialCommands.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import site.stopzero.dev.essentialCommands.EssentialCommands;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MuteCommands implements CommandExecutor {

    private final EssentialCommands plugin;

    public MuteCommands(EssentialCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return switch (command.getName().toLowerCase()) {
            case "뮤트" -> handleMuteCommand(sender, args, false);
            case "시간뮤트" -> handleMuteCommand(sender, args, true);
            case "뮤트해제" -> handleUnmuteCommand(sender, args);
            default -> false;
        };
    }

    private boolean handleMuteCommand(CommandSender sender, String[] args, boolean isTemporary) {
        String requiredPermission = isTemporary ? "ec.tempmute" : "ec.mute";

        if (!checkPermission(sender, requiredPermission)) {
            return true;
        }

        int minArgs = isTemporary ? 2 : 1;
        if (args.length < minArgs) {
            if (isTemporary) {
                sender.sendMessage(ChatColor.RED + "사용법: /시간뮤트 <플레이어> <시간> [사유]");
                sender.sendMessage(ChatColor.GRAY + "시간 단위: 일, 시간, 분, 초. 예시: 7일, 12시간, 30분");

            } else {
                sender.sendMessage(ChatColor.RED + "사용법: /뮤트 <플레이어> [사유]");
            }

            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + args[0] + " 님은 서버에 접속한 기록이 없습니다.");
            return true;
        }

        if (plugin.getMuteManager().isMuted(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + args[0] + " 님은 이미 뮤트 상태입니다.");
            return true;
        }

        long durationMillis = -1;
        int reasonStartIndex = 1;

        String defaultReason = "서버 관리자에 의해 채팅이 차단되었습니다.";
        String messagePath = "punishments.mute-broadcast-message";
        String MuteMessage = ChatColor.RED + "관리자에 의해 서버에서 채팅이 차단되었습니다.";

        if (isTemporary) {
            durationMillis = parseDuration(sender, args[1]);
            if (durationMillis <= 0) {
                if (durationMillis == 0) {
                    sender.sendMessage("시간 값은 0보다 커야 합니다!");
                }

                return true;
            }
            reasonStartIndex = 2;
            defaultReason = "서버 관리자에 의해 채팅이 임시 차단되었습니다.";
            messagePath = "punishments.tempmute-broadcast-message";
        }

        String reason = buildReason(args, reasonStartIndex, defaultReason);
        long expirationTime = (durationMillis == -1) ? -1 : System.currentTimeMillis() + durationMillis;

        plugin.getMuteManager().mutePlayer(target.getUniqueId(), sender.getName(), reason, expirationTime);

        sender.sendMessage(ChatColor.GREEN + target.getName() + " 님을 뮤트했습니다. (사유: " + reason + ")");

        String broadcastMessage = plugin.getFormattedMessage(messagePath, sender, target, reason);
        if (broadcastMessage != null) {
            Bukkit.broadcastMessage(broadcastMessage);
            if (isTemporary) {
                Bukkit.broadcastMessage(ChatColor.GRAY + "만료: " + new Date(expirationTime));
            }
        }

        return true;

    }

    private boolean handleUnmuteCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ec.unmute")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "사용법: /뮤트해제 <플레이어>");

            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + args[0] + "님은 서버에 접속한 기록이 없습니다.");

            return true;
        }

        if (!plugin.getMuteManager().isMuted(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + target.getName() + " 님은 뮤트 상태가 아닙니다.");

            return true;
        }

        plugin.getMuteManager().unmutePlayer(target.getUniqueId());

        sender.sendMessage(ChatColor.GREEN + target.getName() + " 님의 뮤트를 해제했습니다.");

        String broadcastMessage =
                plugin.getFormattedMessage("punishments.unmute-broadcast-message", sender, target, null);

        if (broadcastMessage != null) Bukkit.broadcastMessage(broadcastMessage);

        return true;
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

}
