package arnaria.kingdoms.commands;

import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ClaimBannerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("make_banner")
                .executes(ClaimBannerCommand::makeClaimBanner));

    }

    public static int makeClaimBanner(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        if (!executor.getItemsHand().equals(BannerItem.BLOCK_ITEMS)) {
            NotificationManager.send(executor.getUuid(), "You must be holding a banner to make a claim banner", NotificationTypes.ERROR);
            return 1;
        }
        NbtCompound nbtData = executor.getInventory().main.get(executor.getInventory().selectedSlot).getNbt();
        if (nbtData != null) {
            nbtData.putBoolean("IsClaimMarker", true);
        }
        else NotificationManager.send(executor.getUuid(), "You must be holding a banner to make a claim banner", NotificationTypes.ERROR);
        return 1;
    }
}


