package corviolis.athena.commands;

import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.events.EventManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RevolutionCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("revolt")
                .executes(RevolutionCommand::startRevolution));
    }

    public static int startRevolution(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        EventManager.startRevolution(((PlayerEntityInf) context.getSource().getPlayer()).getKingdomId());
        return 1;
    }
}
