package io.github.diiiaz.fireflies.block.entity.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import io.github.diiiaz.fireflies.utils.ModTags;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;


public record FireflyData(NbtComponent entityData, int ticksInHome, int minTicksInHome) {
    public static final Codec<FireflyData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            NbtComponent.CODEC.optionalFieldOf("entity_data", NbtComponent.DEFAULT).forGetter(FireflyData::entityData),
                            Codec.INT.fieldOf("ticks_in_home").forGetter(FireflyData::ticksInHome),
                            Codec.INT.fieldOf("min_ticks_home").forGetter(FireflyData::minTicksInHome)
                    )
                    .apply(instance, FireflyData::new)
    );


    public static final Codec<List<FireflyData>> LIST_CODEC = CODEC.listOf();
    @SuppressWarnings("deprecation")
    public static final PacketCodec<ByteBuf, FireflyData> PACKET_CODEC = PacketCodec.tuple(
            NbtComponent.PACKET_CODEC, FireflyData::entityData,
            PacketCodecs.VAR_INT, FireflyData::ticksInHome,
            PacketCodecs.VAR_INT, FireflyData::minTicksInHome,
            FireflyData::new
    );


    public static FireflyData of(Entity entity) {
        NbtCompound nbtCompound = new NbtCompound();
        entity.saveNbt(nbtCompound);
        IRRELEVANT_NBT_KEYS.forEach(nbtCompound::remove);
        return new FireflyData(NbtComponent.of(nbtCompound), 0, 200);
    }

    public static FireflyData create(int ticksInHome, int variant, float lightFrequency) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("id", Registries.ENTITY_TYPE.getId(ModEntities.FIREFLY).toString());
        nbtCompound.putInt(FireflyEntity.VARIANT_KEY, variant);
        nbtCompound.putFloat(FireflyEntity.LIGHT_FREQUENCY_OFFSET_KEY, lightFrequency);
        return new FireflyData(NbtComponent.of(nbtCompound), ticksInHome, 200);
    }


    public int getVariant() {
        DataResult<Integer> dataResult = entityData().get(Codec.INT.fieldOf("Variant"));
        if (dataResult.isError() || dataResult.result().isEmpty()) { return -1; }
        return dataResult.result().get();
    }

    @Nullable
    public Entity loadEntity(World world, BlockPos pos) {
        NbtCompound nbtCompound = this.entityData.copyNbt();
        IRRELEVANT_NBT_KEYS.forEach(nbtCompound::remove);
        Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, world, SpawnReason.LOAD, _entity -> _entity);
        if (entity != null && entity.getType().isIn(ModTags.EntityTypes.LUMINESCENT_SOIL_INHABITORS)) {
            entity.setNoGravity(true);
            if (entity instanceof FireflyEntity fireflyEntity) {
                fireflyEntity.setHomePos(pos);
            }
            return entity;
        } else {
            return null;
        }
    }


    public static class Firefly {
        private final FireflyData data;
        private int ticksInHome;

        Firefly(FireflyData data) {
            this.data = data;
            this.ticksInHome = data.ticksInHome();
        }

        public FireflyData getData() {
            return data;
        }

        public boolean canExitHome() {
            return this.ticksInHome++ > this.data.minTicksInHome;
        }

        public FireflyData createData() {
            return new FireflyData(this.data.entityData, this.ticksInHome, this.data.minTicksInHome);
        }
    }


    static final List<String> IRRELEVANT_NBT_KEYS = Arrays.asList(
            "Air",
            "ArmorDropChances",
            "ArmorItems",
            "Brain",
            "CanPickUpLoot",
            "DeathTime",
            "FallDistance",
            "FallFlying",
            "Fire",
            "HandDropChances",
            "HandItems",
            "HurtByTimestamp",
            "HurtTime",
            "LeftHanded",
            "Motion",
            "NoGravity",
            "OnGround",
            "PortalCooldown",
            "Pos",
            "Rotation",
            "SleepingX",
            "SleepingY",
            "SleepingZ",
            "CannotEnterLuminescentSoilTicks",
            "Passengers",
            "leash",
            "UUID"
    );

}


