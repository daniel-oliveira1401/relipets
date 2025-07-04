package net.daniel.relipets.entity.util;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PetCameraEntity extends ArmorStandEntity {
    private final Entity spectatedEntity;

    public PetCameraEntity(World world, Entity spectatedEntity) {
        super(EntityType.ARMOR_STAND, world);
        this.setInvisible(true);
        this.setNoGravity(true);
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), MARKER_FLAG, true));
        this.setSilent(true);
        this.setCustomNameVisible(false);
        this.setInvulnerable(true);
        this.spectatedEntity = spectatedEntity;

        this.prevPos = this.getPos();
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();

        this.currentPos = this.prevPos;
        this.currentYaw = this.getYaw();
        this.currentPitch = this.getPitch();
    }


    //comes from ArmorStandEntity
    private byte setBitField(byte value, int bitField, boolean set) {
        if (set) {
            value = (byte)(value | bitField);
        } else {
            value = (byte)(value & ~bitField);
        }

        return value;
    }

    public Vec3d prevPos;
    public Vec3d currentPos;

    public float prevYaw, currentYaw;
    public float prevPitch, currentPitch;

    private double orbitAngle = 0; // in radians
    private final double orbitRadius = 6;
    private final double orbitHeight = 2;
    private final double orbitSpeed = 0.01; // radians per tick

    @Override
    public boolean shouldRender(double distance) {
        return false;
    }

    @Override
    public boolean shouldRenderName() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient && spectatedEntity != null) {
            orbitAngle += orbitSpeed;

            double targetX = spectatedEntity.getX();
            double targetY = spectatedEntity.getY() + orbitHeight;
            double targetZ = spectatedEntity.getZ();

            // Compute orbit position
            double camX = targetX + orbitRadius * Math.cos(orbitAngle);
            double camZ = targetZ + orbitRadius * Math.sin(orbitAngle);
            double camY = targetY;

            // Set camera position with optional smoothing

            this.setPosition(
                    ease(this.getX(), camX),
                    ease(this.getY(), camY),
                    ease(this.getZ(), camZ)
            );

            // Compute and smoothly update rotation
            Vec3d toTarget = spectatedEntity.getPos().add(0, 1.0, 0).subtract(this.getPos());
            double targetYaw = (Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90F);
            double horizontal =  Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            double targetPitch = (-Math.toDegrees(Math.atan2(toTarget.y, horizontal)));

            this.setYaw((float)lerpAngle(this.getYaw(), targetYaw, 0.02F));
            this.setPitch((float) lerpAngle(this.getPitch(), targetPitch, 0.02F));
            this.setHeadYaw(this.getYaw());
            this.setBodyYaw(this.getYaw());
        }

    }



    private double ease(double current, double target) {
        double distance = Math.abs(target - current);
        double factor = Math.min(1.0, Math.max(0.002, distance * 0.01));
        return current + (target - current) * factor;
    }

    private double lerpAngle(double a, double b, double t) {
        double diff = wrapDegrees(b - a);
        return a + diff * t;
    }

    private double wrapDegrees(double degrees) {
        degrees = degrees % 360.0F;
        if (degrees >= 180.0F) degrees -= 360.0F;
        if (degrees < -180.0F) degrees += 360.0F;
        return degrees;
    }

}
