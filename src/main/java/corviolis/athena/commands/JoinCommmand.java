package corviolis.athena.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import corviolis.athena.services.procedures.KingdomProcedureChecks;
import corviolis.athena.util.InterfaceTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class JoinCommmand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("join")
                    .then(CommandManager.argument("Kingdom", StringArgumentType.string())
                            .executes(context -> sendJoinRequest(context, StringArgumentType.getString(context,"Kingdom")))));
    }

    private static int sendJoinRequest(CommandContext<ServerCommandSource> context, String kingdom) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 1;
        KingdomProcedureChecks.addJoinRequest(InterfaceTypes.COMMAND, kingdom, player.getUuid());
        return 1;
    }
}
