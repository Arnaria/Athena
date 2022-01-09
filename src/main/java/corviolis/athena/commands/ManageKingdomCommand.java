package corviolis.athena.commands;

import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.procedures.KingdomProcedureChecks;
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

import java.util.UUID;

public class ManageKingdomCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("kingdom")
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("KingdomID", StringArgumentType.string())
                                .executes(context -> createNewKingdom(context, StringArgumentType.getString(context, "KingdomID")))))
                .then(CommandManager.literal("disband")
                        .executes(ManageKingdomCommand::disbandKingdom))
                .then(CommandManager.literal("advisers")
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("Player", GameProfileArgumentType.gameProfile()).suggests(((context, builder) -> {
                                            PlayerManager playerManager = ((ServerCommandSource)context.getSource()).getServer().getPlayerManager();
                                            String kingdomID = ((PlayerEntityInf) context.getSource().getPlayer()).getKingdomId();
                                            return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((player) -> {
                                                KingdomsData.getMembers(kingdomID);
                                                return false;
                                            }).map((player) -> {
                                                return player.getGameProfile().getName();
                                            }), builder);
                                        }))
                                        .executes(context -> addAdviser(context, EntityArgumentType.getPlayer(context, "Player")))))
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("Player", EntityArgumentType.player())
                                        .executes(context -> removeAdviser(context, EntityArgumentType.getPlayer(context, "Player"))))))
                .then(CommandManager.literal("colour")
                        .then(CommandManager.argument("Colour", ColorArgumentType.color())
                                .executes(context -> setKingdomColour(context, ColorArgumentType.getColor(context, "Colour")))))
                .then(CommandManager.literal("transfer")
                        .then(CommandManager.argument("Player", EntityArgumentType.player())
                                .executes(context -> transferKingship(context, EntityArgumentType.getPlayer(context, "Player"))))));
    }

    private static int createNewKingdom(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        KingdomProcedureChecks.createKingdom(InterfaceTypes.COMMAND, name, context.getSource().getPlayer().getUuid());
        return 1;
    }

    private static int disbandKingdom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.disbandKingdom(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), executor.getUuid());
        return 1;
    }

    private static int addAdviser(CommandContext<ServerCommandSource> context, ServerPlayerEntity adviser) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null || adviser == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedureChecks.addAdviser(platform, kingdom, adviser.getUuid(), executor.getUuid());
        return 1;
    }

    private static int removeAdviser(CommandContext<ServerCommandSource> context, ServerPlayerEntity adviser) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null || adviser == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedureChecks.removeAdviser(platform, kingdom, adviser.getUuid(), executor.getUuid());
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
        UUID heirUUID = heir.getUuid();
        KingdomProcedureChecks.transferKingShip(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), executor.getUuid(), heirUUID);
        return 1;
    }

}
