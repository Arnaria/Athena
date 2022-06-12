package corviolis.athena.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.events.DuelEvent;
import corviolis.athena.services.events.EventManager;
import corviolis.athena.services.procedures.KingdomProcedures;
import corviolis.athena.util.BetterPlayerManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
import java.util.UUID;

public class DuelCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("duel")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> duel1v1(context, EntityArgumentType.getPlayer(context, "player")))
                )

                .then(CommandManager.literal("leave")
                        .executes(DuelCommands::cancel)
                )
        );

        dispatcher.register(CommandManager.literal("battle")
                .then(CommandManager.argument("nation", StringArgumentType.string()).suggests((context, builder) -> {
                            for (String kingdomId : KingdomsData.getKingdomIds()) builder.suggest(kingdomId);
                            return builder.buildFuture();
                        })
                        .executes(context -> battle(context, StringArgumentType.getString(context, "nation")))
                )

                .then(CommandManager.literal("leave")
                        .executes(DuelCommands::cancel)
                )
        );
    }

    public static int duel1v1(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        boolean isXpDuel = ((PlayerEntityInf) executor).canDuelForXp() && ((PlayerEntityInf) player).canDuelForXp();

        EventManager.startDuel(List.of(new ServerPlayerEntity[]{executor}), List.of(new ServerPlayerEntity[]{player}), isXpDuel);
        return 1;
    }

    public static int battle(CommandContext<ServerCommandSource> context, String kingdomId) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();

        for (UUID uuid : KingdomsData.getMembers(kingdomId)) {
            ServerPlayerEntity player = BetterPlayerManager.getPlayer(uuid);
            if (player != null) {

            }
        }
        return 1;
    }

    public static int cancel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();

        if (EventManager.getEvent(executor) instanceof DuelEvent event) {
            event.cancel(executor);
        }
        return 1;
    }
}
