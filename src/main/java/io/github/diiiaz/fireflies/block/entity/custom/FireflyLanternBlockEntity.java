package io.github.diiiaz.fireflies.block.entity.custom;

import com.google.common.collect.Lists;
import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.custom.FireflyLantern;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import io.github.diiiaz.fireflies.entity.custom.FireflyVariant;
import io.github.diiiaz.fireflies.particle.custom.FireflyParticleEffect;
import io.github.diiiaz.fireflies.sound.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class FireflyLanternBlockEntity extends BlockEntity {

    private static final String FIREFLIES_KEY = "fireflies";
    private final List<FireflyData.Firefly> fireflies = Lists.newArrayList();
    private Vector3f averageColors;


    public FireflyLanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FIREFLY_LANTERN_BLOCK_ENTITY_TYPE, pos, state);
    }


    public List<FireflyData.Firefly> getFireflies() {
        return this.fireflies;
    }

    public int getFirefliesCount() {
        return this.fireflies.size();
    }

    public boolean hasFireflies() { return !this.fireflies.isEmpty(); }

    public void addFirefly(NbtComponent fireflyData) { addFirefly(new FireflyData.Firefly(new FireflyData(fireflyData, 0, 200))); }

    public void addFirefly(FireflyData fireflyData) { addFirefly(new FireflyData.Firefly(fireflyData)); }

    public void addFirefly(FireflyData.Firefly firefly) {
        this.fireflies.add(firefly);
        updateState();
    }

    public FireflyData removeFirefly() {
        FireflyData result = this.fireflies.removeLast().createData();
        updateState();
        return result;
    }

    private void updateState() {
        if (this.world != null && !this.world.isClient) {
            this.world.setBlockState(pos, this.world.getBlockState(this.pos).with(FireflyLantern.FIREFLIES_AMOUNT, getFirefliesCount()));
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.pos, GameEvent.Emitter.of(null, this.getCachedState()));
            updateAverageColors();
        }
    }

    public void tryReleaseFireflies(BlockState state) {
        List<Entity> list = Lists.newArrayList();
        this.fireflies.removeIf(firefly -> releaseFirefly(this.world, this.pos, state, firefly.createData(), list));
        if (!list.isEmpty()) {
            super.markDirty();
        }
    }

    private static boolean releaseFirefly(World world, BlockPos pos, BlockState ignoredState, FireflyData firefly, @Nullable List<Entity> entities) {
        Entity entity = firefly.loadEntity(world, pos);

        if (entity == null) {
            return false;
        }

        if (entity instanceof FireflyEntity fireflyEntity) {

            if (entities != null) {
                entities.add(fireflyEntity);
            }

            double x = (double)pos.getX() + 0.5;
            double y = (double)pos.getY() + 0.3;
            double z = (double)pos.getZ() + 0.5;
            entity.refreshPositionAndAngles(x, y, z, entity.getYaw(), entity.getPitch());
        }
        world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(entity, world.getBlockState(pos)));
        return world.spawnEntity(entity);
    }


    // region +------------------------+ NBT +------------------------+

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.fireflies.clear();
        if (nbt.contains(FIREFLIES_KEY)) {
            FireflyData.LIST_CODEC
                    .parse(NbtOps.INSTANCE, nbt.get(FIREFLIES_KEY))
                    .resultOrPartial(string -> Mod.LOGGER.error("Failed to parse fireflies: '{}'", string))
                    .ifPresent(list -> list.forEach(fireflyData -> this.fireflies.add(new FireflyData.Firefly(fireflyData))));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(FIREFLIES_KEY, FireflyData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.createFirefliesData()).getOrThrow());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove(FIREFLIES_KEY);
    }

    private List<FireflyData> createFirefliesData() { return this.fireflies.stream().map(FireflyData.Firefly::createData).toList(); }


    // endregion

    // region +------------------------+ Components +------------------------+

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.fireflies.clear();
        List<FireflyData> list = components.getOrDefault(ModDataComponentTypes.FIREFLIES_AMOUNT, List.of());
        list.forEach(this::addFirefly);
        if (this.world != null && !this.world.isClient()) {
            world.setBlockState(pos, this.world.getBlockState(pos).with(FireflyLantern.FIREFLIES_AMOUNT, ((FireflyLanternBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).getFirefliesCount()));
            updateAverageColors();
        }
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(ModDataComponentTypes.FIREFLIES_AMOUNT, this.createFirefliesData());
    }

    // endregion


    public static void serverTick(World world, BlockPos pos, BlockState ignoredState, FireflyLanternBlockEntity blockEntity) {
        if (blockEntity.hasFireflies()) {
            if (world.getRandom().nextDouble() < 0.005) {
                double x = (double)pos.getX() + 0.5;
                double y = (double)pos.getY() + 0.5;
                double z = (double)pos.getZ() + 0.5;
                world.playSound(null, x, y, z, ModSounds.FIREFLY_AMBIENT, SoundCategory.BLOCKS, 0.4F, MathHelper.map(world.random.nextFloat(), 0F, 1F, 0.9F, 1.1F));
            }

            if (world.getRandom().nextFloat() < 0.2) {
                double x = (double) pos.getX() + (double) 0.5F + (world.getRandom().nextDouble() - (double) 0.5F) * 0.2;
                double y = (double) pos.getY() + 0.2 + (world.getRandom().nextDouble() - (double) 0.5F) * 0.2;
                double z = (double) pos.getZ() + (double) 0.5F + (world.getRandom().nextDouble() - (double) 0.5F) * 0.2;
                ((ServerWorld) world).spawnParticles(blockEntity.createParticleBasedOnFireflies(world.getRandom()), x, y, z, 1, 0.0F, 0.0F, 0.0F, 0.01);
            }
        }
    }

    private FireflyParticleEffect createParticleBasedOnFireflies(Random random) {
        Vector3f randomOffset = new Vector3f(
                MathHelper.map(random.nextFloat(), 0.0F, 1.0F, 0.8F, 1.2F),
                MathHelper.map(random.nextFloat(), 0.0F, 1.0F, 0.8F, 1.2F),
                MathHelper.map(random.nextFloat(), 0.0F, 1.0F, 0.8F, 1.2F)
        );

        if (averageColors == null) {
            updateAverageColors();
        }

        int color = ColorHelper.fromFloats(1.0F,
                Math.clamp(averageColors.x * randomOffset.x, 0.0F, 1.0F),
                Math.clamp(averageColors.y * randomOffset.y, 0.0F, 1.0F),
                Math.clamp(averageColors.z * randomOffset.z, 0.0F, 1.0F)
        );
        return new FireflyParticleEffect(color, 1.0F);
    }



    private void updateAverageColors() {
        List<Vector3f> colors = Lists.newArrayList();
        getFireflies().forEach(firefly -> colors.add(ColorHelper.toVector(FireflyVariant.byId(firefly.getData().getVariant()).getColor())));
        this.averageColors = calculateAverageColor(colors);
    }

    public static Vector3f calculateAverageColor(List<Vector3f> colors) {
        Vector3f sum = new Vector3f(0, 0, 0);
        for (Vector3f color : colors) {
            sum.add(color);
        }
        return sum.div(colors.size());
    }

}
