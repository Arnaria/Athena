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
                                    String kingdomId = ((PlayerEntityInf) executor).getKingdomId();

                                    if (!kingdomId.isEmpty()) {
                                        for (int i = 0; i < ChallengeManager.getMaxTier(); i++) {
                                            for (String challengeId : ChallengeManager.getChallengeIds(i +  1)) {
                                                if (!KingdomsData.getCompletedChallenges(kingdomId).contains(challengeId) && !KingdomsData.getChallengeQue(kingdomId).contains(challengeId)) {
                                                    builder.suggest(challengeId);
                                                }
                                            }
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> submitChallenge(context, StringArgumentType.getString(context, "challenge")))
                        )
                )
                .then(CommandManager.literal("approve").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(CommandManager.argument("team", StringArgumentType.string()).suggests((context, builder) -> {
                                    for (String kingdomId : KingdomsData.getKingdomIds()) builder.suggest(kingdomId);
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("challenge", StringArgumentType.string()).suggests((context, builder) -> {
                                            String kingdomId = StringArgumentType.getString(context, "team");
                                            if (KingdomsData.getKingdomIds().contains(kingdomId)) {
                                                for (String challenge : KingdomsData.getChallengeQue(kingdomId)) builder.suggest(challenge);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> approveChallenge(context, StringArgumentType.getString(context, "team"), StringArgumentType.getString(context, "challenge"))))))

                .then(CommandManager.literal("decline").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(CommandManager.argument("team", StringArgumentType.string()).suggests((context, builder) -> {
                                    for (String kingdomId : KingdomsData.getKingdomIds()) builder.suggest(kingdomId);
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("challenge", StringArgumentType.string()).suggests((context, builder) -> {
                                            String kingdomId = StringArgumentType.getString(context, "team");
                                            if (KingdomsData.getKingdomIds().contains(kingdomId)) {
                                                for (String challenge : KingdomsData.getChallengeQue(kingdomId)) builder.suggest(challenge);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> declineChallenge(context, StringArgumentType.getString(context, "team"), StringArgumentType.getString(context, "challenge")))))));
    }

    private static int viewCompletedChallenges(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (((PlayerEntityInf) executor).getKingdomId().isEmpty()) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), "You are not part of a team", NotificationTypes.WARN);
            return 1;
        }
        for (String Challenge: KingdomsData.getCompletedChallenges(((PlayerEntityInf) executor).getKingdomId())) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), Challenge, NotificationTypes.INFO);
        }
        return 1;
    }

    private static int viewChallenges(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (((PlayerEntityInf) executor).getKingdomId().isEmpty()) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), "You are not part of a team", NotificationTypes.WARN);
            return 1;
        }
        int tier = 1;
        while (tier <= ChallengeManager.getMaxTier()) {
            KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), "Tier " + tier, NotificationTypes.INFO);
            boolean completedTier = true;
            for (String challenge : ChallengeManager.getChallengeIds(tier)) {
                if (!KingdomsData.getCompletedChallenges(((PlayerEntityInf) executor).getKingdomId()).contains(challenge) || !KingdomsData.getChallengeQue(((PlayerEntityInf) executor).getKingdomId()).contains(challenge)) {
                    completedTier = false;
                    Challenge challengeData = ChallengeManager.getChallenge(challenge);
                    if (challengeData != null) {
                        // String message = challengeData.challengeId() + ", " + challengeData.description() + ", xp:" + challengeData.xp();
                        KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), challengeData.challengeId() +":", NotificationTypes.EVENT);
                        KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), challengeData.description(), NotificationTypes.INFO);
                        KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), "Reward, XP:" + challengeData.xp(), NotificationTypes.ACHIEVEMENT);
                    }
                }
            }
            if (completedTier) {
                KingdomProcedureChecks.sendNotification(InterfaceTypes.COMMAND, executor.getUuid(), "Completed", NotificationTypes.INFO);
            }
            tier++;
        }
        return 1;
    }

    private static int submitChallenge(CommandContext<ServerCommandSource> context, String challenge) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        KingdomProcedureChecks.submitChallenge(InterfaceTypes.COMMAND, ((PlayerEntityInf) executor).getKingdomId(), executor.getUuid(), challenge);
        return 1;
    }

    private static int approveChallenge(CommandContext<ServerCommandSource> context, String kingdomID, String challenge) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        KingdomProcedureChecks.acceptChallenge(InterfaceTypes.COMMAND, kingdomID, executor.getUuid(), challenge);
        return 1;
    }

    private static int declineChallenge(CommandContext<ServerCommandSource> context, String kingdomID, String challenge) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        KingdomProcedureChecks.declineChallenge(InterfaceTypes.COMMAND, kingdomID, executor.getUuid(), challenge);
        return 1;
    }

}
