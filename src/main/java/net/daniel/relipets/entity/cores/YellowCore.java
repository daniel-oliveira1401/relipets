package net.daniel.relipets.entity.cores;

import lombok.Getter;
import net.daniel.relipets.entity.cores.abilities.YellowCoreStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class YellowCore extends BaseCore implements JumpingMount{

    private static final String YELLOW_CORE_STATS_KEY = "yellow_core_stats";
    public static final float DEFAULT_MIN_BOOST_SPEED = 0.5f;
    public static final float DEFAULT_MAX_BOOST_SPEED = 1f;
    public enum MovementMode {
        GROUND,
        FLYING
    }

    public static final TrackedDataHandler<YellowCoreStats> YELLOW_CORE_STATS_HANDLER = new TrackedDataHandler.ImmutableHandler<YellowCoreStats>() {
        public void write(PacketByteBuf packetByteBuf, YellowCoreStats yellowCoreStats) {
            packetByteBuf.writeFloat(yellowCoreStats.getMinBoostSpeed());
            packetByteBuf.writeFloat(yellowCoreStats.getMaxBoostSpeed());
        }

        public YellowCoreStats read(PacketByteBuf packetByteBuf) {
            return new YellowCoreStats(packetByteBuf.readFloat(), packetByteBuf.readFloat());
        }
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final TrackedData<YellowCoreStats> YELLOW_CORE_STATS_TRACKED_DATA = DataTracker.registerData(YellowCore.class, YELLOW_CORE_STATS_HANDLER);

    public YellowCore(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.dataTracker.startTracking(YELLOW_CORE_STATS_TRACKED_DATA, new YellowCoreStats(0.5f, 1));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::movementAnimController));
    }

    private PlayState movementAnimController(software.bernie.geckolib.core.animation.AnimationState<YellowCore> animationState) {
        if (animationState.isMoving() && this.isOnGround()) {
            this.setCurrentAnim(BaseCore.ANIM_WALK);

        } else if(!this.isOnGround() && this.getVelocity().horizontalLength() > 2) {
            //fly fast
            this.setCurrentAnim(BaseCore.ANIM_FLY_FAST);

        }else if (!this.isOnGround() && (this.getPitch() < -15 || !this.hasPassengers())) { //negative pitch == looking up
            //looking up -> flapping
            this.setCurrentAnim(BaseCore.ANIM_FLY);
        }else if (!this.isOnGround()){
            //looking down -> gliding
            this.setCurrentAnim(BaseCore.ANIM_GLIDE);
        }
        else {
            this.setCurrentAnim(BaseCore.ANIM_IDLE);
        }
        return PlayState.STOP;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        this.setupCoreForGroundMovement(this.getWorld());
        //passenger.setInvulnerable(true);
        super.addPassenger(passenger);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        this.applyEffectsFromParts(this.getWorld());
        //passenger.setInvulnerable(false);
        return super.updatePassengerForDismount(passenger);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ActionResult actionResult = super.interactMob(player, hand);

        if((!this.getWorld().isClient()) && actionResult.equals(ActionResult.FAIL)){
            if (player.getStackInHand(hand).isEmpty()) {
                player.startRiding(this);
            }
        }
        return ActionResult.CONSUME;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return true;
    }

    @Override
    public float getStepHeight() {
        return 1;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasPassengers() && this.getFirstPassenger() instanceof PlayerEntity rider) {
            this.fallDistance = 0;
        }
    }

    //can also be "flying"
    @Getter
    MovementMode movementMode = MovementMode.GROUND;

    @Override
    public void travel(Vec3d movementInput) {

        if (this.hasPassengers() && this.getFirstPassenger() instanceof PlayerEntity rider) {
            this.fallDistance = 0;

            this.setYaw(rider.getYaw());
            this.prevYaw = rider.prevYaw;
            // Use half the rider's pitch for a more natural mounting angle.
            this.setPitch(rider.getPitch());
            this.prevPitch = rider.prevPitch;
            this.setRotation(this.getYaw(), this.getPitch());
            this.headYaw = this.bodyYaw = this.getYaw();

            // Use the rider's forward and sideways speeds.
            float forwardInput = rider.forwardSpeed;
            float strafeInput = rider.sidewaysSpeed;

            if(movementMode == MovementMode.GROUND){
                this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);

                Vec3d movementVector = new Vec3d(strafeInput, movementInput.y, forwardInput);
                //owner hit space when they are in ground mode
                if(this.flightBoost > 0){
                    //just wants to jump
                    if(this.isOnGround()){
                        //movementVector = movementVector.add(0, 10, 0);
                        float boost = Math.max(this.flightBoost, this.getYellowCoreStats().getMinBoostSpeed());

                        boost = this.flightBoost > this.getYellowCoreStats().getMaxBoostSpeed()? this.getYellowCoreStats().getMaxBoostSpeed() : boost;

                        this.setVelocity(this.getVelocity().add(0, boost, 0));
                    }else{
                        //want to start flying
                        if(this.hasWings()){
                            this.movementMode = MovementMode.FLYING;
                            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
                            this.setupCoreForGroundMovement(this.getWorld());
                        }
                    }

                    this.flightBoost = 0;
                }

                super.travel(movementVector);

            }else if (movementMode == MovementMode.FLYING){

                this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);

                double d = 0.20;
                this.limitFallDistance();
                Vec3d resultingVelocity = this.getVelocity();
                Vec3d vec3d5 = rider.getRotationVector();
                float fx = rider.getPitch() * (float) (Math.PI / 180.0);
                double i = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
                double speed = resultingVelocity.horizontalLength();
                double k = vec3d5.length();
                double l = Math.cos((double)fx);
                l = l * l * Math.min(1.0, k / 0.4);
                resultingVelocity = this.getVelocity().add(0.0, d * (-1.0 + l * 0.75), 0.0);
                if (resultingVelocity.y < 0.0 && i > 0.0) {
                    double m = resultingVelocity.y * -0.1 * l;
                    resultingVelocity = resultingVelocity.add(vec3d5.x * m / i, m, vec3d5.z * m / i);
                }

                if (fx < 0.0F && i > 0.0) {
                    double m = speed * (double)(-MathHelper.sin(fx)) * 0.04;
                    resultingVelocity = resultingVelocity.add(-vec3d5.x * m / i, m * 5.2, -vec3d5.z * m / i);
                }

                if (i > 0.0) {
                    resultingVelocity = resultingVelocity.add((vec3d5.x / i * speed - resultingVelocity.x) * 0.1, 0.0, (vec3d5.z / i * speed - resultingVelocity.z) * 0.1);
                }

                //resultingVelocity = resultingVelocity.multiply(0.99F, 0.98F, 0.99F);

                if(this.flightBoost > 0){
                    resultingVelocity = resultingVelocity.add(rider.getRotationVector().normalize().multiply(flightBoost));
                    this.flightBoost = 0;
                }

                this.setVelocity(resultingVelocity);
                this.move(MovementType.SELF, this.getVelocity());

                if(this.isOnGround() || !this.hasWings()){
                    this.movementMode = MovementMode.GROUND;
                    this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
                }
            }

            // Delegate to vanilla travel handling (this includes auto stepping over blocks).
        }else {
            super.travel(movementInput);
        }


        this.updateLimbs(this instanceof Flutterer);

    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity entity? entity : null;
    }

    @Override
    public double getMountedHeightOffset() {
        return this.getDimensions(EntityPose.STANDING).height * 0.5;
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
    }

    float flightBoost = 0;

    @Override
    public void setJumpStrength(int strength) {
        if(this.movementMode == MovementMode.FLYING){
            this.flightBoost = Math.max(this.getYellowCoreStats().getMinBoostSpeed()
                    , (strength / 100.0f) * this.getYellowCoreStats().getMaxBoostSpeed());
        }

    }

    public void boost(){
        if(movementMode == MovementMode.GROUND){
            this.flightBoost = this.getYellowCoreStats().getMinBoostSpeed();
        }
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void startJumping(int height) {
    }

    @Override
    public void stopJumping() {
    }

    public YellowCoreStats getYellowCoreStats(){
        return this.dataTracker.get(YELLOW_CORE_STATS_TRACKED_DATA);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.put(YELLOW_CORE_STATS_KEY, this.dataTracker.get(YELLOW_CORE_STATS_TRACKED_DATA).writeToNbt());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(YELLOW_CORE_STATS_TRACKED_DATA, new YellowCoreStats(nbt.getCompound(YELLOW_CORE_STATS_KEY)));

    }
}
