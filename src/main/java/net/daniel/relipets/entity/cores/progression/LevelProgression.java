package net.daniel.relipets.entity.cores.progression;

import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.events.CoreLevelUp;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

@Getter
@Setter
public class LevelProgression {

    public static final String CURRENT_LEVEL_KEY = "current_level";
    public static final String CURRENT_XP_KEY = "current_xp";

    public static final int XP_INCREASE_PER_LEVEL = 1000;
    public static final int BASE_XP_REQUIREMENT = 1000;

    private int currentLevel;
    private int currentXp;
    private int xpRequiredForNextLevel;

    public LevelProgression(int currentLevel, int currentXp){
        this.currentLevel = currentLevel;
        this.currentXp = currentXp;
        this.xpRequiredForNextLevel = BASE_XP_REQUIREMENT * this.currentLevel;
    }

    public LevelProgression(NbtCompound nbt){

        this.currentLevel = nbt.contains(CURRENT_LEVEL_KEY)? nbt.getInt(CURRENT_LEVEL_KEY) : 0;

        this.currentXp = nbt.contains(CURRENT_XP_KEY)? nbt.getInt(CURRENT_XP_KEY): 0;

        this.xpRequiredForNextLevel = BASE_XP_REQUIREMENT * this.currentLevel;
    }

    public boolean checkLevelUp(LivingEntity entity){

        if(this.currentXp >= xpRequiredForNextLevel){
            this.levelUp(1, entity);
            this.currentXp -= xpRequiredForNextLevel;
            this.xpRequiredForNextLevel += XP_INCREASE_PER_LEVEL;
            return true;
        }

        return false;
    }

    public void levelUp(int levels, LivingEntity entity){
        for(int i = 0; i < levels; i ++){
            this.currentLevel++;
            CoreLevelUp.EVENT.invoker().onCoreLevelUp(entity);

        }
    }

    public void receiveXp(int xp){

        this.currentXp += xp;

    }

    public NbtCompound writeToNbt(){
        NbtCompound nbt = new NbtCompound();

        nbt.putInt(CURRENT_LEVEL_KEY, this.currentLevel);
        nbt.putInt(CURRENT_XP_KEY, this.currentXp);

        return nbt;
    }

}
