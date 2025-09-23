package site.stopzero.dev.essentialCommands;

import org.bukkit.plugin.java.JavaPlugin;
import site.stopzero.dev.essentialCommands.listeners.JoinQuitMessage;

public final class EssentialCommands extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info("동명타이쿤 EssentialCommands 플러그인이 정상적으로 켜졌습니다.");

        getServer().getPluginManager().registerEvents(new JoinQuitMessage(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("동명타이쿤 EssentialCommands 플러그인이 비활성화 되었습니다.");
    }
}
