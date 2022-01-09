package corviolis.kingdoms.commands;

import corviolis.kingdoms.interfaces.PlayerEntityInf;
import corviolis.kingdoms.services.procedures.KingdomProcedureChecks;
import corviolis.kingdoms.util.InterfaceTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;


public class ManageSelfCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("self")
                        .then(CommandManager.literal("join")
                                .then(CommandManager.argument("Kingdom", StringArgumentType.string())
                                        .executes(context -> sendJoinRequest(context, StringArgumentType.getString(context,"Kingdom")))))
                .then(CommandManager.literal("leave")
                        .executes(ManageSelfCommand::leaveKingdom)));
    }

    private static int sendJoinRequest(CommandContext<ServerCommandSource> context, String kingdom) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 1;
        Enum<InterfaceTypes> platform = InterfaceTypes.COMMAND;
        System.out.println(kingdom);
        KingdomProcedureChecks.addJoinRequest(platform, kingdom, player.getUuid());
        return 1;
    }

    private static int leaveKingdom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (executor == null) return 1;
        KingdomProcedureChecks.leaveKingdom(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), executor.getUuid());
        return 1;
    }

}
