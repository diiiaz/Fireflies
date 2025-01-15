package io.github.diiiaz.fireflies.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FireflyEntity extends AmbientEntity {

    public FireflyEntity(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
    }


    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 2.0);
    }


    @Override
    public void tick() {
        super.tick();
        this.setVelocity(this.getVelocity().multiply(1.0, 0.6, 1.0));
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
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }


    //    private static final TrackedData<Byte> BAT_FLAGS = DataTracker.registerData(FireflyEntity.class, TrackedDataHandlerRegistry.BYTE);
//    private static final int ROOSTING_FLAG = 1;
//    private static final TargetPredicate CLOSE_PLAYER_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(4.0);
//    public final AnimationState flyingAnimationState = new AnimationState();
//    public final AnimationState roostingAnimationState = new AnimationState();
//    @Nullable
//    private BlockPos hangingPosition;
//
//    public FireflyEntity(EntityType<? extends FireflyEntity> entityType, World world) {
//        super(entityType, world);
//        if (!world.isClient) {
//            this.setRoosting(true);
//        }
//    }
//
//    @Override
//    public boolean isFlappingWings() {
//        return !this.isRoosting() && (float)this.age % 10.0F == 0.0F;
//    }
//
//    @Override
//    protected void initDataTracker(DataTracker.Builder builder) {
//        super.initDataTracker(builder);
//        builder.add(BAT_FLAGS, (byte)0);
//    }
//
//    @Override
//    protected float getSoundVolume() {
//        return 0.1F;
//    }
//
//    @Override
//    public float getSoundPitch() {
//        return super.getSoundPitch() * 0.95F;
//    }
//
//    @Nullable
//    @Override
//    public SoundEvent getAmbientSound() {
//        return this.isRoosting() && this.random.nextInt(4) != 0 ? null : SoundEvents.ENTITY_BAT_AMBIENT;
//    }
//
//    @Override
//    protected SoundEvent getHurtSound(DamageSource source) {
//        return SoundEvents.ENTITY_BAT_HURT;
//    }
//
//    @Override
//    protected SoundEvent getDeathSound() {
//        return SoundEvents.ENTITY_BAT_DEATH;
//    }
//
//    @Override
//    public boolean isPushable() {
//        return false;
//    }
//
//    @Override
//    protected void pushAway(Entity entity) {
//    }
//
//    @Override
//    protected void tickCramming() {
//    }
//
//    public static DefaultAttributeContainer.Builder createAttributes() {
//        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 6.0);
//    }
//
//    /**
//     * Returns whether this bat is hanging upside-down under a block.
//     */
//    public boolean isRoosting() {
//        return (this.dataTracker.get(BAT_FLAGS) & 1) != 0;
//    }
//
//    public void setRoosting(boolean roosting) {
//        byte b = this.dataTracker.get(BAT_FLAGS);
//        if (roosting) {
//            this.dataTracker.set(BAT_FLAGS, (byte)(b | 1));
//        } else {
//            this.dataTracker.set(BAT_FLAGS, (byte)(b & -2));
//        }
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//        if (this.isRoosting()) {
//            this.setVelocity(Vec3d.ZERO);
//            this.setPos(this.getX(), (double) MathHelper.floor(this.getY()) + 1.0 - (double)this.getHeight(), this.getZ());
//        } else {
//            this.setVelocity(this.getVelocity().multiply(1.0, 0.6, 1.0));
//        }
//
//        this.updateAnimations();
//    }
//
//    @Override
//    protected void mobTick(ServerWorld world) {
//        super.mobTick(world);
//        BlockPos blockPos = this.getBlockPos();
//        BlockPos blockPos2 = blockPos.up();
//        if (this.isRoosting()) {
//            boolean bl = this.isSilent();
//            if (world.getBlockState(blockPos2).isSolidBlock(world, blockPos)) {
//                if (this.random.nextInt(200) == 0) {
//                    this.headYaw = (float)this.random.nextInt(360);
//                }
//
//                if (world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, this) != null) {
//                    this.setRoosting(false);
//                    if (!bl) {
//                        world.syncWorldEvent(null, WorldEvents.BAT_TAKES_OFF, blockPos, 0);
//                    }
//                }
//            } else {
//                this.setRoosting(false);
//                if (!bl) {
//                    world.syncWorldEvent(null, WorldEvents.BAT_TAKES_OFF, blockPos, 0);
//                }
//            }
//        } else {
//            if (this.hangingPosition != null && (!world.isAir(this.hangingPosition) || this.hangingPosition.getY() <= world.getBottomY())) {
//                this.hangingPosition = null;
//            }
//
//            if (this.hangingPosition == null || this.random.nextInt(30) == 0 || this.hangingPosition.isWithinDistance(this.getPos(), 2.0)) {
//                this.hangingPosition = BlockPos.ofFloored(
//                        this.getX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7),
//                        this.getY() + (double)this.random.nextInt(6) - 2.0,
//                        this.getZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7)
//                );
//            }
//
//            double d = (double)this.hangingPosition.getX() + 0.5 - this.getX();
//            double e = (double)this.hangingPosition.getY() + 0.1 - this.getY();
//            double f = (double)this.hangingPosition.getZ() + 0.5 - this.getZ();
//            Vec3d vec3d = this.getVelocity();
//            Vec3d vec3d2 = vec3d.add((Math.signum(d) * 0.5 - vec3d.x) * 0.1F, (Math.signum(e) * 0.7F - vec3d.y) * 0.1F, (Math.signum(f) * 0.5 - vec3d.z) * 0.1F);
//            this.setVelocity(vec3d2);
//            float g = (float)(MathHelper.atan2(vec3d2.z, vec3d2.x) * 180.0F / (float)Math.PI) - 90.0F;
//            float h = MathHelper.wrapDegrees(g - this.getYaw());
//            this.forwardSpeed = 0.5F;
//            this.setYaw(this.getYaw() + h);
//            if (this.random.nextInt(100) == 0 && world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
//                this.setRoosting(true);
//            }
//        }
//    }
//
//    @Override
//    protected Entity.MoveEffect getMoveEffect() {
//        return Entity.MoveEffect.EVENTS;
//    }
//
//    @Override
//    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
//    }
//
//    @Override
//    public boolean canAvoidTraps() {
//        return true;
//    }
//
//    @Override
//    public boolean damage(ServerWorld world, DamageSource source, float amount) {
//        if (this.isInvulnerableTo(world, source)) {
//            return false;
//        } else {
//            if (this.isRoosting()) {
//                this.setRoosting(false);
//            }
//
//            return super.damage(world, source, amount);
//        }
//    }
//
//    @Override
//    public void readCustomDataFromNbt(NbtCompound nbt) {
//        super.readCustomDataFromNbt(nbt);
//        this.dataTracker.set(BAT_FLAGS, nbt.getByte("BatFlags"));
//    }
//
//    @Override
//    public void writeCustomDataToNbt(NbtCompound nbt) {
//        super.writeCustomDataToNbt(nbt);
//        nbt.putByte("BatFlags", this.dataTracker.get(BAT_FLAGS));
//    }
//
//    public static boolean canSpawn(EntityType<FireflyEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
//        if (pos.getY() >= world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos).getY()) {
//            return false;
//        } else {
//            int i = world.getLightLevel(pos);
//            int j = 4;
//            if (random.nextBoolean()) {
//                return false;
//            }
//
//            if (i > random.nextInt(j)) {
//                return false;
//            } else {
//                return world.getBlockState(pos.down()).isIn(BlockTags.BATS_SPAWNABLE_ON) && canMobSpawn(type, world, spawnReason, pos, random);
//            }
//        }
//    }
//
//    private void updateAnimations() {
//        if (this.isRoosting()) {
//            this.flyingAnimationState.stop();
//            this.roostingAnimationState.startIfNotRunning(this.age);
//        } else {
//            this.roostingAnimationState.stop();
//            this.flyingAnimationState.startIfNotRunning(this.age);
//        }
//    }
}



