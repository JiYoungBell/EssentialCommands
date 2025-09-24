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

    // [유지] 이 메서드들은 계속 사용되므로 변경 없음
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
            sender.sendMessage(ChatColor.RED + "플레이어 " + playerName + "님을 찾을 수 없습니다.");
        }

        return target;
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

    // [유지] 추방(kick)은 온라인 플레이어 대상이므로 로직 변경 없음
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
        Bukkit.broadcastMessage(ChatColor.YELLOW + target.getName() + "님이 "
                + sender.getName() + "님에 의해 추방되었습니다. (사유: " + reason + ")");

        return true;
    }

    // [변경] handleBanCommand - UUID 기반 비동기 처리로 변경
    private boolean handleBanCommand(CommandSender sender, String [] args) {
        if (!checkPermission(sender, "ec.ban")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "사용법 : /밴 <플레이어> [사유]");
            return true;
        }

        String targetName = args[0];
        String reason = buildReason(args, 1, "서버 관리자에 의해 차단 되었습니다.");

        // 1. 비동기적으로 플레이어 정보(OfflinePlayer)를 조회
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            // 2. 서버 메인 스레드에서 밴 처리 및 메시지 전송
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "플레이어 " + targetName + "님의 정보를 찾을 수 없습니다.");
                    return;
                }

                if (target.isBanned()) {
                    sender.sendMessage(ChatColor.RED + targetName + " 님은 이미 밴 상태입니다.");
                    return;
                }

                // 3. PlayerProfile을 사용하여 밴
                Bukkit.getBanList(BanList.Type.PROFILE).addBan(String.valueOf(target.getPlayerProfile()), reason, null, sender.getName());

                if (target.isOnline()) {
                    target.getPlayer().kickPlayer(ChatColor.RED + "관리자에 의해 서버에서 차단되었습니다.\n사유: " + reason);
                }

                Bukkit.broadcastMessage(ChatColor.YELLOW + target.getName() + " 님이 "
                        + sender.getName() + "님에 의해 서버에서 차단되었습니다. (사유: " + reason + ")");
            });
        });

        return true;
    }

    // [변경] handleTempBanCommand - UUID 기반 비동기 처리로 변경
    private boolean handleTempBanCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ec.tempban")) {
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "사용법 : /기간밴 <플레이어> <시간> [사유]");
            sender.sendMessage(ChatColor.GRAY + "시간 단위 : 일, 시간, 분, 초. 예시: 7일, 12시간, 30분");
            return true;
        }

        String targetName = args[0];
        long durationMillis = parseDuration(sender, args[1]);
        if (durationMillis <= 0) {
            if (durationMillis == 0) {
                sender.sendMessage("시간 값은 0보다 커야 합니다!");
            }
            return true;
        }

        Date expiration = new Date(System.currentTimeMillis() + durationMillis);
        String reason = buildReason(args, 2, "서버 관리자에 의해 임시 차단되었습니다.");

        // 1. 비동기적으로 플레이어 정보 조회
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            // 2. 메인 스레드에서 밴 처리
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "플레이어 " + targetName + "님의 정보를 찾을 수 없습니다.");
                    return;
                }

                if (target.isBanned()) {
                    sender.sendMessage(ChatColor.RED + targetName + " 님은 이미 밴 상태입니다.");
                    return;
                }

                // 3. PlayerProfile을 사용하여 기간밴
                Bukkit.getBanList(BanList.Type.PROFILE).addBan(String.valueOf(target.getPlayerProfile()), reason, expiration, sender.getName());

                if (target.isOnline()) {
                    target.getPlayer().kickPlayer(ChatColor.RED + "관리자에 의해 서버에서 임시 차단되었습니다.\n"
                            + "사유: " + reason
                            + "\n" + "만료: " + expiration);
                }

                Bukkit.broadcastMessage(ChatColor.YELLOW + target.getName() + "님이 "
                        + sender.getName() + "님에 의해 서버에서 임시 차단되었습니다. (사유: " + reason + ")"
                        + "\n만료: " + expiration);
            });
        });

        return true;
    }

    // [변경] handleUnbanCommand - UUID 기반 비동기 처리로 변경
    private boolean handleUnbanCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ec.unban")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "사용법 : /밴해제 <플레이어>");
            return true;
        }

        String targetName = args[0];

        // 1. 비동기적으로 플레이어 정보 조회
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            // 2. 메인 스레드에서 밴 해제 처리
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "플레이어 " + targetName + "님의 정보를 찾을 수 없습니다.");
                    return;
                }

                if (!target.isBanned()) {
                    sender.sendMessage(ChatColor.RED + targetName + " 님은 밴 상태가 아닙니다.");
                    return;
                }

                // 3. PlayerProfile을 사용하여 밴 해제
                Bukkit.getBanList(BanList.Type.PROFILE).pardon(String.valueOf(target.getPlayerProfile()));

                sender.sendMessage(ChatColor.GREEN + target.getName() + "님의 밴이 해제되었습니다.");
                Bukkit.broadcastMessage(ChatColor.YELLOW + sender.getName() + "님이 " + target.getName() + "님의 밴을 해제하였습니다.");
            });
        });

        return true;
    }
}
