package arnaria.kingdoms.commands;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedureChecks;
import arnaria.kingdoms.util.InterfaceTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.playerManager;

public class TransferKingshipCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("Transfer")
                .then(CommandManager.argument("Player", StringArgumentType.string())
                        .executes(context -> transferKingship(context, StringArgumentType.getString(context, "Player")))));
    }

    private static int transferKingship(CommandContext<ServerCommandSource> context, String player) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        PlayerEntity heir = playerManager.getPlayer(player);
        if (heir == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        UUID heirUUID = heir.getUuid();
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedureChecks.transferKingShip(platform, kingdom, executor.getUuid(), heirUUID);
        return 1;
    }
}
