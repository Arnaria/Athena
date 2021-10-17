package arnaria.kingdoms.commands;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.procedures.KingdomProcedureChecks;
import arnaria.kingdoms.util.InterfaceTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static arnaria.kingdoms.Kingdoms.playerManager;

public class ManageAdviserCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("Advisers")
                .then(CommandManager.literal("Add")
                        .then(CommandManager.argument("Player", StringArgumentType.string())
                                .executes(context -> addAdviser(context, StringArgumentType.getString(context, "Player")))))
                .then(CommandManager.literal("Remove")
                        .then(CommandManager.argument("Player", StringArgumentType.string())
                                .executes(context -> addAdviser(context, StringArgumentType.getString(context, "Player"))))));
    }

    private static int addAdviser(CommandContext<ServerCommandSource> context, String player) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        PlayerEntity adviser = playerManager.getPlayer(player);
        if (executor == null || adviser == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedureChecks.addAdviser(platform, kingdom, adviser.getUuid(), executor.getUuid());
        return 1;
    }

    private static int removeAdviser(CommandContext<ServerCommandSource> context, String player) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        PlayerEntity adviser = playerManager.getPlayer(player);
        if (executor == null || adviser == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedureChecks.removeAdviser(platform, kingdom, adviser.getUuid(), executor.getUuid());
        return 1;
    }
}
