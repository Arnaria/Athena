package arnaria.kingdoms.commands;

import arnaria.kingdoms.services.procedures.LinkProcedures;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class LinkCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("link")
                .then(CommandManager.argument("Token", StringArgumentType.string())
                        .executes(context -> verifyUser(context, StringArgumentType.getString(context, "Token")))));
    }

    public static int verifyUser(CommandContext<ServerCommandSource> context, String linkToken) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor != null) LinkProcedures.linkAccounts(linkToken, executor.getUuid());
        return 1;
    }
}