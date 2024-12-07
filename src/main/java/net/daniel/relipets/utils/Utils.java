package net.daniel.relipets.utils;

import net.daniel.relipets.Relipets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

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

    public static void message(String message, PlayerEntity player){
        player.sendMessage(Text.of(message));
    }

    public static float tickToSecond(int ticks){
        return ticks / 20.0f;
    }

}
