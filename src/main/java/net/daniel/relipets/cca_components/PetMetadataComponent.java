package net.daniel.relipets.cca_components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.entity.cores.progression.LevelProgression;
import net.daniel.relipets.entity.cores.progression.UpgradableStats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class PetMetadataComponent implements Component, AutoSyncedComponent, ServerTickingComponent {
    public static final String LEVEL_PROGRESSION_KEY = "level_progression";
    public static final String STAT_UPGRADES_KEY = "stat_upgrades";

    public static final String PLAYER_UUID_KEY = "player_uuid";
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
