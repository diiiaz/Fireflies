package io.github.diiiaz.fireflies.block.custom;

import io.github.diiiaz.fireflies.block.ModProperties;
import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.item.ModItems;
import io.github.diiiaz.fireflies.item.custom.FireflyBottle;
import io.github.diiiaz.fireflies.particle.custom.FireflyParticleEffect;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class FireflyLantern extends LanternBlock {

    public static final IntProperty FIREFLIES_AMOUNT = ModProperties.FIREFLIES_LANTERN_AMOUNT;

    public FireflyLantern(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(HANGING, Boolean.FALSE)
                .with(WATERLOGGED, Boolean.FALSE)
                .with(FIREFLIES_AMOUNT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HANGING, WATERLOGGED, FIREFLIES_AMOUNT);
    }

    public static int getFirefliesAmount(BlockState state) {
        return state.get(FIREFLIES_AMOUNT);
    }

    public static boolean canAddFireflies(BlockState state, int amount) {
        return (getFirefliesAmount(state) + MathHelper.abs(amount)) <= ModProperties.FIREFLIES_LANTERN_AMOUNT_MAX;
    }

    public static boolean canRemoveFireflies(BlockState state, int amount) {
        return (getFirefliesAmount(state) - MathHelper.abs(amount)) >= (ModProperties.FIREFLIES_LANTERN_AMOUNT_MIN);
    }

    public static int getLuminance(BlockState state) {
        return (int) MathHelper.map(getFirefliesAmount(state), ModProperties.FIREFLIES_LANTERN_AMOUNT_MIN, ModProperties.FIREFLIES_LANTERN_AMOUNT_MAX, 0, 15);
    }


    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (getFirefliesAmount(state) < 1) { return; }
        double x = (double)pos.getX() + (double)0.5F + (random.nextDouble() - (double)0.5F) * 0.2;
        double y = (double)pos.getY() + 0.2 + (random.nextDouble() - (double)0.5F) * 0.2;
        double z = (double)pos.getZ() + (double)0.5F + (random.nextDouble() - (double)0.5F) * 0.2;
        world.addParticle(FireflyParticleEffect.createDefault(world.getRandom()), x, y, z, 0.0F, 0.0F, 0.0F);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getWorld().isClient()) { return ActionResult.PASS; }
        if (stack.isOf(ModItems.FIREFLY_BOTTLE)) { return onUseWithFireflyBottle(stack, state, world, pos, player, hand); }
        if (stack.isOf(Items.GLASS_BOTTLE)) { return onUseWithGlassBottle(stack, state, world, pos, player, hand); }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }


    private ActionResult onUseWithGlassBottle(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        // if we are not sneaking, ignore.
        if (!player.isSneaking()) { return ActionResult.FAIL; }
        boolean hasUpdatedAmount = updateFirefliesAmount((ServerWorld) world, pos, state, -1);
        if (!hasUpdatedAmount) { return ActionResult.FAIL; }

        // exchange glass bottle with firefly bottle
        FireflyBottle.playBottleSound((ServerWorld) world, player, stack);
        ItemStack itemStack2 = ItemUsage.exchangeStack(stack, player, ModItems.FIREFLY_BOTTLE.getDefaultStack());
        player.setStackInHand(hand, itemStack2);

        return ActionResult.SUCCESS_SERVER;
    }


    private ActionResult onUseWithFireflyBottle(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        int amountAddedInFireflyLantern = player.isSneaking() ? -1 : +1;

        boolean canChangeFireflyAmountInBottle = amountAddedInFireflyLantern > 0 ? FireflyBottle.canRemoveFireflies(stack, amountAddedInFireflyLantern) : FireflyBottle.canAddFireflies(stack, amountAddedInFireflyLantern);
        boolean canChangeFireflyAmountInLantern = amountAddedInFireflyLantern < 0 ? canRemoveFireflies(state, amountAddedInFireflyLantern) : canAddFireflies(state, amountAddedInFireflyLantern);

        if (!canChangeFireflyAmountInBottle || !canChangeFireflyAmountInLantern) { return ActionResult.FAIL; }

        FireflyBottle.playBottleSound((ServerWorld) world, player, stack);
        FireflyBottle.updateFirefliesAmount(player, stack, hand, -amountAddedInFireflyLantern);
        updateFirefliesAmount((ServerWorld) world, pos, state, amountAddedInFireflyLantern);

        return ActionResult.SUCCESS_SERVER;
    }


    public static boolean updateFirefliesAmount(ServerWorld world, BlockPos pos, BlockState state, int amount) {
        int newAmount = getFirefliesAmount(state) + amount;

        if (!canAddFireflies(state, amount) && amount > 0) { return false; }
        if (!canRemoveFireflies(state, amount) && amount < 0) { return false; }

        // update amount
        world.setBlockState(pos, state.with(FIREFLIES_AMOUNT, newAmount), Block.NOTIFY_LISTENERS);
        return true;
    }


    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (player.isCreative()) {
            return;
        }
        spawnFireflies((ServerWorld) world, getFirefliesAmount(state), pos);
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        spawnFireflies(world, getFirefliesAmount(state), pos);
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    private void spawnFireflies(ServerWorld world, int amount, BlockPos pos) {
        for (int i = 0; i < amount; i++) {
            world.spawnEntity(ModEntities.FIREFLY.spawn(world, pos, SpawnReason.TRIGGERED));
        }
    }

}
