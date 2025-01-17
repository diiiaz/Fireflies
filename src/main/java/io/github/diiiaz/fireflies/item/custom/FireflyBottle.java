package io.github.diiiaz.fireflies.item.custom;

import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.sound.ModSounds;
import io.github.diiiaz.fireflies.utils.ModChat;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Objects;

public class FireflyBottle extends Item {


    private static final EntityType<?> entityType = ModEntities.FIREFLY;
    private static final int MIN_AMOUNT_OF_FIREFLIES = 1;
    private static final int MAX_AMOUNT_OF_FIREFLIES = 16;

    public FireflyBottle(Settings settings) {
        super(settings);
    }


    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        // If the entity is not a Firefly.
        if (entity.getType() != ModEntities.FIREFLY) {
            return super.useOnEntity(stack, user, entity, hand);
        }
        int newAmount = stack.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, 1) + 1;
        // If we are not the Client.
        if (!user.getWorld().isClient()) {
            // Add firefly in bottle
            if (newAmount > MAX_AMOUNT_OF_FIREFLIES) {
                return ActionResult.FAIL;
            }
            spawnBottleParticles((ServerWorld) user.getWorld(), entity.getX(), entity.getY(), entity.getZ());
            stack.set(ModDataComponentTypes.FIREFLIES_AMOUNT, newAmount);
            entity.discard();
        }
        if (user.getWorld().isClient()) {
            if (newAmount > MAX_AMOUNT_OF_FIREFLIES) {
                return ActionResult.FAIL;
            }
            playBottleSound(user, stack);
        }
        return ActionResult.SUCCESS;
    }


    public static void playBottleSound(PlayerEntity player, ItemStack itemStack) {
        float pitchAmount = (float) MathHelper.map(itemStack.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, 1), MIN_AMOUNT_OF_FIREFLIES, MAX_AMOUNT_OF_FIREFLIES, 0.8, 1.2);
        float randomPitchAmount = (float) MathHelper.map(player.getRandom().nextFloat(), 0.0, 1.0, 0.98, 1.02);
        player.playSound(ModSounds.BOTTLE_USED, 1.0F, pitchAmount * randomPitchAmount);
    }

    public static void spawnBottleParticles(ServerWorld serverWorld, double x, double y, double z) {
        serverWorld.spawnParticles(
            ParticleTypes.WHITE_SMOKE,
            x, y, z,
            3,
            0, 0, 0,
            0.005);
    }


    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.fireflies.firefly_bottle", stack.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, 1), MAX_AMOUNT_OF_FIREFLIES).formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        ItemStack itemStack = context.getStack();
        if (!world.isClient()) {
            BlockPos blockPos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos2;
            if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
                blockPos2 = blockPos;
            } else {
                blockPos2 = blockPos.offset(direction);
            }

            // if we are not in creative
            if (!context.getPlayer().isCreative()) {
                int newAmount = itemStack.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, 1) - 1;
                // we still have fireflies in bottle
                if (newAmount >= MIN_AMOUNT_OF_FIREFLIES) {
                    itemStack.set(ModDataComponentTypes.FIREFLIES_AMOUNT, newAmount);
                } else {
                    ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, context.getPlayer(), Items.GLASS_BOTTLE.getDefaultStack());
                    context.getPlayer().setStackInHand(context.getHand(), itemStack2);
                }
            }
            spawnBottleParticles((ServerWorld) world, blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
            // Spawn Firefly
            boolean didSpawnFireflyFromBottle = entityType.spawnFromItemStack((ServerWorld) world, itemStack, context.getPlayer(), blockPos, SpawnReason.SPAWN_ITEM_USE, true, !Objects.equals(blockPos, blockPos2) && direction == Direction.UP) != null;
            if (!didSpawnFireflyFromBottle) {
                return ActionResult.FAIL;
            }
            world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        } else {
            playBottleSound(context.getPlayer(), itemStack);
        }
        return ActionResult.SUCCESS;
    }
}
