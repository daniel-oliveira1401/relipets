package net.daniel.relipets.cca_components.pet_management.progression;

import lombok.Getter;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.cores.YellowCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;

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
@Getter
public class UpgradableStats {

    HashMap<StatsEnum, Integer> stats = new HashMap<>();

    public UpgradableStats(NbtCompound compound) {

        stats.put(StatsEnum.HEALTH, 0);
        stats.put(StatsEnum.HEALTH_REGEN, 0);
        stats.put(StatsEnum.ATTACK, 0);
        stats.put(StatsEnum.ARMOR, 0);
        stats.put(StatsEnum.ARMOR_TOUGHNESS, 0);
        stats.put(StatsEnum.ABILITY_DURATION, 1);
        stats.put(StatsEnum.ABILITY_RANGE, 1);
        stats.put(StatsEnum.ABILITY_STRENGTH, 1);
        stats.put(StatsEnum.MINIMUM_BOOST_SPEED, 1);
        stats.put(StatsEnum.MAXIMUM_BOOST_SPEED, 1);

        stats.replaceAll((key, v) -> compound.contains(key.name()) ? compound.getInt(key.name()) : 0);

    }

    public void addStatPoint(StatsEnum stat){
        int currentValue = stats.get(stat);
        currentValue++;
        stats.put(stat, currentValue);

    }
    public void removeStatPoint(StatsEnum stat){
        int currentValue = stats.get(stat);
        currentValue--;
        stats.put(stat, currentValue);
    }

    public int getStatValue(StatsEnum stat){
        return this.stats.get(stat);

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

    public static boolean entityHasStat(LivingEntity entity, StatsEnum stat){
        return switch (stat){
            case HEALTH -> entity.getAttributes().hasAttribute(EntityAttributes.GENERIC_MAX_HEALTH);
            case HEALTH_REGEN -> true;
            case ATTACK -> entity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            case ARMOR -> entity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ARMOR);
            case ARMOR_TOUGHNESS -> entity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            case ABILITY_RANGE, ABILITY_STRENGTH, ABILITY_DURATION -> entity instanceof BaseCore;
            case MINIMUM_BOOST_SPEED, MAXIMUM_BOOST_SPEED -> entity instanceof YellowCore;
            default -> false;
        };
    }

    public static float getStatScalingByCategory(StatsEnum stat, MobCategoryEnum categoryEnum){

        switch (stat){
            case HEALTH:
                return switch (categoryEnum) {
                    case ORDINARY -> 2;
                    case KINDA_TANK -> 5;
                    case TANK -> 10;
                    default -> 0;
                };

            case HEALTH_REGEN:
                return switch (categoryEnum) {
                    case ORDINARY -> 0.1f;
                    case KINDA_TANK -> 0.2f;
                    case TANK -> 0.3f;
                    default -> 0;
                };

            case ATTACK:
                return switch (categoryEnum) {
                    case ORDINARY -> 0.3f;
                    case KINDA_TANK -> 0.2f;
                    case TANK -> 0.1f;
                    default -> 0;
                };

            case ARMOR_TOUGHNESS:
                return switch (categoryEnum) {
                    case ORDINARY -> 0.2f;
                    case KINDA_TANK -> 0.3f;
                    case TANK -> 0.4f;
                    default -> 0;
                };

            case ARMOR:
                return switch (categoryEnum) {
                    case KINDA_TANK, TANK, ORDINARY -> 0.2f;
                    default -> 0;
                };

            case ABILITY_RANGE, ABILITY_DURATION, ABILITY_STRENGTH:
                return 0.1f;

            case MAXIMUM_BOOST_SPEED, MINIMUM_BOOST_SPEED:
                return 0.05f;

            default:
                return 1;
        }

    }

    public int getTotalPointsUsed(){
        return this.stats.values().stream().reduce(0, Integer::sum);
    }

    public NbtCompound writeToNbt() {
        NbtCompound nbt = new NbtCompound();

        this.stats.forEach((k, v)-> {
            nbt.putInt(k.name(), v);
        });

        return nbt;
    }
}
