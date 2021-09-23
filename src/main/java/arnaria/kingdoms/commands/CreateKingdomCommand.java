package arnaria.kingdoms.commands;

import arnaria.kingdoms.services.procedures.KingdomProcedureChecks;
import arnaria.kingdoms.util.InterfaceTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CreateKingdomCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("create")
                .then(CommandManager.argument("Name", StringArgumentType.string())
                        .executes(context -> createNewKingdom(context, StringArgumentType.getString(context, "Name")))));
    }

    private static int createNewKingdom(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        KingdomProcedureChecks.createKingdom(InterfaceTypes.COMMAND, name, context.getSource().getPlayer().getUuid());
        return 1;
    }
}
