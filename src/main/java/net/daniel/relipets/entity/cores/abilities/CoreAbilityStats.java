package net.daniel.relipets.entity.cores.abilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

@Getter
@Setter
@AllArgsConstructor
public class CoreAbilityStats {

    public static final String DURATION_KEY = "duration";
    public static final String EFFICIENCY_KEY = "efficiency";
    public static final String RANGE_KEY = "range";
    public static final String STRENGTH_KEY = "strength";

    private float abilityDuration = 1.0f;
    private float abilityEfficiency = 1.0f;
    private float abilityRange = 1.0f;
    private float abilityStrength = 1.0f;

    public CoreAbilityStats(NbtCompound nbt){
        this.abilityDuration = nbt.contains(DURATION_KEY)? nbt.getFloat(DURATION_KEY) : 1.0f;
        this.abilityEfficiency = nbt.contains(EFFICIENCY_KEY)? nbt.getFloat(EFFICIENCY_KEY) : 1.0f;
        this.abilityRange = nbt.contains(RANGE_KEY)? nbt.getFloat(RANGE_KEY) : 1.0f;
        this.abilityStrength = nbt.contains(STRENGTH_KEY)? nbt.getFloat(STRENGTH_KEY) : 1.0f;
    }

    public NbtElement writeToNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat(DURATION_KEY, this.getAbilityDuration());
        nbt.putFloat(RANGE_KEY, this.getAbilityDuration());
        nbt.putFloat(STRENGTH_KEY, this.getAbilityDuration());
        nbt.putFloat(EFFICIENCY_KEY, this.getAbilityDuration());

        return nbt;
    }
}
