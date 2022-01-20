package corviolis.athena.commands;

import corviolis.athena.Athena;
import corviolis.athena.services.data.KingdomsData;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class GetKingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("leader")
                .then(CommandManager.argument("Kingdom", StringArgumentType.string())
                        .executes(context -> getLeader(context, StringArgumentType.getString(context,"Kingdom")))));
    }

    private static int getLeader(CommandContext<ServerCommandSource> context, String kingdomID) throws CommandSyntaxException {
        NotificationManager.send(context.getSource().getPlayer().getUuid(), Athena.playerManager.getPlayer(KingdomsData.getKing(kingdomID)) + " is the Leader of " + kingdomID, NotificationTypes.INFO);
        return 1;
    }
}
