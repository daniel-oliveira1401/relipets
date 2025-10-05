package net.daniel.relipets.cca_components;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.progression.LevelProgression;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.UpgradableStats;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.cores.YellowCore;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PetMetadataComponent implements Component, AutoSyncedComponent, ServerTickingComponent {
    public static final String LEVEL_PROGRESSION_KEY = "level_progression";
    public static final String STAT_UPGRADES_KEY = "stat_upgrades";

    public static final String PLAYER_UUID_KEY = "player_uuid";

    @Getter
    LivingEntity petEntity;

    @Getter
    @Setter
    String playerUUID = "";

    @Getter
    private LevelProgression levelProgression;

    @Getter
    private UpgradableStats statUpgrades;

    public PetMetadataComponent(LivingEntity pet){
        this.petEntity = pet;
    }

    public PetMetadataComponent(NbtCompound nbt){
        this.readFromNbt(nbt);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if(tag.contains(PLAYER_UUID_KEY))
            this.playerUUID = tag.getString(PLAYER_UUID_KEY);

        this.levelProgression = new LevelProgression(tag.getCompound(LEVEL_PROGRESSION_KEY));
        this.statUpgrades = new UpgradableStats(tag.getCompound(STAT_UPGRADES_KEY));
    }

    public float getCurrentValueByStatName(StatsEnum stat, PetData pet){
        switch (stat){
            case HEALTH:
                return (float) this.getPetEntity().getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
            case ATTACK:
                float attack = (float)this.getPetEntity().getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

                return  attack;
            case ARMOR:
                return (float) this.getPetEntity().getAttributeValue(EntityAttributes.GENERIC_ARMOR);
            case ARMOR_TOUGHNESS:
                return (float) this.getPetEntity().getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            case HEALTH_REGEN:
                return pet.getNaturalHealing();

            case ABILITY_RANGE:
                if(this.getPetEntity() instanceof BaseCore core){
                    return core.getAbilityStats().getAbilityRange();
                }

                return 0;

            case ABILITY_DURATION:
                if(this.getPetEntity() instanceof BaseCore core){
                    return core.getAbilityStats().getAbilityDuration();
                }

                return 0;

            case ABILITY_STRENGTH:
                if(this.getPetEntity() instanceof BaseCore core){
                    return core.getAbilityStats().getAbilityStrength();
                }

                return 0;

            case MINIMUM_BOOST_SPEED:
                if(this.getPetEntity() instanceof YellowCore core){
                    return core.getYellowCoreStats().getMinBoostSpeed();
                }

                return 0;

            case MAXIMUM_BOOST_SPEED:
                if(this.getPetEntity() instanceof YellowCore core){
                    return core.getYellowCoreStats().getMaxBoostSpeed();
                }

                return 0;

            default: return 0;
        }
    }

    @Nullable
    public PetOwnerComponent getOwner(){
        PlayerEntity player = this.getPetEntity().getWorld().getPlayerByUuid(UUID.fromString(this.playerUUID));
        if(player != null){
            Optional<PetOwnerComponent> owner = CardinalComponentsRegistry.PET_OWNER_KEY.maybeGet(player);

            if(owner.isPresent()){
                return owner.get();
            }
        }

        return null;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putString(PLAYER_UUID_KEY, this.playerUUID);
        if(this.levelProgression != null)
            tag.put(LEVEL_PROGRESSION_KEY, this.levelProgression.writeToNbt());

        if(this.statUpgrades != null)
            tag.put(STAT_UPGRADES_KEY, this.statUpgrades.writeToNbt());
    }

    public void clearPlayerUUID(){
        this.playerUUID = "";
    }

    int levelProgressionCheckCooldown = 0;
    @Override
    public void serverTick() {
        if(!this.playerUUID.isEmpty()){
            if(levelProgressionCheckCooldown <= 0){

                levelProgressionCheckCooldown = 20;

                tickNaturalXpGain();

                if(this.levelProgression != null)
                    this.levelProgression.checkLevelUp(petEntity);

            }
            levelProgressionCheckCooldown = Math.max(levelProgressionCheckCooldown -1, 0);

        }
    }

    public void tickNaturalXpGain(){
        if(this.levelProgression != null){
            this.levelProgression.receiveXp(40);
        }
    }
}
