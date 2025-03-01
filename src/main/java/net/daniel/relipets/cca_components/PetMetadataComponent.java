package net.daniel.relipets.cca_components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.progression.LevelProgression;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.UpgradableStats;
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
                return (float) this.getPetEntity().getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            case ARMOR:
                return (float) this.getPetEntity().getAttributeValue(EntityAttributes.GENERIC_ARMOR);
            case ARMOR_TOUGHNESS:
                return (float) this.getPetEntity().getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            case HEALTH_REGEN:
                return pet.getNaturalHealing();

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

    public void clear(){
        this.playerUUID = "";
    }

    int levelProgressionCheckCooldown = 0;
    @Override
    public void serverTick() {
        if(!this.playerUUID.isEmpty()){
            if(levelProgressionCheckCooldown <= 0){

                levelProgressionCheckCooldown = 20;

                tickNaturalXpGain();

                this.levelProgression.checkLevelUp(petEntity);

            }
            levelProgressionCheckCooldown = Math.max(levelProgressionCheckCooldown -1, 0);

        }
    }

    public void tickNaturalXpGain(){
        this.levelProgression.receiveXp(40);
    }
}
