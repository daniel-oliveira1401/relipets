package net.daniel.relipets.entity.brain.sensor;

import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.function.Supplier;

public class RelipetsSensorTypes {

    public static final Supplier<SensorType<CoreOwnerSensor<?>>> CORE_OWNER_SENSOR = register("core_owner_sensor", CoreOwnerSensor::new);

    private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
        return SBLConstants.SBL_LOADER.registerSensorType(id, sensor);
    }

    public static void init(){}

}
