package site.stopzero.dev.essentialCommands.managers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuteManager {

    private final ConcurrentHashMap<UUID, Muteinfo>
            mutedPlayers = new ConcurrentHashMap<>();

    public void mutePlayer(UUID uuid, String source, String reason, long expirationTime) {
        mutedPlayers.put(uuid, new Muteinfo(source, reason, expirationTime));
    }

    public void unmutePlayer(UUID uuid) {
        mutedPlayers.remove(uuid);
    }

    public boolean isMuted(UUID uuid) {
        if (!mutedPlayers.containsKey(uuid)) {
            return false;
        }

        Muteinfo info = mutedPlayers.get(uuid);

        if (info.getExpirationTime() != -1 && info.getExpirationTime() < System.currentTimeMillis()) {
            unmutePlayer(uuid);
            return false;
        }

        return true;
    }

    public Muteinfo getMuteInfo(UUID uuid) {
        return mutedPlayers.get(uuid);
    }

    public static class Muteinfo {
        private final String source;
        private final String reason;
        private final long expirationTime;

        public Muteinfo(String source, String reason, long expirationTime) {
            this.source = source;
            this.reason = reason;
            this.expirationTime = expirationTime;
        }

        public String getSource() {
            return source;
        }

        public String getReason() {
            return reason;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }
}
