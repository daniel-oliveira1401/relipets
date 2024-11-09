package net.daniel.relipets.cca_components.pet_management;

import net.daniel.relipets.cca_components.ISerializable;
import org.jetbrains.annotations.Nullable;

public class PetSlot<T extends ISerializable> {

    private T content;

    @Nullable
    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public boolean isEmpty(){
        return content == null;
    }

    public void clear(){
        this.content = null;
    }
}
