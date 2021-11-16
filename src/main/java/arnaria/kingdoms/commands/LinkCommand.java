package arnaria.kingdoms.commands;

import arnaria.kingdoms.services.procedures.LinkingProcedures;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
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
                .executes(LinkCommand::viewLink)

                .then(CommandManager.argument("token", StringArgumentType.string())
                .executes(context -> acceptLinkRequest(context, StringArgumentType.getString(context, "token"))))
        );

        dispatcher.register(CommandManager.literal("unlink")
                .executes(LinkCommand::unlinkAccounts)
        );
    }

    public static int viewLink(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        String email = LinkingProcedures.getLinkedEmail(executor.getUuid());

        if (email != null) NotificationManager.send(executor.getUuid(), "Your account is linked to: " + email, NotificationTypes.INFO);
        else NotificationManager.send(executor.getUuid(), "Your account is not linked", NotificationTypes.WARN);
        return 1;
    }

    public static int acceptLinkRequest(CommandContext<ServerCommandSource> context, String token) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (LinkingProcedures.tokenInService(token) && !LinkingProcedures.accountLinked(executor.getUuid())) {
            LinkingProcedures.acceptLinkRequest(token, executor.getUuid());
        }
        return 1;
    }

    public static int unlinkAccounts(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (LinkingProcedures.accountLinked(executor.getUuid())) {
            LinkingProcedures.unlinkAccounts(executor.getUuid());
        }
        return 1;
    }
}