package site.stopzero.dev.essentialCommands.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import site.stopzero.dev.essentialCommands.EssentialCommands;

public class PunishmentCommands implements CommandExecutor {

    private final EssentialCommands plugin;

    public PunishmentCommands(EssentialCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("추방")) {
            if (!sender.hasPermission("es.kick")) {
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

        return false;
    }
}
