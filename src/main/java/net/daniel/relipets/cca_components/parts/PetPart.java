package net.daniel.relipets.cca_components.parts;

import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetPart {

    public static final PetPart EMPTY_ARM_PART = new PetPart("", PetPart.ARM_PART);
    public static final PetPart EMPTY_WING_PART = new PetPart("", PetPart.WING_PART);
    public static final PetPart EMPTY_HEAD_PART = new PetPart("", PetPart.HEAD_PART);
    public static final PetPart EMPTY_LEG_PART = new PetPart("", PetPart.LEG_PART);
    public static final PetPart EMPTY_TORSO_PART = new PetPart("", PetPart.TORSO_PART);
    public static final PetPart EMPTY_TAIL_PART = new PetPart("", PetPart.TAIL_PART);


    public static final String ARM_PART = "arm";
    public static final String WING_PART = "wing";
    public static final String HEAD_PART = "head";
    public static final String LEG_PART = "leg";
    public static final String TAIL_PART = "tail";
    public static final String TORSO_PART = "torso";

    public static final HashMap<String, PetPart> EMPTY_PARTS = new HashMap<>();

    static {
        EMPTY_PARTS.put(ARM_PART, EMPTY_ARM_PART);
        EMPTY_PARTS.put(LEG_PART, EMPTY_LEG_PART);
        EMPTY_PARTS.put(HEAD_PART, EMPTY_HEAD_PART);
        EMPTY_PARTS.put(TAIL_PART, EMPTY_TAIL_PART);
        EMPTY_PARTS.put(TORSO_PART, EMPTY_TORSO_PART);
        EMPTY_PARTS.put(WING_PART, EMPTY_WING_PART);
    }


    public String modelPartId;

    public String partType;

    public Vec3d baseCenterOffset = new Vec3d(-0.5, -0.5, -0.5);

    public PetPart(){

    }

    public boolean isValid(){
        return this.modelPartId != null && !this.modelPartId.isEmpty() && this.partType != null && !this.partType.isEmpty();
    }

    public PetPart(String modelPartId){
        this.modelPartId = modelPartId;
    }

    public PetPart(String modelPartId, String partType){
        this.modelPartId = modelPartId;
        this.partType = partType;
    }

    public NbtCompound writeToNbt (){

        NbtCompound tag = new NbtCompound();

        tag.putString(RelipetsConstantsRegistry.PART_TYPE, this.partType);

        if(this.modelPartId != null)
            tag.putString(RelipetsConstantsRegistry.PART_MODEL_ID, this.modelPartId);

        return tag;
    }

    public static PetPart readFromNbt(NbtCompound tag){
        PetPart emptyPart = new PetPart();
        emptyPart.partType = tag.getString(RelipetsConstantsRegistry.PART_TYPE);
        emptyPart.modelPartId = tag.getString(RelipetsConstantsRegistry.PART_MODEL_ID);

        return emptyPart;
    }

    public static PetPart createFromString(String partVariantName){
        String partVariantNameRegex = "^(?<PartType>[a-z]+)_(?<PartModelId>[a-z_]+)$";
        Matcher matcher = Pattern.compile(partVariantNameRegex).matcher(partVariantName);

        boolean foundMatch = matcher.find();

        String partType = matcher.group("PartType");

        return new PetPart(partVariantName, partType);
    }

    public PetPart getCopy(){
        PetPart copy = new PetPart();
        copy.modelPartId = this.modelPartId;
        copy.partType = this.partType;

        return copy;
    }

}
