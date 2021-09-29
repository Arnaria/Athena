package arnaria.kingdoms.commands;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.procedures.KingdomProcedureChecks;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
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
                                        .executes(context -> declineJoinRequest(context, StringArgumentType.getString(context, "request")))))));
    }

    private static int sendJoinRequest(CommandContext<ServerCommandSource> context, String kingdom) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 1;
        KingdomProcedures.addJoinRequest(kingdom, context.getSource().getPlayer().getUuid());
        return 1;
    }

    private static int acceptJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        PlayerEntity request = playerManager.getPlayer(requester);
        if (request == null) return 1;
        UUID requestUUID = request.getUuid();
        PlayerEntity player = context.getSource().getPlayer();
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) player).getKingdomId();
        KingdomProcedures.addMember(kingdom, requestUUID);
        KingdomProcedures.removeJoinRequest(kingdom ,requestUUID);
        NotificationManager.send(player.getUuid(), requester + " has joined " + kingdom + "!", NotificationTypes.EVENT);
        NotificationManager.send(requestUUID, "You have been accepted into " + kingdom, NotificationTypes.EVENT);
        return 1;
    }

    private static int declineJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        PlayerEntity request = playerManager.getPlayer(requester);
        if (request == null) return 1;
        UUID requestUUID = request.getUuid();
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) player).getKingdomId();
        KingdomProcedureChecks.declineJoinRequest(platform, kingdom, player.getUuid(), requestUUID);
        return 1;
    }
}
