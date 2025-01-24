package io.github.diiiaz.fireflies.block.entity.custom;

import com.google.common.collect.Lists;
import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.ModProperties;
import io.github.diiiaz.fireflies.block.custom.LuminescentSoilBlock;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import io.github.diiiaz.fireflies.sound.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class LuminescentSoilBlockEntity extends BlockEntity {

    private static final String FIREFLIES_KEY = "fireflies";
    private static final int MIN_TIME_BEFORE_RELEASING_FIREFLY = 20;
    private int timeSinceReleasingFirefly = 0;
    private final List<FireflyData.Firefly> fireflies = Lists.newArrayList();


    public LuminescentSoilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LUMINESCENT_SOIL_BLOCK_ENTITY_TYPE, pos, state);
    }

    public boolean isNearFire() {
        if (this.world != null) {
            for (BlockPos blockPos : BlockPos.iterate(this.pos.add(-1, -1, -1), this.pos.add(1, 1, 1))) {
                if (this.world.getBlockState(blockPos).getBlock() instanceof FireBlock) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean hasFireflies() {
        return !this.fireflies.isEmpty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFullOfFireflies() {
        return this.fireflies.size() >= ModProperties.LUMINESCENT_SOIL_AMOUNT_MAX;
    }



    public void tryReleaseFireflies(BlockState state, boolean forceRelease) {
        List<Entity> list = Lists.newArrayList();
        this.fireflies.removeIf(firefly -> releaseFirefly(this.world, this.pos, state, firefly.createData(), list, forceRelease));
        if (!list.isEmpty()) {
            super.markDirty();
        }
    }


    public int getFirefliesCount() {
        return this.fireflies.size();
    }

    public void tryEnterHome(FireflyEntity entity) {
        if (this.fireflies.size() < ModProperties.LUMINESCENT_SOIL_AMOUNT_MAX) {
            entity.stopRiding();
            entity.removeAllPassengers();
            entity.detachLeash();
            this.addFirefly(FireflyData.of(entity));
            if (this.world != null) {
                BlockPos blockPos = this.getPos();
                this.world.setBlockState(pos, this.world.getBlockState(blockPos).with(LuminescentSoilBlock.FIREFLIES_AMOUNT, getFirefliesCount()));
                this.world.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
                this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(entity, this.getCachedState()));
            }

            entity.remove(Entity.RemovalReason.DISCARDED);
            super.markDirty();
        }
    }

    public void addFirefly(FireflyData firefly) {
        this.fireflies.add(new FireflyData.Firefly(firefly));
    }


    private static boolean releaseFirefly(World world, BlockPos pos, BlockState ignoredState, FireflyData firefly, @Nullable List<Entity> entities, boolean forceRelease) {
        if (!FireflyEntity.isNight(world) && !forceRelease) {
            return false;
        }

        if (world.getBlockEntity(pos) != null && !forceRelease) {
            //noinspection DataFlowIssue
            if (((LuminescentSoilBlockEntity) world.getBlockEntity(pos)).timeSinceReleasingFirefly < MIN_TIME_BEFORE_RELEASING_FIREFLY) {
                return false;
            }
        }

        Direction direction = Direction.UP;
        BlockPos blockPos = pos.offset(direction);
        boolean blockedByCollision = !world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty();

        if (blockedByCollision && !forceRelease) {
            return false;
        }

        Entity entity = firefly.loadEntity(world, pos);

        if (entity == null) {
            return false;
        }

        if (entity instanceof FireflyEntity fireflyEntity) {

            if (entities != null) {
                entities.add(fireflyEntity);
            }

            float f = entity.getWidth();
            double d = 0.55 + (double)(f / 2.0F);
            double x = (double)pos.getX() + 0.5 + d * (double)direction.getOffsetX();
            double y = (double)pos.getY() + 0.5 + d * (double)direction.getOffsetY();
            double z = (double)pos.getZ() + 0.5 + d * (double)direction.getOffsetZ();
            entity.refreshPositionAndAngles(x, y, z, entity.getYaw(), entity.getPitch());
        }
        world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(entity, world.getBlockState(pos)));
        if (world.getBlockEntity(pos) != null) {
            //noinspection DataFlowIssue
            ((LuminescentSoilBlockEntity) world.getBlockEntity(pos)).timeSinceReleasingFirefly = 0;
        }
        return world.spawnEntity(entity);
    }

    private static void tickFireflies(World world, BlockPos pos, BlockState state, List<FireflyData.Firefly> fireflies) {
        boolean bl = false;
        Iterator<FireflyData.Firefly> iterator = fireflies.iterator();

        while (iterator.hasNext()) {
            FireflyData.Firefly firefly = iterator.next();
            if (firefly.canExitHome()) {
                if (releaseFirefly(world, pos, state, firefly.createData(), null, false)) {
                    bl = true;
                    iterator.remove();
                    world.setBlockState(pos, state.with(LuminescentSoilBlock.FIREFLIES_AMOUNT, ((LuminescentSoilBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).getFirefliesCount()));
                }
            }
        }

        if (bl) {
            markDirty(world, pos, state);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, LuminescentSoilBlockEntity blockEntity) {
        tickFireflies(world, pos, state, blockEntity.fireflies);
        if (blockEntity.hasFireflies()) {
            blockEntity.timeSinceReleasingFirefly += world.getRandom().nextBetween(1, 15);
            if (world.getRandom().nextDouble() < 0.005) {
                double x = (double)pos.getX() + 0.5;
                double y = (double)pos.getY() + 0.5;
                double z = (double)pos.getZ() + 0.5;
                world.playSound(null, x, y, z, ModSounds.FIREFLY_AMBIENT, SoundCategory.BLOCKS, 0.4F, MathHelper.map(world.random.nextFloat(), 0F, 1F, 0.9F, 1.1F));
            }
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.fireflies.clear();
        if (nbt.contains(FIREFLIES_KEY)) {
            FireflyData.LIST_CODEC
                    .parse(NbtOps.INSTANCE, nbt.get(FIREFLIES_KEY))
                    .resultOrPartial(string -> Mod.LOGGER.error("Failed to parse fireflies: '{}'", string))
                    .ifPresent(list -> list.forEach(this::addFirefly));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(FIREFLIES_KEY, FireflyData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.createFirefliesData()).getOrThrow());
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        this.fireflies.clear();
        List<FireflyData> list = components.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, List.of());
        list.forEach(this::addFirefly);
        if (this.world != null && !this.world.isClient()) {
            world.setBlockState(pos, this.world.getBlockState(pos).with(LuminescentSoilBlock.FIREFLIES_AMOUNT, ((LuminescentSoilBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).getFirefliesCount()));
        }
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(ModDataComponentTypes.FIREFLIES_AMOUNT, this.createFirefliesData());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove(FIREFLIES_KEY);
    }

    private List<FireflyData> createFirefliesData() {
        return this.fireflies.stream().map(FireflyData.Firefly::createData).toList();
    }
}
