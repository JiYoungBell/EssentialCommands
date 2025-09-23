package site.stopzero.dev.essentialCommands.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            if (!sender.hasPermission("ec.kick")) {
                sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "사용법 : /추방 <플레이어> [사유]");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "플레이어 `" + args[0] + "`을/를 찾을 수 없습니다.");
                return true;
            }

            String reason = "관리자에 의해 추방되었습니다.";
            if (args.length > 1) {
                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
                reason = reasonBuilder.toString().trim();
            }

            String kickMessage = ChatColor.RED + "서버에서 추방되었습니다!\n사유 : " + reason;
            target.kickPlayer(kickMessage);
            Bukkit.broadcastMessage(ChatColor.YELLOW + target.getName() + "님이 "
                    + sender.getName() + "님에 의해 추방되었습니다. (사유: " + reason + ")");

            return true;
        }


        if (command.getName().equalsIgnoreCase("밴")) {
            if (!sender.hasPermission("ec.ban")) {
                sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "사용법 : /밴 <플레이어> [사유]");
                return true;
            }

            String targetName = args[0];

            String reason = "서버 관리자에 의해 차단되었습니다.";
            if (args.length > 1) {
                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
                reason = reasonBuilder.toString().trim();
            }

            Bukkit.getBanList(BanList.Type.NAME).addBan(targetName, reason, null, sender.getName());

            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                targetPlayer.kickPlayer(ChatColor.RED
                        + "관리자에 의해 서버에서 차단되었습니다.\n사유: " + reason);
            }

            Bukkit.broadcastMessage(ChatColor.YELLOW + targetName + " 님이 "
                    + sender.getName() + "님에 의해 서버에서 차단되었습니다. (사유: " + reason + ")");

            return true;
        }

        if (command.getName().equalsIgnoreCase("기간밴")) {
            if (!sender.hasPermission("ec.tempban")) {
                sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "사용법 : /기간밴 <플레이어> <시간> [사유]");
                sender.sendMessage(ChatColor.GRAY + "시간 단위 : 일, 시간, 분, 초. 예시: 7일, 12시간, 30분");
                return true;
            }

            String targetName = args[0]; // 플레이어
            String durationString = args[1]; // 시간 단위, 7일, 12시간 등

            long durationMillis;

            try {
                String numberString;
                long value;

                if (durationString.endsWith("일")) {
                    numberString = durationString.replace("일", "");
                    value = Long.parseLong(numberString);
                    durationMillis = TimeUnit.DAYS.toMillis(value);

                } else if (durationString.endsWith("시간")) {
                    numberString = durationString.replace("시간", "");
                    value = Long.parseLong(numberString);
                    durationMillis = TimeUnit.HOURS.toMillis(value);

                } else if (durationString.endsWith("분")) {
                    numberString = durationString.replace("분", "");
                    value = Long.parseLong(numberString);
                    durationMillis = TimeUnit.MINUTES.toMillis(value);

                } else if (durationString.endsWith("초")) {
                    numberString = durationString.replace("초", "");
                    value = Long.parseLong(numberString);
                    durationMillis = TimeUnit.SECONDS.toMillis(value);


                } else {
                    sender.sendMessage(ChatColor.RED + "알 수 없는 시간 단위입니다. (일, 시간, 분, 초)");
                    return true;
                }

            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED
                        + "잘못된 시간 형식입니다. 숫자와 단위를 함께 입력해주세요. (예: 7일, 30분");
                return true;
            }

            Date expiration = new Date(System.currentTimeMillis() + durationMillis);

            String reason = "서버 관리자에 의해 임시 차단되었습니다.";

            if (args.length > 2) {
                StringBuilder reasonBuilder = new StringBuilder();

                for (int i = 2; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
                reason = reasonBuilder.toString().trim();
            }

            Bukkit.getBanList(BanList.Type.NAME).addBan(targetName, reason, expiration, sender.getName());

            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer != null) {
                targetPlayer.kickPlayer(ChatColor.RED + "관리자에 의해 서버에서 임시 차단되었습니다.\n"
                        + "사유: " + reason
                        + "\n" + "만료일: " + expiration);
            }

            Bukkit.broadcastMessage(ChatColor.YELLOW + targetName + "님이 "
                    + sender.getName() + "님에 의해 서버에서 임시 차단되었습니다. (사유: " + reason + ")");

            return true;
        }
/*
        if (command.getName().equalsIgnoreCase("밴해제")) {

        }
*/

        return false;
    }
}
