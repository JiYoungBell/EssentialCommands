package site.stopzero.dev.essentialCommands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import site.stopzero.dev.essentialCommands.commands.MuteCommands;
import site.stopzero.dev.essentialCommands.commands.PunishmentCommands;
import site.stopzero.dev.essentialCommands.listeners.BanMessage;
import site.stopzero.dev.essentialCommands.listeners.JoinQuitMessage;
import site.stopzero.dev.essentialCommands.listeners.ChatMuteListener;
import site.stopzero.dev.essentialCommands.managers.DatabaseManager;
import site.stopzero.dev.essentialCommands.managers.MuteManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class EssentialCommands extends JavaPlugin {

    private DatabaseManager databaseManager;
    private MuteManager muteManager;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        String loadMessage = "[ " + getConfig().getString("server-name", "EC")
                + " ] EssentialCommands 플러그인이 로딩중입니다...";
        getLogger().info(loadMessage);
    }



    @Override
    public void onEnable() {

        if (!loadConfiguration()) {
            getLogger().severe("config.yml 파일의 문제로 인해 EssentialCommands 플러그인을 비활성화합니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.getConnection();
            databaseManager.initializeDatabase();

        } catch (SQLException e) {
            getLogger().severe("데이터베이스 연결에 실패하여 플러그인을 비활성화합니다.");
            getLogger().severe("config.yml의 database 설정을 확인해주세요.");

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        muteManager = new MuteManager();

        //입퇴장 메세지 리스너
        getServer().getPluginManager().registerEvents(new JoinQuitMessage(this), this);
        //벤 상태 유저 접속 시 리스너
        getServer().getPluginManager().registerEvents(new BanMessage(this), this);
        // 플레이어 채팅 관련 리스너
        getServer().getPluginManager().registerEvents(new ChatMuteListener(this), this);


        getCommand("추방").setExecutor(new PunishmentCommands(this));
        getCommand("밴").setExecutor(new PunishmentCommands(this));
        getCommand("기간밴").setExecutor(new PunishmentCommands(this));
        getCommand("밴해제").setExecutor(new PunishmentCommands(this));

        getCommand("뮤트").setExecutor(new MuteCommands(this));
        getCommand("시간뮤트").setExecutor(new MuteCommands(this));
        getCommand("뮤트해제").setExecutor(new MuteCommands(this));


        String enableMessage = "[" + getConfig().getString("server-name", "EC")
                + "] EssentialCommands 플러그인이 정상적으로 활성화 되었습니다.";
        getLogger().info(enableMessage);
    }

    @Override
    public void onDisable() {
        // 데이터베이스 비활성화
        if (databaseManager != null) databaseManager.closeConnection();

        String disableMessage = "[" + getConfig().getString("server-name", "EC")
                + "] EssentialCommands 플러그인이 비활성화 되었습니다.";
        getLogger().info(disableMessage);
    }


    public MuteManager getMuteManager() {
        return muteManager;
    }


    public boolean loadConfiguration() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            getConfig().load(configFile);
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            getLogger().severe("!!! config.yml 파일에 오류가 발견되었습니다. !!!");
            getLogger().severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            getLogger().severe("오류 내용: " + e.getMessage());

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File crashFile = new File(getDataFolder(), "config_crash_" + timestamp + ".yml");

            if (configFile.renameTo(crashFile)) {
                getLogger().info("기존 설정 파일을 '" + crashFile.getName() + "` 로 백업했습니다.");

            } else {
                getLogger().severe("기존 설정 파일을 백업하는 데 실패했습니다. 수동으로 확인해주세요.");
                return false;
            }

            saveDefaultConfig();
            getLogger().info("새로운 기본 config.yml 파일을 생성했습니다.");
            getLogger().warning("서버를 중지한 후, " +
                    "백업된 파일을 참고하여 새 config.yml 파일을 구성한 뒤 서버를 재시작 해주세요.");

            reloadConfig();
            return true;
        }
    }

    public String getFormattedMessage(String path, CommandSender sender, OfflinePlayer target, String reason) {
        String message = getConfig().getString(path);

        if (message == null || message.isEmpty()) {
            return null;
        }

        if (target != null) message = message.replace("%player%", target.getName());
        if (reason != null) message = message.replace("%reason%", reason);
        message = message.replace("%sender%", sender.getName());

        return ChatColor.translateAlternateColorCodes('&', message);
    }


    public DatabaseManager getDababaseManager() {
        return databaseManager;
    }

}
