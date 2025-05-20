package net.daniel.relipets.utils;

import net.daniel.relipets.Relipets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;

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

    public static int secondToTick(float second){
        return (int)(second * 20);
    }

    public static BlockPos findRandomSafePositionAroundPlayer(ServerWorld world, BlockPos center, int radius, Random random) {
        int maxAttempts = radius * radius * radius; // cube of the radius
        for (int i = 0; i < maxAttempts; i++) {
            int dx = random.nextInt(2 * radius + 1) - radius;
            int dy = random.nextInt(2 * radius + 1) - radius;
            int dz = random.nextInt(2 * radius + 1) - radius;
            BlockPos candidate = center.add(dx, dy, dz);
            if (isSafe(world, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    //TODO: make this safe check safer. (check for entity bounding box)
    public static boolean isSafe(ServerWorld world, BlockPos pos) {

        if (world.getBlockState(pos.down()).isAir()) {
            return false;
        }

        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        VoxelShape shapeAbove = world.getBlockState(pos.up()).getCollisionShape(world, pos.up());
        return shape.isEmpty() && shapeAbove.isEmpty();
    }

}
