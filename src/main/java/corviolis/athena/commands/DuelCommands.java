package corviolis.athena.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.events.DuelEvent;
import corviolis.athena.services.events.Event;
import corviolis.athena.services.events.EventManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DuelCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("duel")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> duel(context, EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    public static int duel(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        boolean isXpDuel = ((PlayerEntityInf) executor).canDuelForXp() && ((PlayerEntityInf) player).canDuelForXp();
        EventManager.startDuel(executor, player, isXpDuel);
        return 1;
    }

    public static int cancel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();

        if (EventManager.getEvent(executor) instanceof DuelEvent event) {

        }
    }
}
