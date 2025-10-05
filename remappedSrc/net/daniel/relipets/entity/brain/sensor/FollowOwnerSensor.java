package net.daniel.relipets.entity.brain.sensor;

import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.player.PlayerEntity;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class FollowOwnerSensor extends PredicateSensor{

    private int minDist = 5;
    private int maxDist = 30;

    public FollowOwnerSensor(){

        this.sensingCondition((entity)->{
            PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);

            return owner != null && entity.squaredDistanceTo(owner) > this.minDist * this.minDist && entity.squaredDistanceTo(owner) <= this.maxDist * this.maxDist;
        });

    }

    public FollowOwnerSensor minDistance(int minDist){
        this.minDist = minDist;
        return this;
    }

    public FollowOwnerSensor maxDistance(int maxDist){
        this.maxDist = maxDist;
        return this;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return RelipetsSensorTypes.FOLLOW_OWNER_SENSOR.get();
    }
}
