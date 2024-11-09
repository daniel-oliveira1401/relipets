package net.daniel.relipets.utils;

import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class Utils {

    public static String serializeBlockPos(BlockPos blockPos){
        return blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }

    public static BlockPos deserializeBlockPos(String serializedBlockPos){

        String[] coords = serializedBlockPos.split(",");

        return new BlockPos(
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2])
        );

    }

    public static String setTimeout(SetTimeoutManager.TimeoutCallback callback, int ticks){
        return SetTimeoutManager.setTimeout(callback, ticks);
    }

}
