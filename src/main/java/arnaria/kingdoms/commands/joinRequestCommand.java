package arnaria.kingdoms.commands;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static arnaria.kingdoms.Kingdoms.playerManager;

import java.util.UUID;

public class joinRequestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("Requests")
                .then(CommandManager.literal("Join")
                        .then(CommandManager.literal("Send")
                                .then(CommandManager.argument("Kingdom", StringArgumentType.string())
                                        .executes(context -> sendJoinRequest(context, StringArgumentType.getString(context,"Kingdom")))))
                        .then(CommandManager.literal("Accept")
                                .then(CommandManager.argument("request", StringArgumentType.string())
                                        .executes(context -> acceptJoinRequest(context, StringArgumentType.getString(context, "request")))))
                        .then(CommandManager.literal("Decline")
                                .then(CommandManager.argument("request", StringArgumentType.string())
                                        .executes(context -> acceptJoinRequest(context, StringArgumentType.getString(context, "request")))))));
    }

    private static int sendJoinRequest(CommandContext<ServerCommandSource> context, String kingdom) throws CommandSyntaxException {
        KingdomProcedures.addJoinRequest(InterfaceTypes.COMMAND, kingdom, context.getSource().getPlayer().getUuid());
        return 1;
    }

    private static int acceptJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        UUID request = playerManager.getPlayer(requester).getUuid();
        PlayerEntity player = context.getSource().getPlayer();
        String kingdom = ((PlayerEntityInf) player).getKingdomId();
        if (KingdomsData.getKing(kingdom) != player.getUuid()) {
            if (!KingdomsData.getJoinRequests(kingdom).contains(request)) {
                NotificationManager.send(player.getUuid(), requester + " has not asked to join your kingdom.", NotificationTypes.WARN);
                return 1;
            }
        NotificationManager.send(player.getUuid(), "Only a leader can use this command", NotificationTypes.ERROR);
        return 1;
        }
        KingdomProcedures.addMember(kingdom, request);
        KingdomProcedures.deleteJoinRequest(kingdom, request);
        NotificationManager.send(player.getUuid(), requester + " has joined " + kingdom + "!", NotificationTypes.EVENT);
        NotificationManager.send(request, "You have been accepted into " + kingdom, NotificationTypes.EVENT);
        return 1;
    }

    private static int declineJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        UUID request = playerManager.getPlayer(requester).getUuid();
        PlayerEntity player = context.getSource().getPlayer();
        String kingdom = ((PlayerEntityInf) player).getKingdomId();
        if (KingdomsData.getKing(kingdom) != player.getUuid()) {
            if (!KingdomsData.getJoinRequests(kingdom).contains(request)) {
                NotificationManager.send(player.getUuid(), requester + " has not asked to join " + kingdom + ".", NotificationTypes.WARN);
                return 1;
            }
            NotificationManager.send(player.getUuid(), "Only a leader can use this command", NotificationTypes.ERROR);
            return 1;
        }
        KingdomProcedures.deleteJoinRequest(kingdom, request);
        NotificationManager.send(player.getUuid(), requester + " has been declined from joining " + kingdom, NotificationTypes.EVENT);
        NotificationManager.send(request, "You have been declined from joining " + kingdom, NotificationTypes.EVENT);
        return 1;
    }
}
