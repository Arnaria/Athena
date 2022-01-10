package corviolis.athena.util;

import com.mojang.authlib.GameProfile;
import corviolis.athena.Athena;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class BetterPlayerManager {

    public static ServerPlayerEntity getPlayer(UUID uuid) {
        return Athena.playerManager.getPlayer(uuid);
    }

    public static ServerPlayerEntity getPlayer(String name) {
        return Athena.playerManager.getPlayer(name);
    }

    public static String getName(UUID uuid) {
        Optional<GameProfile> gameProfile = Athena.userCache.getByUuid(uuid);
        return gameProfile.map(GameProfile::getName).orElse(null);
    }

    public static UUID getUuid(String name) {
        Optional<GameProfile> gameProfile = Athena.userCache.findByName(name);
        return gameProfile.map(GameProfile::getId).orElse(null);
    }

    public static ArrayList<ServerPlayerEntity> getOnlinePlayers() {
        return (ArrayList<ServerPlayerEntity>) Athena.playerManager.getPlayerList();
    }
}