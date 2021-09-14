package arnaria.kingdoms.commands;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
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
        if (kingdom == null) {
            NotificationManager.send(executor.getUuid(), "You are not in a Kingdom", NotificationTypes.ERROR);
            return 1;

        }
        if (executor.getUuid() != KingdomsData.getKing(kingdom)) {
            NotificationManager.send(executor.getUuid(), "Only the Leader can run this command", NotificationTypes.ERROR);
            return 1;
        }

        KingdomProcedures.disbandKingdom(kingdom);
        NotificationManager.send(executor.getUuid(), kingdom + " has been disbanded", NotificationTypes.EVENT);
        return 1;
    }
}
