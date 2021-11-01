package arnaria.kingdoms.commands;

import arnaria.kingdoms.services.claims.ClaimManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

//I'm tired shut up
public class AdminClaimCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("admin_claim_this_bitch").requires(source -> source.hasPermissionLevel(4))
                    .executes(AdminClaimCommand::addClaim)

                .then(CommandManager.literal("admin_drop_kick_this_claim")
                    .executes(AdminClaimCommand::removeClaim))
        );
    }

    public static int addClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        ClaimManager.addAdminClaim(executor.getBlockPos());

        executor.sendMessage(new LiteralText("Yeah she good boss man"), false);
        return 1;
    }

    public static int removeClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        ClaimManager.dropAdminClaim(executor.getBlockPos());

        executor.sendMessage(new LiteralText("Cya looser"), false);
        return 1;
    }
}
