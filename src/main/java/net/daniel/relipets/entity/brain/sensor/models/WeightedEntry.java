package net.daniel.relipets.entity.brain.sensor.models;

import lombok.Data;

@Data
public class WeightedEntry<T> {

    private T value;
    private int weight;

    public WeightedEntry(T value, int weight){
        this.value = value;
        this.weight = weight;
    }
}
