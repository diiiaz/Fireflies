package io.github.diiiaz.fireflies.entity.custom;

import com.google.common.collect.Lists;
import io.github.diiiaz.fireflies.block.ModBlocks;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.block.entity.custom.LuminescentSoilBlockEntity;
import io.github.diiiaz.fireflies.item.ModItems;
import io.github.diiiaz.fireflies.item.custom.FireflyBottle;
import io.github.diiiaz.fireflies.point_of_interest.ModPointOfInterestTypes;
import io.github.diiiaz.fireflies.sound.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.NoWaterTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FireflyEntity extends PathAwareEntity implements Flutterer {

    private static final float MAX_SPEED = 0.5F;
    private static final int TOO_FAR_DISTANCE = 48;
    private static final int MAX_WANDER_DISTANCE = 8;
    private static final String HOME_POS_KEY = "HomePos";
    private static final String VARIANT_KEY = "Variant";
    private static final String LIGHT_FREQUENCY_OFFSET_KEY = "LightFrequencyOffset";

    private static final TrackedData<Integer> DATA_ID_TYPE_VARIANT = DataTracker.registerData(FireflyEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> LIGHT_FREQUENCY_OFFSET = DataTracker.registerData(FireflyEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private BlockPos homePos;
    private int cannotEnterHomeTicks;
    private int ticksLeftToFindHome;
    private int ticksInsideWater;
    private MoveToHomeGoal moveToHomeGoal;


    // +--------------------------+ Defaults +--------------------------+

    public FireflyEntity(EntityType<? extends FireflyEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 0;
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.lookControl = new FireflyLookControl(this);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
        this.setPathfindingPenalty(PathNodeType.WATER_BORDER, 16.0F);
        this.setPathfindingPenalty(PathNodeType.COCOA, -1.0F);
        this.setPathfindingPenalty(PathNodeType.FENCE, -1.0F);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new FireflyEntity.EnterHomeGoal());
        this.goalSelector.add(3, new FireflyEntity.ValidateHomeGoal());
        this.goalSelector.add(5, new FireflyEntity.FindHomeGoal());
        this.moveToHomeGoal = new FireflyEntity.MoveToHomeGoal();
        this.goalSelector.add(5, this.moveToHomeGoal);
        this.goalSelector.add(8, new FireflyEntity.FireflyWanderAroundGoal());
        this.goalSelector.add(9, new SwimGoal(this));
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.dataTracker.set(LIGHT_FREQUENCY_OFFSET, this.random.nextFloat());
        this.setVariant(getRandomVariant(random).orElse(0));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    protected void tickCramming() {
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean isInAir() {
        return !this.isOnGround();
    }

    @Override
    protected void swimUpward(TagKey<Fluid> fluid) {
        this.setVelocity(this.getVelocity().add(0.0, 0.01, 0.0));
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.5F * this.getStandingEyeHeight(), this.getWidth() * 0.2F);
    }


    public float getLightFrequencyOffset() {
        return this.dataTracker.get(LIGHT_FREQUENCY_OFFSET);
    }


    // region +--------------------------+ Variants +--------------------------+


    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DATA_ID_TYPE_VARIANT, 0);
        builder.add(LIGHT_FREQUENCY_OFFSET, 0.0F);
    }


    public int getVariant() {
        return this.dataTracker.get(DATA_ID_TYPE_VARIANT);
    }

    private void setVariant(int variant) {
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, variant);
    }

    private Optional<Integer> getRandomVariant(Random random) {
        DataPool.Builder<Integer> pool = DataPool.builder();
        Arrays.stream(FireflyVariant.values()).forEach(variant -> pool.add(variant.getId(), variant.getWeight()));
        return pool.build().getDataOrEmpty(random);
    }


    // endregion

    // region +--------------------------+ Home Position +--------------------------+

    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
    }

    @Debug
    public boolean hasHomePos() {
        return this.homePos != null;
    }

    @Nullable
    @Debug
    public BlockPos getHomePos() {
        return this.homePos;
    }

    void clearHomePos() {
        this.homePos = null;
        this.ticksLeftToFindHome = 200;
    }


    boolean canEnterHome() {
        if (this.cannotEnterHomeTicks <= 0 && this.getTarget() == null) {
            return !isNight(this.getWorld()) && !this.isHomeNearFire();
        } else {
            return false;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNight(World world) {
        return world.getDimension().hasSkyLight() && world.isNight();
    }

    // endregion

    // region +--------------------------+ Home Block Entity +--------------------------+


    private boolean isHomeNearFire() {
        LuminescentSoilBlockEntity luminescentSoilBlockEntity = this.getHome();
        return luminescentSoilBlockEntity != null && luminescentSoilBlockEntity.isNearFire();
    }

    private boolean doesHomeHaveSpace(BlockPos pos) {
        BlockEntity blockEntity = this.getWorld().getBlockEntity(pos);
        return blockEntity instanceof LuminescentSoilBlockEntity && !((LuminescentSoilBlockEntity) blockEntity).isFullOfFireflies();
    }


    boolean hasValidHome() {
        return this.getHome() != null;
    }

    @Nullable
    LuminescentSoilBlockEntity getHome() {
        if (this.homePos == null) {
            return null;
        } else {
            return this.isTooFar(this.homePos) ? null : this.getWorld().getBlockEntity(this.homePos, ModBlockEntityTypes.LUMINESCENT_SOIL_BLOCK_ENTITY_TYPE).orElse(null);
        }
    }

    // endregion

    // region +--------------------------+ Mob Tick +--------------------------+

    @Override
    protected void mobTick(ServerWorld world) {
        if (this.isInsideWaterOrBubbleColumn()) {
            this.ticksInsideWater++;
        } else {
            this.ticksInsideWater = 0;
        }

        if (this.ticksInsideWater > 20) {
            this.damage(world, this.getDamageSources().drown(), 1.0F);
        }
    }

    // endregion

    // region +--------------------------+ NBT +--------------------------+


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(VARIANT_KEY, this.getVariant());
        nbt.putFloat(LIGHT_FREQUENCY_OFFSET_KEY, this.getLightFrequencyOffset());
        if (this.hasHomePos()) {
            //noinspection DataFlowIssue
            nbt.put(HOME_POS_KEY, NbtHelper.fromBlockPos(this.getHomePos()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.homePos = NbtHelper.toBlockPos(nbt, HOME_POS_KEY).orElse(null);
        this.dataTracker.set(LIGHT_FREQUENCY_OFFSET, nbt.getFloat(LIGHT_FREQUENCY_OFFSET_KEY));
        this.setVariant(nbt.getInt(VARIANT_KEY));
    }


    // endregion

    // region +--------------------------+ Path Finding +--------------------------+

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return world.getBlockState(pos).isAir() ? 20.0F : 0.0F;
    }

    boolean isTooFar(BlockPos pos) {
        return !this.isWithinDistance(pos, TOO_FAR_DISTANCE);
    }

    boolean isWithinDistance(BlockPos pos, int distance) {
        return pos.isWithinDistance(this.getBlockPos(), distance);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            if (this.cannotEnterHomeTicks > 0) {
                this.cannotEnterHomeTicks--;
            }

            if (this.ticksLeftToFindHome > 0) {
                this.ticksLeftToFindHome--;
            }

            if (this.age % 20 == 0 && !this.hasValidHome()) {
                this.homePos = null;
            }
        }
    }

    void startMovingTo(BlockPos pos) {
        Vec3d vec3d = Vec3d.ofBottomCenter(pos);
        int i = 0;
        BlockPos blockPos = this.getBlockPos();
        int j = (int)vec3d.y - blockPos.getY();
        if (j > 2) {
            i = 4;
        } else if (j < -2) {
            i = -4;
        }

        int k = 6;
        int l = 8;
        int m = blockPos.getManhattanDistance(pos);
        if (m < 15) {
            k = m / 2;
            l = m / 2;
        }

        Vec3d vec3d2 = NoWaterTargeting.find(this, k, l, i, vec3d, (float) (Math.PI / 10));
        if (vec3d2 != null) {
            this.navigation.setRangeMultiplier(0.5F);
            this.navigation.startMovingTo(vec3d2.x, vec3d2.y, vec3d2.z, 1.0);
        }
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world) {
            @Override
            public boolean isValidPosition(BlockPos pos) {
                return !this.world.getBlockState(pos.down()).isAir();
            }

        };
        birdNavigation.setCanPathThroughDoors(false);
        birdNavigation.setCanSwim(false);
        birdNavigation.setMaxFollowRange(TOO_FAR_DISTANCE);
        return birdNavigation;
    }

    // endregion

    // region +--------------------------+ Attributes +--------------------------+

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 2.0)
                .add(EntityAttributes.FLYING_SPEED, 0.6F)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3F);
    }

    // endregion

    // region +--------------------------+ Interactions +--------------------------+

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.GLASS_BOTTLE)) { return super.interactMob(player, hand); }
        if (player.getEntityWorld().isClient()) { return super.interactMob(player, hand); }

        ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
        FireflyBottle.spawnBottleParticles(serverWorld, this.getX(), this.getY(), this.getZ());
        FireflyBottle.playBottleSound(serverWorld, player, itemStack);
        ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, player, ModItems.FIREFLY_BOTTLE.getDefaultStack());
        player.setStackInHand(hand, itemStack2);
        this.remove(RemovalReason.DISCARDED);
        return ActionResult.SUCCESS_SERVER;
    }

    // endregion

    // region +--------------------------+ Sounds +--------------------------+

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {

    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.FIREFLY_AMBIENT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.AMBIENT;
    }

    // endregion

    // region +--------------------------+ Hurt & Death +--------------------------+

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        } else {
            return super.damage(world, source, amount);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (!this.getEntityWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld) this.getEntityWorld();
            serverWorld.spawnParticles(
                    ParticleTypes.WHITE_SMOKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    5,
                    0, 0, 0,
                    0.01);
        }
        this.remove(Entity.RemovalReason.KILLED);
    }

    // endregion

    // region +--------------------------+ Goals +--------------------------+


    abstract static class NormalGoal extends Goal {
        public abstract boolean canFireflyStart();

        public abstract boolean canFireflyContinue();

        @Override
        public boolean canStart() {
            return this.canFireflyStart();
        }

        @Override
        public boolean shouldContinue() {
            return this.canFireflyContinue();
        }
    }


    static class FireflyLookControl extends LookControl {
        FireflyLookControl(final MobEntity entity) {
            super(entity);
        }
    }



    class FireflyWanderAroundGoal extends Goal {
        FireflyWanderAroundGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return FireflyEntity.this.navigation.isIdle() && FireflyEntity.this.random.nextInt(2) == 0;
        }

        @Override
        public boolean shouldContinue() {
            return FireflyEntity.this.navigation.isFollowingPath();
        }

        @Override
        public void start() {
            Vec3d vec3d = this.getRandomLocation();
            if (vec3d != null) {
                FireflyEntity.this.navigation.startMovingAlong(FireflyEntity.this.navigation.findPathTo(BlockPos.ofFloored(vec3d), 1), MAX_SPEED);
            }
        }

        @Nullable
        private Vec3d getRandomLocation() {
            Vec3d vec3d2;
            if (FireflyEntity.this.hasValidHome() && !FireflyEntity.this.isWithinDistance(FireflyEntity.this.homePos, MAX_WANDER_DISTANCE)) {
                Vec3d vec3d = Vec3d.ofCenter(FireflyEntity.this.homePos);
                vec3d2 = vec3d.subtract(FireflyEntity.this.getPos()).normalize();
            } else {
                vec3d2 = FireflyEntity.this.getRotationVec(0.0F);
            }

            Vec3d vec3d3 = AboveGroundTargeting.find(FireflyEntity.this, 8, 7, vec3d2.x, vec3d2.z, (float) (Math.PI / 2), 3, 1);
            return vec3d3 != null ? vec3d3 : NoPenaltySolidTargeting.find(FireflyEntity.this, 8, 4, -2, vec3d2.x, vec3d2.z, (float) (Math.PI / 2));
        }

    }

    
    class EnterHomeGoal extends FireflyEntity.NormalGoal {

        @Override
        public boolean canFireflyStart() {
            if (FireflyEntity.this.homePos != null && FireflyEntity.this.canEnterHome() && FireflyEntity.this.homePos.isWithinDistance(FireflyEntity.this.getPos(), 2.0)) {
                LuminescentSoilBlockEntity luminescentSoilBlockEntity = FireflyEntity.this.getHome();
                if (luminescentSoilBlockEntity != null) {
                    if (!luminescentSoilBlockEntity.isFullOfFireflies()) {
                        return true;
                    }

                    FireflyEntity.this.homePos = null;
                }
            }
            return false;
        }

        @Override
        public boolean canFireflyContinue() {
            return false;
        }

        @Override
        public void start() {
            LuminescentSoilBlockEntity luminescentSoilBlockEntity = FireflyEntity.this.getHome();
            if (luminescentSoilBlockEntity != null) {
                luminescentSoilBlockEntity.tryEnterHome(FireflyEntity.this);
            }
        }
    }


    class FindHomeGoal extends FireflyEntity.NormalGoal {

        @Override
        public boolean canFireflyStart() {
            return FireflyEntity.this.ticksLeftToFindHome == 0 && !FireflyEntity.this.hasHomePos() && FireflyEntity.this.canEnterHome();
        }

        @Override
        public boolean canFireflyContinue() {
            return false;
        }

        @Override
        public void start() {
            FireflyEntity.this.ticksLeftToFindHome = 200;
            List<BlockPos> list = this.getNearbyFreeHomes();
            if (!list.isEmpty()) {
                for (BlockPos blockPos : list) {
                    if (!FireflyEntity.this.moveToHomeGoal.isPossibleHome(blockPos)) {
                        FireflyEntity.this.homePos = blockPos;
                        return;
                    }
                }

                FireflyEntity.this.moveToHomeGoal.clearPossibleHome();
                FireflyEntity.this.homePos = list.getFirst();
            }
        }

        private List<BlockPos> getNearbyFreeHomes() {
            BlockPos blockPos = FireflyEntity.this.getBlockPos();
            PointOfInterestStorage pointOfInterestStorage = ((ServerWorld)FireflyEntity.this.getWorld()).getPointOfInterestStorage();
            Stream<PointOfInterest> stream = pointOfInterestStorage.getInCircle(
                    poiType -> poiType.matchesKey(ModPointOfInterestTypes.FIREFLY_HOME), blockPos, 20, PointOfInterestStorage.OccupationStatus.ANY
            );
            return stream.map(PointOfInterest::getPos)
                    .filter(FireflyEntity.this::doesHomeHaveSpace)
                    .sorted(Comparator.comparingDouble(blockPos2 -> blockPos2.getSquaredDistance(blockPos)))
                    .collect(Collectors.toList());
        }
    }


    @Debug
    public class MoveToHomeGoal extends FireflyEntity.NormalGoal {

        int ticks = FireflyEntity.this.getWorld().random.nextInt(10);
        final List<BlockPos> possibleHomes = Lists.newArrayList();
        @Nullable
        private Path path;
        private int ticksUntilLost;

        MoveToHomeGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canFireflyStart() {
            return FireflyEntity.this.homePos != null
                    && !FireflyEntity.this.isTooFar(FireflyEntity.this.homePos)
                    && !FireflyEntity.this.hasPositionTarget()
                    && FireflyEntity.this.canEnterHome()
                    && !this.isCloseEnough(FireflyEntity.this.homePos)
                    && FireflyEntity.this.getWorld().getBlockState(FireflyEntity.this.homePos).getBlock() == ModBlocks.LUMINESCENT_SOIL;
        }

        @Override
        public boolean canFireflyContinue() {
            return this.canFireflyStart();
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.ticksUntilLost = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.ticks = 0;
            this.ticksUntilLost = 0;
            FireflyEntity.this.navigation.stop();
            FireflyEntity.this.navigation.resetRangeMultiplier();
        }

        @Override
        public void tick() {
            if (FireflyEntity.this.homePos != null) {
                this.ticks++;
                if (this.ticks > this.getTickCount(2400)) {
                    this.makeChosenHomePossibleHome();
                } else if (!FireflyEntity.this.navigation.isFollowingPath()) {
                    if (!FireflyEntity.this.isWithinDistance(FireflyEntity.this.homePos, MAX_WANDER_DISTANCE)) {
                        if (FireflyEntity.this.isTooFar(FireflyEntity.this.homePos)) {
                            FireflyEntity.this.clearHomePos();
                        } else {
                            FireflyEntity.this.startMovingTo(FireflyEntity.this.homePos);
                        }
                    } else {
                        boolean bl = this.startMovingToFar(FireflyEntity.this.homePos);
                        if (!bl) {
                            this.makeChosenHomePossibleHome();
                        } else if (this.path != null && Objects.requireNonNull(FireflyEntity.this.navigation.getCurrentPath()).equalsPath(this.path)) {
                            this.ticksUntilLost++;
                            if (this.ticksUntilLost > 60) {
                                FireflyEntity.this.clearHomePos();
                                this.ticksUntilLost = 0;
                            }
                        } else {
                            this.path = FireflyEntity.this.navigation.getCurrentPath();
                        }
                    }
                }
            }
        }

        private boolean startMovingToFar(BlockPos pos) {
            int i = FireflyEntity.this.isWithinDistance(pos, 3) ? 1 : 2;
            FireflyEntity.this.navigation.setRangeMultiplier(10.0F);
            FireflyEntity.this.navigation.startMovingTo(pos.getX(), pos.getY(), pos.getZ(), i, 1.0);
            return FireflyEntity.this.navigation.getCurrentPath() != null && FireflyEntity.this.navigation.getCurrentPath().reachesTarget();
        }

        boolean isPossibleHome(BlockPos pos) {
            return this.possibleHomes.contains(pos);
        }

        private void addPossibleHome(BlockPos pos) {
            this.possibleHomes.add(pos);

            while (this.possibleHomes.size() > 3) {
                this.possibleHomes.removeFirst();
            }
        }

        void clearPossibleHome() {
            this.possibleHomes.clear();
        }

        private void makeChosenHomePossibleHome() {
            if (FireflyEntity.this.homePos != null) {
                this.addPossibleHome(FireflyEntity.this.homePos);
            }

            FireflyEntity.this.clearHomePos();
        }

        private boolean isCloseEnough(BlockPos pos) {
            if (FireflyEntity.this.isWithinDistance(pos, 2)) {
                return true;
            } else {
                Path path = FireflyEntity.this.navigation.getCurrentPath();
                return path != null && path.getTarget().equals(pos) && path.reachesTarget() && path.isFinished();
            }
        }
    }


    class ValidateHomeGoal extends FireflyEntity.NormalGoal {
        private final int ticksUntilNextValidate = MathHelper.nextInt(FireflyEntity.this.random, 20, 40);
        private long lastValidateTime = -1L;

        @Override
        public void start() {
            if (FireflyEntity.this.homePos != null && FireflyEntity.this.getWorld().isPosLoaded(FireflyEntity.this.homePos) && !FireflyEntity.this.hasValidHome()) {
                FireflyEntity.this.clearHomePos();
            }

            this.lastValidateTime = FireflyEntity.this.getWorld().getTime();
        }

        @Override
        public boolean canFireflyStart() {
            return FireflyEntity.this.getWorld().getTime() > this.lastValidateTime + (long)this.ticksUntilNextValidate;
        }

        @Override
        public boolean canFireflyContinue() {
            return false;
        }

    }


//    static class FireflyMoveControl extends MoveControl {
//        private final FireflyEntity firefly;
//        private int collisionCheckCooldown;
//
//        public FireflyMoveControl(FireflyEntity firefly) {
//            super(firefly);
//            this.firefly = firefly;
//        }
//
//        @Override
//        public void tick() {
//            if (this.state == MoveControl.State.MOVE_TO) {
//                if (this.collisionCheckCooldown-- <= 0) {
//                    this.collisionCheckCooldown = this.collisionCheckCooldown + this.firefly.getRandom().nextInt(2) + 1;
//                    Vec3d vec3d = new Vec3d(this.targetX - this.firefly.getX(), this.targetY - this.firefly.getY(), this.targetZ - this.firefly.getZ());
//                    double d = vec3d.length();
//                    vec3d = vec3d.normalize();
//                    if (this.willCollide(vec3d, MathHelper.ceil(d))) {
//                        this.firefly.setVelocity(this.firefly.getVelocity().add(vec3d.multiply(MAX_SPEED)));
//                    } else {
//                        this.state = MoveControl.State.WAIT;
//                    }
//                }
//            }
//        }
//
//        private boolean willCollide(Vec3d direction, int steps) {
//            Box box = this.firefly.getBoundingBox();
//
//            for (int i = 1; i < steps; i++) {
//                box = box.offset(direction);
//                if (!this.firefly.getWorld().isSpaceEmpty(this.firefly, box)) {
//                    return false;
//                }
//            }
//
//            return true;
//        }
//    }
    // endregion
}