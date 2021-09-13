package arnaria.kingdoms.commands;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import static arnaria.kingdoms.Kingdoms.playerManager;

public class GetKingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("Leader")
                .then(CommandManager.argument("Kingdom", StringArgumentType.string())
                        .executes(context -> getLeader(context, StringArgumentType.getString(context,"Kingdom")))));
    }

    private static int getLeader(CommandContext<ServerCommandSource> context, String kingdomID) throws CommandSyntaxException {
        NotificationManager.send(context.getSource().getPlayer().getUuid(), playerManager.getPlayer(KingdomsData.getKing(kingdomID)) + " is the Leader of " + kingdomID, NotificationTypes.INFO);
        return 1;
    }
}
