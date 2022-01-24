package corviolis.athena.commands;

import corviolis.athena.services.data.KingdomsData;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import corviolis.athena.util.BetterPlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class GetKingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("leader")
                .then(CommandManager.argument("team", StringArgumentType.string()).suggests((context, builder) -> {
                            for (String kingdomId : KingdomsData.getKingdomIds()) builder.suggest(kingdomId);
                            return builder.buildFuture();
                        })
                        .executes(context -> getLeader(context, StringArgumentType.getString(context,"team")))));
    }

    private static int getLeader(CommandContext<ServerCommandSource> context, String kingdomID) throws CommandSyntaxException {
        NotificationManager.send(context.getSource().getPlayer().getUuid(), BetterPlayerManager.getName(KingdomsData.getKing(kingdomID)) + " is the Leader of " + kingdomID, NotificationTypes.INFO);
        return 1;
    }
}
