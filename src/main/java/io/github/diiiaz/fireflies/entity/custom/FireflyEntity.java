package io.github.diiiaz.fireflies.entity.custom;

import com.google.common.collect.Lists;
import io.github.diiiaz.fireflies.block.ModBlocks;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyAlcoveBlockEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FireflyEntity extends PathAwareEntity implements Flutterer {

    /**
     * The minimum distance that fireflies lose their alcove position at.
     */
    private static final int TOO_FAR_DISTANCE = 48;
    /**
     * The minimum distance that fireflies will immediately return to their alcove at.
     */
    private static final int MAX_WANDER_DISTANCE = 8;
    public static final String ALCOVE_POS_KEY = "alcove_pos";

    private static final float MAX_SPEED = 0.5F;

    private BlockPos alcovePos;

    private int cannotEnterAlcoveTicks;
    int ticksLeftToFindAlcove;
    private int ticksInsideWater;
    FireflyEntity.MoveToAlcoveGoal moveToAlcoveGoal;

    public final double lightRandomValue;


    // +--------------------------+ Defaults +--------------------------+

    public FireflyEntity(EntityType<? extends FireflyEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 0;
        this.lightRandomValue = getRandom().nextFloat();
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
        this.goalSelector.add(1, new FireflyEntity.EnterAlcoveGoal());
        this.goalSelector.add(3, new FireflyEntity.ValidateAlcoveGoal());
        this.goalSelector.add(5, new FireflyEntity.FindAlcoveGoal());
        this.moveToAlcoveGoal = new FireflyEntity.MoveToAlcoveGoal();
        this.goalSelector.add(5, this.moveToAlcoveGoal);
        this.goalSelector.add(8, new FireflyEntity.FireflyWanderAroundGoal());
        this.goalSelector.add(9, new SwimGoal(this));
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


    // region +--------------------------+ Alcove Position +--------------------------+

    public void setAlcovePos(BlockPos pos) {
        this.alcovePos = pos;
    }

    @Debug
    public boolean hasAlcovePos() {
        return this.alcovePos != null;
    }

    @Nullable
    @Debug
    public BlockPos getAlcovePos() {
        return this.alcovePos;
    }

    void clearAlcovePos() {
        this.alcovePos = null;
        this.ticksLeftToFindAlcove = 200;
    }


    boolean canEnterAlcove() {
        if (this.cannotEnterAlcoveTicks <= 0 && this.getTarget() == null) {
            return !isNight(this.getWorld()) && !this.isAlcoveNearFire();
        } else {
            return false;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNight(World world) {
        return world.getDimension().hasSkyLight() && world.isNight();
    }

//    public void setCannotEnterAlcoveTicks(int cannotEnterHiveTicks) {
//        this.cannotEnterAlcoveTicks = cannotEnterHiveTicks;
//    }

    // endregion

    // region +--------------------------+ Alcove Block Entity +--------------------------+


    private boolean isAlcoveNearFire() {
        FireflyAlcoveBlockEntity fireflyAlcoveBlockEntity = this.getAlcove();
        return fireflyAlcoveBlockEntity != null && fireflyAlcoveBlockEntity.isNearFire();
    }

    private boolean doesAlcoveHaveSpace(BlockPos pos) {
        BlockEntity blockEntity = this.getWorld().getBlockEntity(pos);
        return blockEntity instanceof FireflyAlcoveBlockEntity && !((FireflyAlcoveBlockEntity) blockEntity).isFullOfFireflies();
    }


    boolean hasValidAlcove() {
        return this.getAlcove() != null;
    }

    @Nullable
    FireflyAlcoveBlockEntity getAlcove() {
        if (this.alcovePos == null) {
            return null;
        } else {
            return this.isTooFar(this.alcovePos) ? null : this.getWorld().getBlockEntity(this.alcovePos, ModBlockEntityTypes.FIREFLY_ALCOVE_BLOCK_ENTITY_TYPE).orElse(null);
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
        if (this.hasAlcovePos()) {
            assert this.getAlcovePos() != null;
            nbt.put(ALCOVE_POS_KEY, NbtHelper.fromBlockPos(this.getAlcovePos()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.alcovePos = NbtHelper.toBlockPos(nbt, ALCOVE_POS_KEY).orElse(null);
    }

    // endregion

    // region +--------------------------+ Path Finding +--------------------------+

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return world.getBlockState(pos).isAir() ? 10.0F : 0.0F;
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
            if (this.cannotEnterAlcoveTicks > 0) {
                this.cannotEnterAlcoveTicks--;
            }

            if (this.ticksLeftToFindAlcove > 0) {
                this.ticksLeftToFindAlcove--;
            }

            if (this.age % 20 == 0 && !this.hasValidAlcove()) {
                this.alcovePos = null;
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
            if (FireflyEntity.this.hasValidAlcove() && !FireflyEntity.this.isWithinDistance(FireflyEntity.this.alcovePos, MAX_WANDER_DISTANCE)) {
                Vec3d vec3d = Vec3d.ofCenter(FireflyEntity.this.alcovePos);
                vec3d2 = vec3d.subtract(FireflyEntity.this.getPos()).normalize();
            } else {
                vec3d2 = FireflyEntity.this.getRotationVec(0.0F);
            }

            Vec3d vec3d3 = AboveGroundTargeting.find(FireflyEntity.this, 8, 7, vec3d2.x, vec3d2.z, (float) (Math.PI / 2), 3, 1);
            return vec3d3 != null ? vec3d3 : NoPenaltySolidTargeting.find(FireflyEntity.this, 8, 4, -2, vec3d2.x, vec3d2.z, (float) (Math.PI / 2));
        }

    }

    
    class EnterAlcoveGoal extends FireflyEntity.NormalGoal {

        @Override
        public boolean canFireflyStart() {
            if (FireflyEntity.this.alcovePos != null && FireflyEntity.this.canEnterAlcove() && FireflyEntity.this.alcovePos.isWithinDistance(FireflyEntity.this.getPos(), 2.0)) {
                FireflyAlcoveBlockEntity fireflyAlcoveBlockEntity = FireflyEntity.this.getAlcove();
                if (fireflyAlcoveBlockEntity != null) {
                    if (!fireflyAlcoveBlockEntity.isFullOfFireflies()) {
                        return true;
                    }

                    FireflyEntity.this.alcovePos = null;
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
            FireflyAlcoveBlockEntity fireflyAlcoveBlockEntity = FireflyEntity.this.getAlcove();
            if (fireflyAlcoveBlockEntity != null) {
                fireflyAlcoveBlockEntity.tryEnterAlcove(FireflyEntity.this);
            }
        }
    }


    class FindAlcoveGoal extends FireflyEntity.NormalGoal {

        @Override
        public boolean canFireflyStart() {
            return FireflyEntity.this.ticksLeftToFindAlcove == 0 && !FireflyEntity.this.hasAlcovePos() && FireflyEntity.this.canEnterAlcove();
        }

        @Override
        public boolean canFireflyContinue() {
            return false;
        }

        @Override
        public void start() {
            FireflyEntity.this.ticksLeftToFindAlcove = 200;
            List<BlockPos> list = this.getNearbyFreeAlcoves();
            if (!list.isEmpty()) {
                for (BlockPos blockPos : list) {
                    if (!FireflyEntity.this.moveToAlcoveGoal.isPossibleAlcove(blockPos)) {
                        FireflyEntity.this.alcovePos = blockPos;
                        return;
                    }
                }

                FireflyEntity.this.moveToAlcoveGoal.clearPossibleAlcove();
                FireflyEntity.this.alcovePos = list.getFirst();
            }
        }

        private List<BlockPos> getNearbyFreeAlcoves() {
            BlockPos blockPos = FireflyEntity.this.getBlockPos();
            PointOfInterestStorage pointOfInterestStorage = ((ServerWorld)FireflyEntity.this.getWorld()).getPointOfInterestStorage();
            Stream<PointOfInterest> stream = pointOfInterestStorage.getInCircle(
                    poiType -> poiType.matchesKey(ModPointOfInterestTypes.FIREFLY_HOME), blockPos, 20, PointOfInterestStorage.OccupationStatus.ANY
            );
            return stream.map(PointOfInterest::getPos)
                    .filter(FireflyEntity.this::doesAlcoveHaveSpace)
                    .sorted(Comparator.comparingDouble(blockPos2 -> blockPos2.getSquaredDistance(blockPos)))
                    .collect(Collectors.toList());
        }
    }


    @Debug
    public class MoveToAlcoveGoal extends FireflyEntity.NormalGoal {

        int ticks = FireflyEntity.this.getWorld().random.nextInt(10);
        final List<BlockPos> possibleAlcoves = Lists.newArrayList();
        @Nullable
        private Path path;
        private int ticksUntilLost;

        MoveToAlcoveGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canFireflyStart() {
            return FireflyEntity.this.alcovePos != null
                    && !FireflyEntity.this.isTooFar(FireflyEntity.this.alcovePos)
                    && !FireflyEntity.this.hasPositionTarget()
                    && FireflyEntity.this.canEnterAlcove()
                    && !this.isCloseEnough(FireflyEntity.this.alcovePos)
                    && FireflyEntity.this.getWorld().getBlockState(FireflyEntity.this.alcovePos).getBlock() == ModBlocks.FIREFLY_ALCOVE;
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
            if (FireflyEntity.this.alcovePos != null) {
                this.ticks++;
                if (this.ticks > this.getTickCount(2400)) {
                    this.makeChosenAlcovePossibleAlcove();
                } else if (!FireflyEntity.this.navigation.isFollowingPath()) {
                    if (!FireflyEntity.this.isWithinDistance(FireflyEntity.this.alcovePos, MAX_WANDER_DISTANCE)) {
                        if (FireflyEntity.this.isTooFar(FireflyEntity.this.alcovePos)) {
                            FireflyEntity.this.clearAlcovePos();
                        } else {
                            FireflyEntity.this.startMovingTo(FireflyEntity.this.alcovePos);
                        }
                    } else {
                        boolean bl = this.startMovingToFar(FireflyEntity.this.alcovePos);
                        if (!bl) {
                            this.makeChosenAlcovePossibleAlcove();
                        } else if (this.path != null && Objects.requireNonNull(FireflyEntity.this.navigation.getCurrentPath()).equalsPath(this.path)) {
                            this.ticksUntilLost++;
                            if (this.ticksUntilLost > 60) {
                                FireflyEntity.this.clearAlcovePos();
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

        boolean isPossibleAlcove(BlockPos pos) {
            return this.possibleAlcoves.contains(pos);
        }

        private void addPossibleAlcove(BlockPos pos) {
            this.possibleAlcoves.add(pos);

            while (this.possibleAlcoves.size() > 3) {
                this.possibleAlcoves.removeFirst();
            }
        }

        void clearPossibleAlcove() {
            this.possibleAlcoves.clear();
        }

        private void makeChosenAlcovePossibleAlcove() {
            if (FireflyEntity.this.alcovePos != null) {
                this.addPossibleAlcove(FireflyEntity.this.alcovePos);
            }

            FireflyEntity.this.clearAlcovePos();
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


    class ValidateAlcoveGoal extends FireflyEntity.NormalGoal {
        private final int ticksUntilNextValidate = MathHelper.nextInt(FireflyEntity.this.random, 20, 40);
        private long lastValidateTime = -1L;

        @Override
        public void start() {
            if (FireflyEntity.this.alcovePos != null && FireflyEntity.this.getWorld().isPosLoaded(FireflyEntity.this.alcovePos) && !FireflyEntity.this.hasValidAlcove()) {
                FireflyEntity.this.clearAlcovePos();
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