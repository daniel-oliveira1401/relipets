package net.daniel.relipets.entity.brain.sensor;

import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.function.Supplier;

public class RelipetsSensorTypes {

    public static final Supplier<SensorType<CoreOwnerSensor<?>>> CORE_OWNER_SENSOR = register("core_owner_sensor", CoreOwnerSensor::new);
    public static final Supplier<SensorType<FeelLikeDoingSomethingSensor<?>>> FEEL_LIKE_DOING_SOMETHING = register("do_something", FeelLikeDoingSomethingSensor::new);
    public static final Supplier<SensorType<FightWithOwnerSensor>> FIGHT_WITH_OWNER = register("fight_with_owner", FightWithOwnerSensor::new);
    public static final Supplier<SensorType<FollowOwnerSensor>> FOLLOW_OWNER_SENSOR = register("follow_owner", FollowOwnerSensor::new);

    private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
        return SBLConstants.SBL_LOADER.registerSensorType(id, sensor);
    }

    public static void init(){}

}
