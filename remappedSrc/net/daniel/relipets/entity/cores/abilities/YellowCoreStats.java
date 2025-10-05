package net.daniel.relipets.entity.cores.abilities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

@Getter
@Setter
@AllArgsConstructor
public class YellowCoreStats {

    public static final String MIN_BOOST_SPEED = "min_boost_speed";
    public static final String MAX_BOOST_SPEED = "max_boost_speed";

    private float minBoostSpeed;
    private float maxBoostSpeed;

    public YellowCoreStats(NbtCompound nbt){
        this.minBoostSpeed = nbt.contains(MIN_BOOST_SPEED)? nbt.getFloat(MIN_BOOST_SPEED) : 0.5f;
        this.maxBoostSpeed = nbt.contains(MAX_BOOST_SPEED)? nbt.getFloat(MAX_BOOST_SPEED) : 1.0f;
    }

    public NbtElement writeToNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat(MIN_BOOST_SPEED, this.getMinBoostSpeed());
        nbt.putFloat(MAX_BOOST_SPEED, this.getMaxBoostSpeed());

        return nbt;
    }
}
