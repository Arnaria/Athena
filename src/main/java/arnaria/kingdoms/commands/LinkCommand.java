package arnaria.kingdoms.commands;

import arnaria.kingdoms.services.procedures.LinkingProcedures;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class LinkCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("link")
                .then(CommandManager.argument("token", StringArgumentType.string())
                .executes(context -> acceptLinkRequest(context, StringArgumentType.getString(context, "token"))))
        );
    }

    public static int acceptLinkRequest(CommandContext<ServerCommandSource> context, String token) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        LinkingProcedures.acceptLinkRequest(token, executor.getUuid());
        return 1;
    }
}