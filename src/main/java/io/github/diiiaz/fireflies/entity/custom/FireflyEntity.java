package io.github.diiiaz.fireflies.entity.custom;

import io.github.diiiaz.fireflies.item.ModItems;
import io.github.diiiaz.fireflies.item.custom.FireflyBottle;
import io.github.diiiaz.fireflies.sound.ModSounds;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FireflyEntity extends FlyingEntity {

    private static final int MAX_X = 12;
    private static final int MAX_Y = 6;
    private static final int MAX_Z = 12;
    private static final float MAX_SPEED = 0.01F;

    private BlockPos turnAroundPos;
    public double random;


    public FireflyEntity(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 0;
        this.turnAroundPos = BlockPos.ORIGIN;
        this.random = world.getRandom().nextFloat();
        this.moveControl = new FireflyEntity.FireflyMoveControl(this);
    }


    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(5, new FireflyEntity.FlyRandomlyGoal(this));
        this.goalSelector.add(7, new FireflyEntity.LookAtTargetGoal(this));
    }


    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 2.0)
                .add(EntityAttributes.FOLLOW_RANGE, 100.0);
    }


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
        this.discard();
        return ActionResult.SUCCESS_SERVER;
    }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        LongArrayList arr = new LongArrayList(3);
        arr.add(this.turnAroundPos.getX());
        arr.add(this.turnAroundPos.getY());
        arr.add(this.turnAroundPos.getZ());
        nbt.putLongArray("TurnAroundPos", arr);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (!nbt.contains("TurnAroundPos", NbtElement.LONG_ARRAY_TYPE)) {
            return;
        }
        LongArrayList arr = LongArrayList.wrap(nbt.getLongArray("TurnAroundPos"));
        this.turnAroundPos = new BlockPos(Math.toIntExact(arr.get(0)), Math.toIntExact(arr.get(1)), Math.toIntExact(arr.get(2)));
    }


    static class FlyRandomlyGoal extends Goal {
        private final FireflyEntity firefly;

        public FlyRandomlyGoal(FireflyEntity firefly) {
            this.firefly = firefly;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            MoveControl moveControl = this.firefly.getMoveControl();
            if (!moveControl.isMoving()) {
                return true;
            } else {
                double xPos = moveControl.getTargetX() - this.firefly.getX();
                double yPos = moveControl.getTargetY() - this.firefly.getY();
                double zPos = moveControl.getTargetZ() - this.firefly.getZ();
                double g = xPos * xPos + yPos * yPos + zPos * zPos;
                return g < 1.0 || g > 3600.0;
            }
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            Random random = this.firefly.getRandom();

            if (this.firefly.turnAroundPos == BlockPos.ORIGIN) {
                this.firefly.turnAroundPos = this.firefly.getBlockPos();
            }

            double xPos = this.firefly.turnAroundPos.getX() + (double) ((random.nextFloat() * 2.0F - 1.0F) * MAX_X);
            double yPos = this.firefly.turnAroundPos.getY() + (double) (random.nextFloat() * MAX_Y);
            double zPos = this.firefly.turnAroundPos.getZ() + (double) ((random.nextFloat() * 2.0F - 1.0F) * MAX_Z);
/*          These positions limit a firefly position to a box of (MAX_X x MAX_Z) horizontally centered around 'turnAroundPos', and MAX_Y in height starting from 'turnAroundPos'.
            Here is an approximation :

                        +-------------------+
                       /|                  /|     P = turnAroundPos
                      / |                 / |
                     /  |                /  |
                    +-------------------+   |
                    |   + - - - - - - - | - +
                    |  /                |  /
                    | /        P        | /
                    |/                  |/
                    +-------------------+
 */

            this.firefly.getMoveControl().moveTo(xPos, yPos, zPos, 1.0);
        }
    }


    static class FireflyMoveControl extends MoveControl {
        private final FireflyEntity firefly;
        private int collisionCheckCooldown;

        public FireflyMoveControl(FireflyEntity firefly) {
            super(firefly);
            this.firefly = firefly;
        }

        @Override
        public void tick() {
            if (this.state == MoveControl.State.MOVE_TO) {
                if (this.collisionCheckCooldown-- <= 0) {
                    this.collisionCheckCooldown = this.collisionCheckCooldown + this.firefly.getRandom().nextInt(2) + 1;
                    Vec3d vec3d = new Vec3d(this.targetX - this.firefly.getX(), this.targetY - this.firefly.getY(), this.targetZ - this.firefly.getZ());
                    double d = vec3d.length();
                    vec3d = vec3d.normalize();
                    if (this.willCollide(vec3d, MathHelper.ceil(d))) {
                        this.firefly.setVelocity(this.firefly.getVelocity().add(vec3d.multiply(MAX_SPEED)));
                    } else {
                        this.state = MoveControl.State.WAIT;
                    }
                }
            }
        }

        private boolean willCollide(Vec3d direction, int steps) {
            Box box = this.firefly.getBoundingBox();

            for (int i = 1; i < steps; i++) {
                box = box.offset(direction);
                if (!this.firefly.getWorld().isSpaceEmpty(this.firefly, box)) {
                    return false;
                }
            }

            return true;
        }
    }


    static class LookAtTargetGoal extends Goal {
        private final FireflyEntity firefly;

        public LookAtTargetGoal(FireflyEntity firefly) {
            this.firefly = firefly;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Vec3d vec3d = this.firefly.getVelocity();
            this.firefly.setYaw(-((float) MathHelper.atan2(vec3d.x, vec3d.z)) * (180.0F / (float) Math.PI));
            this.firefly.bodyYaw = this.firefly.getYaw();
        }
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
    public SoundCategory getSoundCategory() {
        return SoundCategory.AMBIENT;
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
        this.discard();
    }
}