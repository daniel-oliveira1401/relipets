package net.daniel.relipets.cca_components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.daniel.relipets.cca_components.parts.*;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class PartSystemComponent implements Component, AutoSyncedComponent {

    public PetPart armPart = PetPart.EMPTY_ARM_PART;
    public PetPart headPart = PetPart.EMPTY_HEAD_PART;
    public PetPart legPart = PetPart.EMPTY_LEG_PART;
    public PetPart wingPart = PetPart.EMPTY_WING_PART;
    public PetPart tailPart = PetPart.EMPTY_TAIL_PART;
    public PetPart torsoPart = PetPart.EMPTY_TORSO_PART;


    private final Object provider;

    public PartSystemComponent(Object provider){
        this.provider = provider;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        NbtCompound headPartTag = tag.getCompound(RelipetsConstantsRegistry.HEAD_PART_KEY);
        if(headPartTag != null){
            this.headPart = PetPart.readFromNbt(headPartTag);
        }

        NbtCompound legPartTag = tag.getCompound(RelipetsConstantsRegistry.LEG_PART_KEY);
        if(legPartTag != null){
            this.legPart = PetPart.readFromNbt(legPartTag);
        }

        NbtCompound wingPartTag = tag.getCompound(RelipetsConstantsRegistry.WING_PART_KEY);
        if(wingPartTag != null){
            this.wingPart = PetPart.readFromNbt(wingPartTag);
        }

        NbtCompound armPartTag = tag.getCompound(RelipetsConstantsRegistry.ARM_PART_KEY);
        if(armPartTag != null){
            this.armPart = PetPart.readFromNbt(armPartTag);
        }

        NbtCompound tailPartTag = tag.getCompound(RelipetsConstantsRegistry.TAIL_PART_KEY);
        if(tailPartTag != null){
            this.tailPart = PetPart.readFromNbt(tailPartTag);
        }

        NbtCompound torsoPartTag = tag.getCompound(RelipetsConstantsRegistry.TORSO_PART_KEY);
        if(torsoPartTag != null){
            this.torsoPart = PetPart.readFromNbt(torsoPartTag);
        }

    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if(this.headPart != null && this.headPart.isValid())
            tag.put(RelipetsConstantsRegistry.HEAD_PART_KEY, this.headPart.writeToNbt());

        if(this.armPart != null && this.armPart.isValid())
            tag.put(RelipetsConstantsRegistry.ARM_PART_KEY, this.armPart.writeToNbt());

        if(this.legPart != null && this.legPart.isValid())
            tag.put(RelipetsConstantsRegistry.LEG_PART_KEY, this.legPart.writeToNbt());

        if(this.wingPart != null && this.wingPart.isValid())
            tag.put(RelipetsConstantsRegistry.WING_PART_KEY, this.wingPart.writeToNbt());

        if(this.tailPart != null && this.tailPart.isValid())
            tag.put(RelipetsConstantsRegistry.TAIL_PART_KEY, this.tailPart.writeToNbt());

        if(this.torsoPart != null && this.torsoPart.isValid())
            tag.put(RelipetsConstantsRegistry.TORSO_PART_KEY, this.torsoPart.writeToNbt());
    }

    public void addOrUpdatePart(PetPart part){
        this.setPart(part);
        CardinalComponentsRegistry.PART_SYSTEM_KEY.sync(this.provider);
    }

    public PetPart getPartByType(String partType){
        switch (partType){

            case PetPart.HEAD_PART -> {
                return this.headPart;
            }

            case PetPart.LEG_PART -> {
                return this.legPart;
            }

            case PetPart.TORSO_PART -> {
                return this.torsoPart;
            }

            case PetPart.TAIL_PART -> {
                return this.tailPart;
            }

            case PetPart.WING_PART -> {
                return this.wingPart;
            }

            default -> { return this.armPart; }
        }
    }

    private void setPart(PetPart part){
        switch (part.partType){

            case PetPart.HEAD_PART -> this.headPart = part;

            case PetPart.LEG_PART -> this.legPart = part;

            case PetPart.WING_PART -> this.wingPart = part;

            case PetPart.TORSO_PART -> this.torsoPart = part;

            case PetPart.TAIL_PART -> this.tailPart = part;

            default -> this.armPart = part;
        }
    }

    public boolean hasValidPart(String partType){
        PetPart part = this.getPartByType(partType);

        return part != null && part.isValid();
    }


    public @Nullable PetPart removeNextPart(){

        PetPart partRemoved = null;
        if(this.headPart.isValid()){
            partRemoved = this.headPart.getCopy();
            this.headPart = PetPart.EMPTY_PARTS.get(this.headPart.partType);
        }else if(this.legPart.isValid()){
            partRemoved = this.legPart.getCopy();
            this.legPart = PetPart.EMPTY_PARTS.get(this.legPart.partType);
        }else if(this.armPart.isValid()){
            partRemoved = this.armPart.getCopy();
            this.armPart = PetPart.EMPTY_PARTS.get(this.armPart.partType);
        }else if(this.wingPart.isValid()){
            partRemoved = this.wingPart.getCopy();
            this.wingPart = PetPart.EMPTY_PARTS.get(this.headPart.partType);
        }else if(this.torsoPart.isValid()){
            partRemoved = this.torsoPart.getCopy();
            this.torsoPart = PetPart.EMPTY_PARTS.get(this.headPart.partType);
        }else if(this.tailPart.isValid()){
            partRemoved = this.tailPart.getCopy();
            this.tailPart = PetPart.EMPTY_PARTS.get(this.headPart.partType);
        }

        CardinalComponentsRegistry.PART_SYSTEM_KEY.sync(this.provider);

        return partRemoved;
    }

}
