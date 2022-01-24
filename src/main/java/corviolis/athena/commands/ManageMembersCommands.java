package corviolis.athena.commands;

import arnaria.notifacaitonlib.NotificationTypes;
import corviolis.athena.Athena;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.procedures.KingdomProcedureChecks;
import corviolis.athena.util.BetterPlayerManager;
import corviolis.athena.util.InterfaceTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class ManageMembersCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("members")
                .then(CommandManager.literal("requests")
                        .then(CommandManager.literal("accept")
                                .then(CommandManager.argument("Requester", StringArgumentType.string()).suggests((context, builder) -> {
                                            ServerPlayerEntity executor = context.getSource().getPlayer();
                                            for (UUID requester : KingdomsData.getJoinRequests(((PlayerEntityInf) executor).getKingdomId())) {
                                                builder.suggest(BetterPlayerManager.getName(requester));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> acceptJoinRequest(context, StringArgumentType.getString(context, "Requester")))))
                        .then(CommandManager.literal("decline")
                                .then(CommandManager.argument("Requester", StringArgumentType.string()).suggests((context, builder) -> {
                                            ServerPlayerEntity executor = context.getSource().getPlayer();
                                            for (UUID requester : KingdomsData.getJoinRequests(((PlayerEntityInf) executor).getKingdomId())) {
                                                builder.suggest(BetterPlayerManager.getName(requester));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> declineJoinRequest(context, StringArgumentType.getString(context, "Requester")))))
                        .then(CommandManager.literal("view")
                                .executes(ManageMembersCommands::viewJoinRequests)))
                .then(CommandManager.literal("kick")
                        .then(CommandManager.argument("Player", StringArgumentType.string()).suggests((context, builder) -> {
                                    ServerPlayerEntity executor = context.getSource().getPlayer();
                                    for (UUID member : KingdomsData.getMembers(((PlayerEntityInf) executor).getKingdomId())) {
                                        builder.suggest(BetterPlayerManager.getName(member));
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> kickMember(context, StringArgumentType.getString(context, "Player")))))
                .then(CommandManager.literal("banish")
                        .then(CommandManager.argument("Player", StringArgumentType.string()).suggests((context, builder) -> {
                                    ServerPlayerEntity executor = context.getSource().getPlayer();
                                    for (UUID member : KingdomsData.getMembers(((PlayerEntityInf) executor).getKingdomId())) {
                                        builder.suggest(BetterPlayerManager.getName(member));
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> banishMember(context, StringArgumentType.getString(context, "Player")))))
                .then(CommandManager.literal("unbanish")
                        .then(CommandManager.argument("Player", StringArgumentType.string()).suggests((context, builder) -> {
                                    ServerPlayerEntity executor = context.getSource().getPlayer();
                                    for (UUID member : KingdomsData.getBlockedPlayers(((PlayerEntityInf) executor).getKingdomId())) {
                                        builder.suggest(BetterPlayerManager.getName(member));
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> unbanishMember(context, StringArgumentType.getString(context, "Player")))))
                .then(CommandManager.literal("view")
                        .executes(ManageMembersCommands::viewMembers)));
    }

    private static int viewMembers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        String kingdomId = ((PlayerEntityInf) executor).getKingdomId();
        if (kingdomId.isEmpty()) return 1;
        for (UUID member : KingdomsData.getMembers(kingdomId)) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), BetterPlayerManager.getName(member), NotificationTypes.INFO);
        }
        return 1;
    }

    private static int viewJoinRequests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        String kingdomId = ((PlayerEntityInf) executor).getKingdomId();
        if (kingdomId.isEmpty()) return 1;
        for (UUID member : KingdomsData.getJoinRequests(kingdomId)) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), BetterPlayerManager.getName(member), NotificationTypes.INFO);
        }
        return 1;
    }

    private static int acceptJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.acceptJoinRequest(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), BetterPlayerManager.getUuid(requester), executor.getUuid());
        return 1;
    }

    private static int declineJoinRequest(CommandContext<ServerCommandSource> context, String requester) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.declineJoinRequest(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), BetterPlayerManager.getUuid(requester), executor.getUuid());
        return 1;
    }

    private static int kickMember(CommandContext<ServerCommandSource> context, String member) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.removePlayer(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), BetterPlayerManager.getUuid(member), executor.getUuid());
        return 1;
    }

    private static int banishMember(CommandContext<ServerCommandSource> context, String member) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.banishPlayer(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), BetterPlayerManager.getUuid(member), executor.getUuid());
        return 1;
    }

    private static int unbanishMember(CommandContext<ServerCommandSource> context, String member) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.unBanishPlayer(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), BetterPlayerManager.getUuid(member), executor.getUuid());
        return 1;
    }
}
