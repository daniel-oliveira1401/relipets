package net.daniel.relipets.entity.cores.progression;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/***
 * This class is used to represent the current distribution of the point that the pet gets as they level up.
 * The points taken into account here are only the ones "used", which means the sum of the points in here
 * might be less than or equal to the total amount of point that the pet has, but never more.
 *
 * When these stats should be used to modify the base stats:
 *
 *  - Whenever a change occurs to the stats.
 *  - Whenever the entity is summoned
 */
public class UpgradableStats {

    int healthUpgradePoints = 0;

    public UpgradableStats(NbtCompound compound) {
        this.healthUpgradePoints = compound.contains(StatsEnum.HEALTH.name())? compound.getInt(StatsEnum.HEALTH.name()) : 0;
        //this.healthUpgradePoints = 2;
    }

    public void addStatPoint(StatsEnum stat){
        switch (stat){
            case HEALTH:
                this.healthUpgradePoints++;
        }
        this.notifyStatChange();
    }
    public void removeStatPoint(StatsEnum stat){
        switch (stat){
            case HEALTH:
                this.healthUpgradePoints--;
        }
        this.notifyStatChange();
    }

    public int getStatValue(StatsEnum stat){
        switch (stat){
            case HEALTH:
                return this.healthUpgradePoints;
            default:
                return 0;
        }

    }

    public static MobCategoryEnum getEntityCategory(LivingEntity entity){
        //category is decided by health

        if(entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) <= 20){
            return MobCategoryEnum.ORDINARY;
        }else if(entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) <= 40){
            return MobCategoryEnum.KINDA_TANK;
        }else{
            return MobCategoryEnum.TANK;
        }

    }

    public static float getStatScalingByCategory(StatsEnum stat, MobCategoryEnum categoryEnum){

        switch (stat){
            case HEALTH:
                switch (categoryEnum){
                    case ORDINARY:
                        return 2;
                    case KINDA_TANK:
                        return 5;
                    case TANK:
                        return 10;
                    default:
                        return 0;
                }

            default:
                return 0;
        }

    }

    public void notifyStatChange(){

    }

    public int getTotalPointsUsed(){
        return this.healthUpgradePoints;
    }

    public NbtCompound writeToNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt(StatsEnum.HEALTH.name(), this.healthUpgradePoints);

        return nbt;
    }
}
