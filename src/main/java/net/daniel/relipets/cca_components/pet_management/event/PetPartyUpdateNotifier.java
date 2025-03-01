package net.daniel.relipets.cca_components.pet_management.event;

import net.daniel.relipets.cca_components.pet_management.PetParty;

import java.util.ArrayList;
import java.util.List;

public class PetPartyUpdateNotifier {

    private static PetPartyUpdateNotifier INSTANCE;

    //store the people subscribed
    private static final List<Subscriber> subscribers = new ArrayList<>();
    //add a way to trigger an event

    //upon event triggering, notify all subscribers

    private PetPartyUpdateNotifier(){}

    public static PetPartyUpdateNotifier getInstance(){
        if(INSTANCE == null)
            INSTANCE = new PetPartyUpdateNotifier();

        return INSTANCE;
    }

    public void emit(PetParty dataToEmit){
        subscribers.forEach((s)-> s.onUpdate(dataToEmit));
    }

    public void subscribe(Subscriber sub){
        subscribers.add(sub);
    }

    public void unsubscribe(Subscriber sub){
        subscribers.remove(sub);
    }

    public interface Subscriber {

        void onUpdate(PetParty data);

    }

}
