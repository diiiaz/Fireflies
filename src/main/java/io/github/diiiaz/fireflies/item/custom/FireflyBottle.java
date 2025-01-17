package io.github.diiiaz.fireflies.item.custom;

import io.github.diiiaz.fireflies.block.custom.FireflyLantern;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.sound.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Objects;

public class FireflyBottle extends Item {


    private static final EntityType<?> entityType = ModEntities.FIREFLY;
    public static final int MIN_AMOUNT_OF_FIREFLIES = 1;
    public static final int MAX_AMOUNT_OF_FIREFLIES = 16;

    public FireflyBottle(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.fireflies.firefly_bottle", getFirefliesAmount(stack), MAX_AMOUNT_OF_FIREFLIES).formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }


    public static int getFirefliesAmount(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, 1);
    }

    public static boolean canAddFireflies(ItemStack stack, int amount) {
        return (getFirefliesAmount(stack) + MathHelper.abs(amount)) <= MAX_AMOUNT_OF_FIREFLIES;
    }

    public static boolean canRemoveFireflies(ItemStack stack, int amount) {
        return (getFirefliesAmount(stack) - MathHelper.abs(amount)) >= (MIN_AMOUNT_OF_FIREFLIES - 1);
    }


    public static void playBottleSound(ServerWorld serverWorld, PlayerEntity player, ItemStack itemStack) {
        float pitchAmount = (float) MathHelper.map(getFirefliesAmount(itemStack), MIN_AMOUNT_OF_FIREFLIES, MAX_AMOUNT_OF_FIREFLIES, 0.8, 1.2);
        float randomPitchAmount = (float) MathHelper.map(player.getRandom().nextFloat(), 0.0, 1.0, 0.98, 1.02);
        serverWorld.playSoundFromEntity(null, player, ModSounds.BOTTLE_USED, SoundCategory.PLAYERS, 1.0F, pitchAmount * randomPitchAmount);
    }

    public static void spawnBottleParticles(ServerWorld serverWorld, double x, double y, double z) {
        serverWorld.spawnParticles(
                ParticleTypes.WHITE_SMOKE,
                x, y, z, 3,
                0, 0, 0,
                0.005);
    }


    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        // If we are the client, ignore.
        if (user.getWorld().isClient()) { return super.useOnEntity(stack, user, entity, hand); }
        // If the entity is not a Firefly, ignore.
        if (entity.getType() != ModEntities.FIREFLY) { return super.useOnEntity(stack, user, entity, hand); }
        // If we are using off-hand, ignore.
        if (hand == Hand.OFF_HAND) { return super.useOnEntity(stack, user, entity, hand); }

        boolean changedFirefliesAmount = updateFirefliesAmount(user, stack, hand, +1);
        if (!changedFirefliesAmount) { return ActionResult.FAIL; }

        spawnBottleParticles((ServerWorld) user.getWorld(), entity.getX(), entity.getY(), entity.getZ());
        playBottleSound((ServerWorld) user.getWorld(), user, stack);
        entity.discard();
        return ActionResult.SUCCESS_SERVER;
    }


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // If we are the client, ignore.
        if (context.getWorld().isClient()) { return super.useOnBlock(context); }

        ServerWorld world = (ServerWorld) context.getWorld();
        ItemStack itemStack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockState blockState = world.getBlockState(blockPos);
        BlockPos faceBlockPos = (blockState.getCollisionShape(world, blockPos).isEmpty()) ? blockPos : blockPos.offset(direction);

        boolean usedOnFireflyLantern = blockState.getBlock() instanceof FireflyLantern;
        if (usedOnFireflyLantern) {
            blockState.onUseWithItem(itemStack, world, context.getPlayer(), context.getHand(), BlockHitResult.createMissed(context.getHitPos(), context.getSide(), blockPos));
            return super.useOnBlock(context);
        }

        ActionResult result = useOnDefaultBlock(context, world, itemStack, blockPos, faceBlockPos, direction);
        // Play sound & particles if success
        if (result == ActionResult.SUCCESS_SERVER || result == ActionResult.SUCCESS) {
            playBottleSound(world, Objects.requireNonNull(context.getPlayer()), itemStack);
        }
        return result;
    }

    private ActionResult useOnDefaultBlock(ItemUsageContext context, ServerWorld world, ItemStack itemStack, BlockPos blockPos, BlockPos faceBlockPos, Direction direction) {
        // if we are not sneaking, ignore.
        if (!Objects.requireNonNull(context.getPlayer()).isSneaking()) { return ActionResult.FAIL; }
        boolean changedFirefliesAmount = updateFirefliesAmount(context.getPlayer(), itemStack, Hand.MAIN_HAND, -1);
        if (!changedFirefliesAmount) { return ActionResult.FAIL; }

        // spawn a firefly
        boolean invertY = !Objects.equals(blockPos, faceBlockPos) && direction == Direction.UP;
        boolean didFireflySpawn = entityType.spawnFromItemStack(world, itemStack, context.getPlayer(), blockPos, SpawnReason.SPAWN_ITEM_USE, true, invertY) != null;
        if (didFireflySpawn) {
            spawnBottleParticles(world, blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
        }
        return didFireflySpawn ? ActionResult.SUCCESS_SERVER : ActionResult.FAIL;
    }


    public static boolean updateFirefliesAmount(PlayerEntity player, ItemStack itemStack, Hand hand, int amount) {
        // if we are in creative we don't care.
        if (Objects.requireNonNull(player).isCreative()) {  return true; }

        int newAmount = getFirefliesAmount(itemStack) + amount;

        if (!canAddFireflies(itemStack, amount) && amount > 0) { return false; }
        if (!canRemoveFireflies(itemStack, amount) && amount < 0) { return false; }

        itemStack.set(ModDataComponentTypes.FIREFLIES_AMOUNT, newAmount);

        if (getFirefliesAmount(itemStack) < MIN_AMOUNT_OF_FIREFLIES) {
            // exchange firefly bottle to a glass bottle
            ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, player, Items.GLASS_BOTTLE.getDefaultStack());
            player.setStackInHand(hand, itemStack2);
        }
        return true;
    }
}
