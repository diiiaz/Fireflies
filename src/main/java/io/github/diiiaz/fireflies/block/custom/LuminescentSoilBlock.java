package io.github.diiiaz.fireflies.block.custom;

import com.mojang.serialization.MapCodec;
import io.github.diiiaz.fireflies.block.ModProperties;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.block.entity.custom.LuminescentSoilBlockEntity;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.particle.custom.FireflyParticleEffect;
import io.github.diiiaz.fireflies.utils.ModTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class LuminescentSoilBlock extends BlockWithEntity {
    
    public static final MapCodec<LuminescentSoilBlock> CODEC = createCodec(LuminescentSoilBlock::new);
    public static final IntProperty FIREFLIES_AMOUNT = ModProperties.LUMINESCENT_SOIL_FIREFLIES_AMOUNT;

    public LuminescentSoilBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FIREFLIES_AMOUNT, 0));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntityTypes.LUMINESCENT_SOIL_BLOCK_ENTITY_TYPE, LuminescentSoilBlockEntity::serverTick);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LuminescentSoilBlockEntity(pos, state);
    }

    @Override
    public MapCodec<LuminescentSoilBlock> getCodec() {
        return CODEC;
    }




    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FIREFLIES_AMOUNT);
    }





    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (int) MathHelper.map(getFirefliesAmount(state), ModProperties.LUMINESCENT_SOIL_AMOUNT_MIN, ModProperties.LUMINESCENT_SOIL_AMOUNT_MAX, 0, 15);
    }

    public static int getFirefliesAmount(BlockState state) {
        return state.get(FIREFLIES_AMOUNT);
    }


    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (!world.isClient && blockEntity instanceof LuminescentSoilBlockEntity luminescentSoilBlockEntity) {
            if (!EnchantmentHelper.hasAnyEnchantmentsIn(tool, ModTags.Enchantments.PREVENTS_FIREFLY_SPAWNS_WHEN_MINING)) {
                luminescentSoilBlockEntity.tryReleaseFireflies(state, true);
            }
            world.updateComparators(pos, this);
        }
    }


    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld
                && player.isCreative()
                && serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)
                && world.getBlockEntity(pos) instanceof LuminescentSoilBlockEntity LuminescentSoilBlockEntity) {
            int i = state.get(FIREFLIES_AMOUNT);
            boolean bl = LuminescentSoilBlockEntity.hasFireflies();
            if (bl || i > 0) {
                ItemStack itemStack = new ItemStack(this);
                itemStack.applyComponentsFrom(LuminescentSoilBlockEntity.createComponentMap());
                itemStack.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(FIREFLIES_AMOUNT, i));
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        ((LuminescentSoilBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).tryReleaseFireflies(state, true);
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        Entity entity = builder.getOptional(LootContextParameters.THIS_ENTITY);
        if (entity instanceof TntEntity
                || entity instanceof CreeperEntity
                || entity instanceof WitherSkullEntity
                || entity instanceof WitherEntity
                || entity instanceof TntMinecartEntity) {
            builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        }

        return super.getDroppedStacks(state, builder);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack itemStack = super.getPickStack(world, pos, state, includeData);
        if (includeData) {
            itemStack.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(FIREFLIES_AMOUNT, state.get(FIREFLIES_AMOUNT)));
        }

        return itemStack;
    }


    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (getFirefliesAmount(state) < 1) { return; }
        if (random.nextInt(20) == 0) {
            Direction direction = Direction.UP;
            BlockPos blockPos = pos.offset(direction);
            BlockState blockState = world.getBlockState(blockPos);
            if (!state.isOpaque() || !blockState.isSideSolidFullSquare(world, blockPos, direction.getOpposite())) {
                double x = direction.getOffsetX() == 0 ? random.nextDouble() : 0.5 + (double)direction.getOffsetX() * 0.6;
                double y = direction.getOffsetY() == 0 ? random.nextDouble() : 0.5 + (double)direction.getOffsetY() * 0.6;
                double z = direction.getOffsetZ() == 0 ? random.nextDouble() : 0.5 + (double)direction.getOffsetZ() * 0.6;
                world.addParticle(FireflyParticleEffect.createDefault(world.getRandom()), (double)pos.getX() + x, (double)pos.getY() + y, (double)pos.getZ() + z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        super.appendTooltip(stack, context, tooltip, options);
        int firefliesAmount = stack.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, List.of()).size();
        tooltip.add(Text.translatable("container.fireflies", firefliesAmount, ModProperties.LUMINESCENT_SOIL_AMOUNT_MAX).formatted(Formatting.GRAY));
    }

}
