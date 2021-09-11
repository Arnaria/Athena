package arnaria.kingdoms.commands;

import arnaria.kingdoms.systems.procedures.VerificationProcedures;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class VerifyCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("verify")
                .then(CommandManager.argument("Token", StringArgumentType.string())
                        .executes(context -> verifyUser(context, StringArgumentType.getString(context, "Token"))))

                .then(CommandManager.literal("decline")
                .then(CommandManager.argument("Token", StringArgumentType.string())
                        .executes(context -> declineVerification(context, StringArgumentType.getString(context, "Token"))))));
    }

    public static int verifyUser(CommandContext<ServerCommandSource> context, String token) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();

        if (VerificationProcedures.verifyUser(token, executor.getUuid())) executor.sendMessage(new LiteralText("Your accounts have been linked!"), false);
        else executor.sendMessage(new LiteralText("There was an error linking your account. Try again"), false);
        return 1;
    }

    public static int declineVerification(CommandContext<ServerCommandSource> context, String token) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        VerificationProcedures.removeVerificationRequest(token);
        executor.sendMessage(new LiteralText("The verification request has been declined"), false);
        return 1;
    }
}