package net.daniel.relipets.entity.brain.sensor.models;

import lombok.Data;
import lombok.Getter;

@Getter
public class BehaviorDefinition {
    private String name;
    private int duration;

    private BehaviorDefinition(){}

    public static BehaviorDefinition of(String name, int duration){
        BehaviorDefinition behaviorDefinition = new BehaviorDefinition();
        behaviorDefinition.name = name;
        behaviorDefinition.duration = duration;

        return behaviorDefinition;
    }
}
