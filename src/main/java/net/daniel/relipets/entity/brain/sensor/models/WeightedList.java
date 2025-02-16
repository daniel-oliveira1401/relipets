package net.daniel.relipets.entity.brain.sensor.models;

import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
public class WeightedList<T>{

    private List<WeightedEntry<T>> entries = new ArrayList<>();
    private int totalWeight;

    @Nullable
    public WeightedEntry<T> pickRandom(){
        this.calculateWeight();

        int randomNumber = (int)(Math.random() * totalWeight);

        WeightedEntry<T> selectedEntry = null;

        for(WeightedEntry<T> entry : entries){
            randomNumber -= entry.getWeight();

            if(randomNumber <= 0){
                selectedEntry = entry;
                break;
            }
        }

        return selectedEntry;
    }

    public WeightedList<T> addEntry(WeightedEntry<T> entry){
        this.entries.add(entry);
        this.calculateWeight();

        return this;
    }
    public WeightedList<T> addEntry(T value, int weight){
        this.entries.add(new WeightedEntry<>(value, weight));
        this.calculateWeight();

        return this;
    }

    public void removeEntryByObject(WeightedEntry<T> entry){
        this.entries.remove(entry);
        this.calculateWeight();
    }

    public <T> void removeEntryByIndex(int index){
        this.entries.remove(index);
        this.calculateWeight();
    }

    private void calculateWeight(){
        totalWeight = 0;
        entries.forEach((e)-> totalWeight+= e.getWeight());
    }

}
