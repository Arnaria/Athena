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
public class ClaimManagerCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("claims").requires(source -> source.hasPermissionLevel(4))

                .then(CommandManager.literal("admin_claim_this_bitch")
                    .executes(ClaimManagerCommands::addClaim))

                .then(CommandManager.literal("drop_kick_this_chunk")
                    .executes(ClaimManagerCommands::removeClaim))
        );
    }

    public static int addClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();

        if (!ClaimManager.claimExistsAt(executor.getChunkPos())) {
            ClaimManager.addAdminClaim(executor.getChunkPos());
            executor.sendMessage(new LiteralText("Yeah she good boss man"), false);
        } else executor.sendMessage(new LiteralText("Yeah there is already a claim there dumbass"), false);

        return 1;
    }

    public static int removeClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        ClaimManager.dropChunk(executor.getChunkPos());
        executor.sendMessage(new LiteralText("Cya looser"), false);
        return 1;
    }
}