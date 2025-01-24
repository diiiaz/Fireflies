package io.github.diiiaz.fireflies.block.custom;

import com.mojang.serialization.MapCodec;
import io.github.diiiaz.fireflies.block.ModProperties;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyLanternBlockEntity;
import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.item.ModItems;
import io.github.diiiaz.fireflies.item.custom.CatchingNet;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class FireflyLantern extends BlockWithEntity implements Waterloggable {

    public static final MapCodec<FireflyLantern> CODEC = createCodec(FireflyLantern::new);
    public static final IntProperty FIREFLIES_AMOUNT = ModProperties.FIREFLIES_LANTERN_AMOUNT;
    public static final BooleanProperty HANGING = Properties.HANGING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected static final VoxelShape STANDING_SHAPE = VoxelShapes.union(Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 7.0, 11.0), Block.createCuboidShape(6.0, 7.0, 6.0, 10.0, 9.0, 10.0));
    protected static final VoxelShape HANGING_SHAPE = VoxelShapes.union(Block.createCuboidShape(5.0, 1.0, 5.0, 11.0, 8.0, 11.0), Block.createCuboidShape(6.0, 8.0, 6.0, 10.0, 10.0, 10.0));

    public FireflyLantern(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(HANGING, Boolean.FALSE).with(WATERLOGGED, Boolean.FALSE).with(FIREFLIES_AMOUNT, 0));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntityTypes.FIREFLY_LANTERN_BLOCK_ENTITY_TYPE, FireflyLanternBlockEntity::serverTick);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FireflyLanternBlockEntity(pos, state);
    }

    @Override
    public MapCodec<FireflyLantern> getCodec() {
        return CODEC;
    }


    // region +------------------------+ Lantern Default +------------------------+

    public static int getLuminance(BlockState state) {
        return switch (getFirefliesAmount(state)) {
            case 1 -> 5;
            case 2 -> 10;
            case 3 -> 15;
            default -> 0;
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HANGING, WATERLOGGED, FIREFLIES_AMOUNT);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = attachedDirection(state).getOpposite();
        return Block.sideCoversSmallSquare(world, pos.offset(direction), direction.getOpposite());
    }

    protected static Direction attachedDirection(BlockState state) {
        return state.get(HANGING) ? Direction.DOWN : Direction.UP;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return attachedDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }


    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        for (Direction direction : ctx.getPlacementDirections()) {
            if (direction.getAxis() == Direction.Axis.Y) {
                BlockState blockState = this.getDefaultState().with(HANGING, direction == Direction.UP);
                if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                    return blockState.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
                }
            }
        }
        return null;
    }

    // endregion

    // region +------------------------+ Use with Item +------------------------+

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getWorld().isClient()) { return super.onUseWithItem(stack, state, world, pos, player, hand, hit); }
        if (stack.isOf(ModItems.CATCHING_NET)) { return onUseWithCatchingNet(stack, state,(ServerWorld) world, pos, player, hand); }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    private ActionResult onUseWithCatchingNet(ItemStack stack, BlockState state, ServerWorld world, BlockPos pos, PlayerEntity player, Hand ignoredHand) {
        if (world.getBlockEntity(pos) instanceof FireflyLanternBlockEntity fireflyLanternBlockEntity) {

            // if we are sneaking we remove firefly from lantern
            if (player.isSneaking() && CatchingNet.canAddEntity(stack) && canRemoveFirefly(state)) {
                CatchingNet.addEntity(player, stack, fireflyLanternBlockEntity.removeFirefly().entityData());
            }

            // if we are not sneaking we add a firefly to the lantern
            else if (!player.isSneaking() && CatchingNet.canRemoveCaughtEntity(stack) && canAddFireflies(state)) {
                CatchingNet.CaughtEntityData caughtEntityData = CatchingNet.removeLastEntity(player, stack, Identifier.of("fireflies:firefly"));
                if (caughtEntityData == null) {
                    return ActionResult.FAIL;
                }
                fireflyLanternBlockEntity.addFirefly(caughtEntityData.entityData());
            }

            else {
                return ActionResult.FAIL;
            }

            CatchingNet.playCatchSound(world, player, stack);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.FAIL;
    }

    // endregion

    // region +------------------------+ Fireflies Manager +------------------------+

    public static int getFirefliesAmount(BlockState state) {
        return state.get(FIREFLIES_AMOUNT);
    }

    public static boolean canAddFireflies(BlockState state) {
        return (getFirefliesAmount(state) + 1) <= ModProperties.FIREFLIES_LANTERN_AMOUNT_MAX;
    }

    public static boolean canRemoveFirefly(BlockState state) {
        return (getFirefliesAmount(state) - 1) >= (ModProperties.FIREFLIES_LANTERN_AMOUNT_MIN);
    }

    private void spawnFireflies(ServerWorld world, int amount, BlockPos pos) {
        for (int i = 0; i < amount; i++) {
            world.spawnEntity(ModEntities.FIREFLY.spawn(world, pos, SpawnReason.TRIGGERED));
        }
    }

    // endregion

    // region +------------------------+ Block Break +------------------------+

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (player.isCreative()) { return; }
        spawnFireflies((ServerWorld) world, getFirefliesAmount(state), pos);
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        spawnFireflies(world, getFirefliesAmount(state), pos);
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    // endregion


}
