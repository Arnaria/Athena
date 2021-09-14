package arnaria.kingdoms.commands;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.InterfaceTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DisbandKingdomCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("disband")
                .executes(DisbandKingdomCommand::disbandKingdom));
    }

    private static int disbandKingdom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        String kingdom = ((PlayerEntityInf) executor).getKingdomId();
        KingdomProcedures.disbandKingdom(platform, kingdom, executor.getUuid());
        return 1;
    }
}
