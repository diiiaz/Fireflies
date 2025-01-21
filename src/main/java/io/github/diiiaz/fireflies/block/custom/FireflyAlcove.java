package io.github.diiiaz.fireflies.block.custom;

import com.mojang.serialization.MapCodec;
import io.github.diiiaz.fireflies.block.ModProperties;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyAlcoveBlockEntity;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireflyAlcove extends BlockWithEntity {
    
    public static final MapCodec<FireflyAlcove> CODEC = createCodec(FireflyAlcove::new);
    public static final IntProperty FIREFLIES_AMOUNT = ModProperties.FIREFLY_ALCOVE_AMOUNT;

    @Override
    public MapCodec<FireflyAlcove> getCodec() {
        return CODEC;
    }

    public FireflyAlcove(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FIREFLIES_AMOUNT, 0));
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (int) MathHelper.map(getFirefliesAmount(state), ModProperties.FIREFLY_ALCOVE_AMOUNT_MIN, ModProperties.FIREFLY_ALCOVE_AMOUNT_MAX, 0, 15);
    }

    public static int getFirefliesAmount(BlockState state) {
        return state.get(FIREFLIES_AMOUNT);
    }


    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (!world.isClient && blockEntity instanceof FireflyAlcoveBlockEntity fireflyAlcoveBlockEntity) {
            if (!EnchantmentHelper.hasAnyEnchantmentsIn(tool, ModTags.Enchantments.PREVENTS_FIREFLY_SPAWNS_WHEN_MINING)) {
                fireflyAlcoveBlockEntity.tryReleaseFireflies(state, true);
            }
            world.updateComparators(pos, this);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FIREFLIES_AMOUNT);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FireflyAlcoveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntityTypes.FIREFLY_ALCOVE_BLOCK_ENTITY_TYPE, FireflyAlcoveBlockEntity::serverTick);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld
                && player.isCreative()
                && serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)
                && world.getBlockEntity(pos) instanceof FireflyAlcoveBlockEntity FireflyAlcoveBlockEntity) {
            int i = state.get(FIREFLIES_AMOUNT);
            boolean bl = !FireflyAlcoveBlockEntity.hasNoFireflies();
            if (bl || i > 0) {
                ItemStack itemStack = new ItemStack(this);
                itemStack.applyComponentsFrom(FireflyAlcoveBlockEntity.createComponentMap());
                itemStack.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(FIREFLIES_AMOUNT, i));
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        return super.onBreak(world, pos, state, player);
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
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        super.appendTooltip(stack, context, tooltip, options);
        int firefliesAmount = stack.getOrDefault(ModDataComponentTypes.ALCOVE_FIRELIES_AMOUNT, List.of()).size();
        tooltip.add(Text.translatable("container.firefly_alcove.fireflies", firefliesAmount, ModProperties.FIREFLY_ALCOVE_AMOUNT_MAX).formatted(Formatting.GRAY));
    }

}
