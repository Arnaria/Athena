package arnaria.kingdoms.commands;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.procedures.KingdomProcedureChecks;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;


import static arnaria.kingdoms.Kingdoms.playerManager;
import java.util.UUID;

public class JoinRequestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("Join")
                        .then(CommandManager.argument("Kingdom", StringArgumentType.string())
                                .executes(context -> sendJoinRequest(context, StringArgumentType.getString(context,"Kingdom")))));
    }

    private static int sendJoinRequest(CommandContext<ServerCommandSource> context, String kingdom) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        KingdomProcedureChecks.addJoinRequest(platform, kingdom, context.getSource().getPlayer().getUuid());
        return 1;
    }

}
