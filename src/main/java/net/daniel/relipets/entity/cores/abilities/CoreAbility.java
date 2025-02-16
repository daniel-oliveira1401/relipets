package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.utils.Utils;

public interface CoreAbility {

    void start(CoreAbilityStats stats, BaseCore core);

    default void tick(BaseCore core){};

    default void finish(BaseCore core){};

    default AbilityRunningStateEnum getRunningState(){
        return AbilityRunningStateEnum.FINISHED;
    }

    default int getCooldown(BaseCore core){
        return Utils.secondToTick(5);
    }

}
