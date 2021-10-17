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

public class PopulationManagerCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("Population")
                .then(CommandManager.literal("Requests")
                        .then(CommandManager.literal("Accept")
                                .then(CommandManager.argument("Requester", StringArgumentType.string())
                                        .executes(context -> acceptJoinRequest(context, StringArgumentType.getString(context, "Requester")))))
                        .then(CommandManager.literal("Decline")
                                .then(CommandManager.argument("Requester", StringArgumentType.string())
                                        .executes(context -> declineJoinRequest(context, StringArgumentType.getString(context, "Requester"))))))
                .then(CommandManager.literal("Kick")
                        .then(CommandManager.argument("Player", StringArgumentType.string())
                                .executes(context -> kickMember(context, StringArgumentType.getString(context, "Player")))))
                .then(CommandManager.literal("Banish")
                        .then(CommandManager.argument("Player", StringArgumentType.string())
                                .executes(context -> banishMember(context, StringArgumentType.getString(context, "Player"))))));
    }

    private static int acceptJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        PlayerEntity request = playerManager.getPlayer(requester);
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null || request == null) return 1;
        KingdomProcedureChecks.acceptJoinRequest(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), request.getUuid(), executor.getUuid());
        return 1;
    }

    private static int declineJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        PlayerEntity request = playerManager.getPlayer(requester);
        if (executor == null || request == null) return 1;
        KingdomProcedureChecks.declineJoinRequest(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), request.getUuid(), executor.getUuid());
        return 1;
    }

    private static int kickMember(CommandContext<ServerCommandSource> context, String member) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        PlayerEntity player = playerManager.getPlayer(member);
        if (executor == null || player == null) return 1;
        KingdomProcedureChecks.removePlayer(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), player.getUuid(), executor.getUuid());
        return 1;
    }

    private static int banishMember(CommandContext<ServerCommandSource> context, String member) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        PlayerEntity player = playerManager.getPlayer(member);
        if (executor == null || player == null) return 1;
        KingdomProcedureChecks.banishPlayer(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), player.getUuid(), executor.getUuid());
        return 1;
    }

}
