package arnaria.kingdoms.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.playerManager;
import static arnaria.kingdoms.Kingdoms.userCache;

public class BetterPlayerManager {

    public static ServerPlayerEntity getPlayer(UUID uuid) {
        return playerManager.getPlayer(uuid);
    }

    public static ServerPlayerEntity getPlayer(String name) {
        return playerManager.getPlayer(name);
    }

    public static String getName(UUID uuid) {
        Optional<GameProfile> gameProfile = userCache.getByUuid(uuid);
        return gameProfile.map(GameProfile::getName).orElse(null);
    }

    public static UUID getUuid(String name) {
        Optional<GameProfile> gameProfile = userCache.findByName(name);
        return gameProfile.map(GameProfile::getId).orElse(null);
    }

    public static ArrayList<ServerPlayerEntity> getOnlinePlayers() {
        return (ArrayList<ServerPlayerEntity>) playerManager.getPlayerList();
    }
}