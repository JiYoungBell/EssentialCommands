package site.stopzero.dev.essentialCommands;

import org.bukkit.plugin.java.JavaPlugin;
import site.stopzero.dev.essentialCommands.commands.PunishmentCommands;
import site.stopzero.dev.essentialCommands.listeners.JoinQuitMessage;

public final class EssentialCommands extends JavaPlugin {

    @Override
    public void onLoad() {
        saveDefaultConfig();

        String loadMessage = "[ " + getConfig().getString("server-name", "EC")
                + " ] EssentialCommands 플러그인이 로딩중입니다...";
        getLogger().info(loadMessage);
    }

    @Override
    public void onEnable() {

        String enableMessage = "[ " + getConfig().getString("server-name", "EC")
                + " ] EssentialCommands 플러그인이 정상적으로 활성화 되었습니다.";
        getLogger().info(enableMessage);


        getServer().getPluginManager().registerEvents(new JoinQuitMessage(this), this);

        this.getCommand("추방").setExecutor(new PunishmentCommands(this));
        this.getCommand("밴").setExecutor(new PunishmentCommands(this));
        this.getCommand("기간밴").setExecutor(new PunishmentCommands(this));
        this.getCommand("밴해제").setExecutor(new PunishmentCommands(this));
    }

    @Override
    public void onDisable() {
        String disableMessage = "[ " + getConfig().getString("server-name", "EC")
                + " ] EssentialCommands 플러그인이 비활성화 되었습니다.";
        getLogger().info(disableMessage);
    }
}
