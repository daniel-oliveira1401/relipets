package net.daniel.relipets.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetTimeoutManager {

    private static List<Timeout> activeTimeouts = new ArrayList<>();

    public static void tickActiveTimeouts(){
        if(!activeTimeouts.isEmpty()){
            List<Integer> timeoutsToRemove = new ArrayList<>();
            int i = 0;
            for(Timeout timeout : activeTimeouts){
                timeout.timeoutTicks--;
                if(timeout.timeoutTicks <= 0){
                    timeout.callback.execute();
                    timeoutsToRemove.add(i);
                }
                i++;
            }
            if(!timeoutsToRemove.isEmpty()){
                for(int index = timeoutsToRemove.size()-1; index >=0 ; index--){
                    activeTimeouts.remove(timeoutsToRemove.get(index).intValue());
                }
            }
        }
    }

    public static String setTimeout(TimeoutCallback callback, int ticks){
        Timeout t = new Timeout();
        t.callback = callback;
        t.timeoutTicks = ticks;

        t.id = UUID.randomUUID().toString();

        activeTimeouts.add(t);

        return t.id;
    }

    public static boolean cancelTimeout(String timeoutId){
        activeTimeouts = activeTimeouts.stream().filter(t -> !t.id.equals(timeoutId)).toList();

        return true; //TODO: add a check to see whether it was removed or not
    }

    public static boolean forceExecution(String timeoutId){
        boolean found = false;
        for(Timeout tm : activeTimeouts){
            if(tm.id.equals(timeoutId)){
                tm.callback.execute();
                found = true;
            }
        }

        return found;
    }

    private static class Timeout{
        public TimeoutCallback callback;
        int timeoutTicks = 0;
        String id;
    }

    public interface TimeoutCallback{
        void execute();
    }

}
