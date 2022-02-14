package corviolis.athena.commands;

import arnaria.notifacaitonlib.NotificationTypes;
import com.mojang.authlib.GameProfile;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.procedures.KingdomProcedureChecks;
import corviolis.athena.util.BetterPlayerManager;
import corviolis.athena.util.InterfaceTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.UUID;

public class ManageTeamCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nation")
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("team", StringArgumentType.string())
                                .executes(context -> createNewKingdom(context, StringArgumentType.getString(context, "team")))))
                .then(CommandManager.literal("disband")
                        .executes(ManageTeamCommand::disbandKingdom))
                .then(CommandManager.literal("advisers")
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("Player", StringArgumentType.string()).suggests(((context, builder) -> {
                                            PlayerEntity player =  context.getSource().getPlayer();
                                            String kingdomId = ((PlayerEntityInf) player).getKingdomId();

                                            if (!kingdomId.isEmpty()) {
                                                for (UUID uuid : KingdomsData.getMembers(kingdomId)) {
                                                    if (!player.getUuid().equals(uuid)) {
                                                        builder.suggest(BetterPlayerManager.getName(uuid));
                                                    }
                                                }
                                            }
                                            return builder.buildFuture();
                                        }))
                                        .executes(context -> addAdviser(context, StringArgumentType.getString(context, "Player")))))
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("Player", StringArgumentType.string()).suggests((context, builder) -> {
                                            PlayerEntity player =  context.getSource().getPlayer();
                                            String kingdomId = ((PlayerEntityInf) player).getKingdomId();

                                            if (!kingdomId.isEmpty()) {
                                                for (UUID uuid : KingdomsData.getAdvisers(kingdomId)) {
                                                    builder.suggest(BetterPlayerManager.getName(uuid));
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> removeAdviser(context, StringArgumentType.getString(context, "Player"))))))
                .then(CommandManager.literal("colour")
                        .then(CommandManager.argument("Colour", ColorArgumentType.color())
                                .executes(context -> setKingdomColour(context, ColorArgumentType.getColor(context, "Colour")))))
                .then(CommandManager.literal("transfer")
                        .then(CommandManager.argument("Player", EntityArgumentType.player())
                                .executes(context -> transferKingship(context, EntityArgumentType.getPlayer(context, "Player")))))
                .then(CommandManager.literal("rename")
                        .then(CommandManager.argument("New Team name", StringArgumentType.string())
                                .executes(context -> renameKingdom(context, StringArgumentType.getString(context, "New Team name")))))
        );
    }

    private static int createNewKingdom(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        if (name.length() >= 50) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, context.getSource().getPlayer().getUuid(), "This team name is to long" ,NotificationTypes.ERROR);
        }
        KingdomProcedureChecks.createKingdom(InterfaceTypes.COMMAND, name, context.getSource().getPlayer().getUuid());
        return 1;
    }

    private static int disbandKingdom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.disbandKingdom(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), executor.getUuid());
        return 1;
    }

    private static int addAdviser(CommandContext<ServerCommandSource> context, String adviser) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();

        if (executor == null || adviser == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedureChecks.addAdviser(platform, kingdom, BetterPlayerManager.getUuid(adviser), executor.getUuid());
        return 1;
    }

    private static int removeAdviser(CommandContext<ServerCommandSource> context, String adviser) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();

        if (executor == null || adviser == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedureChecks.removeAdviser(platform, kingdom, BetterPlayerManager.getUuid(adviser), executor.getUuid());
        return 1;
    }

    public static int setKingdomColour(CommandContext<ServerCommandSource> context, Formatting colour) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 1;
        KingdomProcedureChecks.setColour(InterfaceTypes.COMMAND, ((PlayerEntityInf) player).getKingdomId(), colour, player.getUuid());
        return 1;
    }

    private static int transferKingship(CommandContext<ServerCommandSource> context, ServerPlayerEntity heir) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null || heir == null) return 1;
        KingdomProcedureChecks.transferKingShip(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), heir.getUuid(), executor.getUuid());
        return 1;
    }

    private static int renameKingdom(CommandContext<ServerCommandSource> context, String newKingdomName) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        KingdomProcedureChecks.renameKingdom(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), newKingdomName, executor.getUuid());
        return 1;
    }

}
