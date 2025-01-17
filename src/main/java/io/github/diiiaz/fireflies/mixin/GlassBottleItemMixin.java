package io.github.diiiaz.fireflies.mixin;

import io.github.diiiaz.fireflies.block.custom.FireflyLantern;
import io.github.diiiaz.fireflies.item.custom.GlassBottleItemCustomInterface;
import net.minecraft.block.BlockState;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GlassBottleItem.class)
public class GlassBottleItemMixin implements GlassBottleItemCustomInterface {

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // If we are the client, ignore.
        if (context.getWorld().isClient()) {
            return ActionResult.FAIL;
        }

        ServerWorld world = (ServerWorld) context.getWorld();
        ItemStack itemStack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        boolean usedOnFireflyLantern = blockState.getBlock() instanceof FireflyLantern;
        if (!usedOnFireflyLantern) { return ActionResult.FAIL; }

        blockState.onUseWithItem(itemStack, world, context.getPlayer(), context.getHand(), BlockHitResult.createMissed(context.getHitPos(), context.getSide(), blockPos));
        return ActionResult.SUCCESS_SERVER;
    }

}