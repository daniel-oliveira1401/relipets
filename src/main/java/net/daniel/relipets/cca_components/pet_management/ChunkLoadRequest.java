package net.daniel.relipets.cca_components.pet_management;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@AllArgsConstructor
@Getter
@Setter
public class ChunkLoadRequest {
    private ServerWorld world;
    private List<Vec2f> chunkCoords;
    private BooleanSupplier action;

    static List<Vec2f> chunkAreaAroundCenterPoint(int centerX, int centerZ, int size) {
        List<Vec2f> chunkCoords = new ArrayList<>();

        int halfSize = size / 2;
        for (int x = centerX - halfSize; x <= centerX + halfSize; x++) {
            for (int z = centerZ - halfSize; z <= centerZ + halfSize; z++) {
                chunkCoords.add(new Vec2f(x, z));
            }
        }

        return chunkCoords;
    }
}
