package corviolis.athena.commands;

import arnaria.notifacaitonlib.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.events.Challenge;
import corviolis.athena.services.events.ChallengeManager;
import corviolis.athena.services.procedures.KingdomProcedureChecks;
import corviolis.athena.util.InterfaceTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.http.cookie.CommonCookieAttributeHandler;

import javax.sql.CommonDataSource;
import java.util.ArrayList;

public class ChallengeCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("challenges")
                .then(CommandManager.literal("view")
                        .then(CommandManager.literal("available")
                                .executes(ChallengeCommands::viewChallenges))
                        .then(CommandManager.literal("completed")
                                .executes(ChallengeCommands::viewCompletedChallenges)))
                .then(CommandManager.literal("submit")
                        .then(CommandManager.argument("challenge", StringArgumentType.string()).suggests((context, builder) -> {
                                    PlayerEntity executor = context.getSource().getPlayer();

                                    for (int i = 1; i < ChallengeManager.getMaxTier(); i++) {
                                        for (String challengeId : ChallengeManager.getChallengeIds(i)) {
                                            if (!KingdomsData.getCompletedChallenges(((PlayerEntityInf) executor).getKingdomId()).contains(challengeId) || KingdomsData.getChallengeQue(((PlayerEntityInf) executor).getKingdomId()).contains(challengeId)) {
                                                builder.suggest(challengeId);
                                            }
                                        }
                                    }
                                    return builder.buildFuture();
                                })

                        )));
    }

    private static int viewCompletedChallenges(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        for (String Challenge: KingdomsData.getCompletedChallenges(((PlayerEntityInf) executor).getKingdomId())) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), Challenge, NotificationTypes.INFO);
        }
        return 1;
    }

    private static int viewChallenges(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        int tier = 1;
        while (tier <= ChallengeManager.getMaxTier()) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), String.valueOf(tier), NotificationTypes.INFO);
            boolean completedTier = true;
            for (String challange : ChallengeManager.getChallengeIds(1)) {
                if (!KingdomsData.getCompletedChallenges(((PlayerEntityInf) executor).getKingdomId()).contains(challange) || KingdomsData.getChallengeQue(((PlayerEntityInf) executor).getKingdomId()).contains(challange)) {
                    completedTier = false;
                    KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), challange, NotificationTypes.INFO);
                }
            }
            if (completedTier) {
                KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), "Completed", NotificationTypes.INFO);
            }
            tier++;
        }
        return 1;
    }

    private static int submitChallenge()

}
