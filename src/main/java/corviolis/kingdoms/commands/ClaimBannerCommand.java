package corviolis.kingdoms.commands;

import corviolis.kingdoms.interfaces.PlayerEntityInf;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class ClaimBannerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("make_banner")
                .executes(ClaimBannerCommand::makeClaimBanner));
    }

    public static int makeClaimBanner(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity executor = context.getSource().getPlayer();
        ItemStack itemStack = executor.getItemsHand().iterator().next();
        String kingdomId = ((PlayerEntityInf) executor).getKingdomId();

        if (itemStack.getItem() instanceof BannerItem && !kingdomId.isEmpty()) {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("IS_CLAIM_MARKER", true);
            itemStack.setNbt(nbt);
            itemStack.setCustomName(new LiteralText(kingdomId.toUpperCase() + " CLAIM MARKER"));

        } else NotificationManager.send(executor.getUuid(), "You must be holding a banner to make a claim banner", NotificationTypes.ERROR);
        return 1;
    }
}


