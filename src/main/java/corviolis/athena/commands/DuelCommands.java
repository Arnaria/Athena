package corviolis.athena.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.events.DuelEvent;
import corviolis.athena.services.events.EventManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

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
    }

    public static int duel1v1(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        boolean isXpDuel = ((PlayerEntityInf) executor).canDuelForXp() && ((PlayerEntityInf) player).canDuelForXp();

        EventManager.startDuel(List.of(new ServerPlayerEntity[]{executor}), List.of(new ServerPlayerEntity[]{player}), isXpDuel);
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
