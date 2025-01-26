package io.github.diiiaz.fireflies.item.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.sound.ModSounds;
import io.github.diiiaz.fireflies.utils.ModTags;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CatchingNet extends Item {

    private static final int FULL_ITEM_BAR_COLOR = ColorHelper.Argb.fromFloats(1.0F, 1.0F, 0.33F, 0.33F);
    private static final int ITEM_BAR_COLOR = ColorHelper.Argb.fromFloats(1.0F, 0.44F, 0.53F, 1.0F);
    public static final int MAX_CAUGHT_AMOUNT = 16;

    public CatchingNet(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        List<CaughtEntityData> caughtEntities = new ArrayList<>(stack.getOrDefault(ModDataComponentTypes.CAUGHT_ENTITIES, List.of()));

        if (caughtEntities.isEmpty()) {
            tooltip.add(Text.translatable("tooltip.fireflies.catching_net.empty.description").formatted(Formatting.GRAY));
            return;
        }

        HashMap<Identifier, Integer> caughtEntitiesHash = new HashMap<>();

        caughtEntities.forEach(caughtEntityData -> {
            if (caughtEntitiesHash.containsKey(caughtEntityData.entityData.get())) {
                caughtEntitiesHash.replace(caughtEntityData.entityData.getId(), caughtEntitiesHash.get(caughtEntityData.entityData.getId()) + 1);
            } else {
                caughtEntitiesHash.put(caughtEntityData.entityData.getId(), 1);
            }
        });

        caughtEntitiesHash.forEach((identifier, integer) -> tooltip.add(
                Text.translatable("tooltip.fireflies.catching_net",
                        Text.translatable(identifier.toTranslationKey("entity")), integer)
                        .formatted(Formatting.GRAY)));
    }



    @Override
    public int getItemBarColor(ItemStack stack) {
        return getCaughtAmount(stack) < MAX_CAUGHT_AMOUNT ? ITEM_BAR_COLOR : FULL_ITEM_BAR_COLOR;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(MathHelper.clampedMap(getCaughtAmount(stack), 0, MAX_CAUGHT_AMOUNT, 0, 13));
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getCaughtAmount(stack) > 0;
    }


    public static int getCaughtAmount(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.CAUGHT_ENTITIES, List.of()).size();
    }

    public static boolean canAddEntity(ItemStack stack) {
        return (getCaughtAmount(stack) + 1) <= MAX_CAUGHT_AMOUNT;
    }

    public static boolean canRemoveCaughtEntity(ItemStack stack) {
        return (getCaughtAmount(stack) - 1) >= 0;
    }


    public static void playCatchSound(ServerWorld serverWorld, PlayerEntity player, ItemStack itemStack) {
        float pitchAmount = (float) MathHelper.map(getCaughtAmount(itemStack), 0, MAX_CAUGHT_AMOUNT, 0.8, 1.2);
        float randomPitchAmount = (float) MathHelper.map(player.getRandom().nextFloat(), 0.0, 1.0, 0.98, 1.02);
        serverWorld.playSoundFromEntity(null, player, ModSounds.CATCHING_NET_USED, SoundCategory.PLAYERS, 1.0F, pitchAmount * randomPitchAmount);
    }

    public static void spawnCatchParticles(ServerWorld serverWorld, double x, double y, double z) {
        serverWorld.spawnParticles(
                ParticleTypes.CRIT,
                x, y, z, 3,
                0.1, 0.1, 0.1,
                0.15);
    }


    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        // If we are the client, ignore.
        if (user.getWorld().isClient()) { return super.useOnEntity(stack, user, entity, hand); }
        // If the entity is not catchable.
        if (!entity.getType().isIn(ModTags.EntityTypes.NET_CATCHABLE)) { return super.useOnEntity(stack, user, entity, hand); }
        // If we are using off-hand, ignore.
        if (hand == Hand.OFF_HAND) { return super.useOnEntity(stack, user, entity, hand); }

        boolean addedEntity = addEntity(user, stack, entity);
        if (!addedEntity) { return ActionResult.FAIL; }

        spawnCatchParticles((ServerWorld) user.getWorld(), entity.getX(), entity.getY(), entity.getZ());
        playCatchSound((ServerWorld) user.getWorld(), user, stack);
        entity.remove(Entity.RemovalReason.DISCARDED);
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

        ActionResult result = blockState.onUseWithItem(itemStack, world, context.getPlayer(), context.getHand(), BlockHitResult.createMissed(context.getHitPos(), context.getSide(), blockPos));
        if (result == ActionResult.SUCCESS || result == ActionResult.SUCCESS_SERVER) {
            return result;
        }

        result = useOnDefaultBlock(context, world, itemStack, blockPos, faceBlockPos, direction);
        // Play sound & particles if success
        if (result == ActionResult.SUCCESS_SERVER || result == ActionResult.SUCCESS) {
            playCatchSound(world, Objects.requireNonNull(context.getPlayer()), itemStack);
        }
        return result;
    }

    private ActionResult useOnDefaultBlock(ItemUsageContext context, ServerWorld world, ItemStack itemStack, BlockPos blockPos, BlockPos ignoredFaceBlockPos, Direction direction) {
        // if we are not sneaking, ignore.
        if (!Objects.requireNonNull(context.getPlayer()).isSneaking()) { return ActionResult.FAIL; }

        CaughtEntityData entityData = removeLastEntity(context.getPlayer(), itemStack);
        if (entityData == null) { return ActionResult.FAIL; }

        Entity entity = entityData.loadEntity(context.getWorld());
        if (entity == null) { return ActionResult.FAIL; }

        float f = entity.getWidth();
        double d = 0.55 + (double)(f / 2.0F);
        double x = (double)blockPos.getX() + 0.5 + d * (double)direction.getOffsetX() + (world.getRandom().nextFloat() * 2 - 1) * 0.4;
        double y = (double)blockPos.getY() + 0.5 + d * (double)direction.getOffsetY();
        double z = (double)blockPos.getZ() + 0.5 + d * (double)direction.getOffsetZ() + (world.getRandom().nextFloat() * 2 - 1) * 0.4;
        entity.refreshPositionAndAngles(x, y, z, entity.getYaw(), entity.getPitch());
        world.spawnEntity(entity);

        spawnCatchParticles(world, x, y, z);
        return ActionResult.SUCCESS;
    }


    public static boolean addEntity(PlayerEntity player, ItemStack itemStack, Entity entity) { return addEntity(player, itemStack, CaughtEntityData.of(entity)); }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean addEntity(PlayerEntity player, ItemStack itemStack, NbtComponent entityData) { return addEntity(player, itemStack, new CaughtEntityData(entityData)); }

    public static boolean addEntity(PlayerEntity player, ItemStack itemStack, CaughtEntityData entity) {
        // if we are in creative we don't care.
        if (Objects.requireNonNull(player).isCreative()) {  return true; }

        if ((getCaughtAmount(itemStack) + 1) > MAX_CAUGHT_AMOUNT) { return false; }

        List<CaughtEntityData> caughtEntitistartes = new ArrayList<>(itemStack.getOrDefault(ModDataComponentTypes.CAUGHT_ENTITIES, List.of()));
        caughtEntities.add(entity);
        itemStack.set(ModDataComponentTypes.CAUGHT_ENTITIES, caughtEntities);
        return true;
    }


    public static CaughtEntityData removeLastEntity(PlayerEntity player, ItemStack itemStack) {
        return removeLastEntity(player, itemStack, null);
    }

    public static CaughtEntityData removeLastEntity(PlayerEntity player, ItemStack itemStack, @Nullable Identifier filterEntityId) {
        // if we are in creative we don't care.
        if (Objects.requireNonNull(player).isCreative()) {  return null; }

        if (getCaughtAmount(itemStack) < 1) { return null; }


        List<CaughtEntityData> caughtEntities = new ArrayList<>(itemStack.getOrDefault(ModDataComponentTypes.CAUGHT_ENTITIES, List.of()));
        CaughtEntityData entityData;

        if (filterEntityId != null) {
            List<CaughtEntityData> list = caughtEntities.stream().filter(caughtEntityData -> Objects.equals(caughtEntityData.entityData.getNbt().get("id"), filterEntityId)).toList();

            if (list.stream().findFirst().isEmpty()) { return null; }

            entityData = list.stream().findFirst().get();
            caughtEntities.remove(entityData);
        } else {
            entityData = caughtEntities.removeLast();
        }

        itemStack.set(ModDataComponentTypes.CAUGHT_ENTITIES, caughtEntities);
        return entityData;
    }


    public record CaughtEntityData(NbtComponent entityData) {
        public static final Codec<CatchingNet.CaughtEntityData> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(NbtComponent.CODEC.optionalFieldOf("entity_data", NbtComponent.DEFAULT).forGetter(CatchingNet.CaughtEntityData::entityData))
                .apply(instance, CatchingNet.CaughtEntityData::new)
        );

        public static final Codec<List<CatchingNet.CaughtEntityData>> LIST_CODEC = CODEC.listOf();
        @SuppressWarnings("deprecation")
        public static final PacketCodec<ByteBuf, CatchingNet.CaughtEntityData> PACKET_CODEC = PacketCodec.tuple(
                NbtComponent.PACKET_CODEC,
                CatchingNet.CaughtEntityData::entityData,
                CatchingNet.CaughtEntityData::new
        );

        public static CatchingNet.CaughtEntityData of(Entity entity) {
            NbtCompound nbtCompound = new NbtCompound();
            entity.saveNbt(nbtCompound);
            CatchingNet.IRRELEVANT_NBT_KEYS.forEach(nbtCompound::remove);
            return new CatchingNet.CaughtEntityData(NbtComponent.of(nbtCompound));
        }

        @Nullable
        public Entity loadEntity(World world) {
            NbtCompound nbtCompound = this.entityData.copyNbt();
            CatchingNet.IRRELEVANT_NBT_KEYS.forEach(nbtCompound::remove);
            Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, world, entityx -> entityx);
            if (entity != null) {
                entity.setNoGravity(true);
                return entity;
            } else {
                return null;
            }
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
