package net.daniel.relipets.entity.cores;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.brain.activity.CoreCustomActivities;
import net.daniel.relipets.entity.brain.behavior.CoreFollowPartyOwner;
import net.daniel.relipets.entity.brain.behavior.DoSomethingOnce;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.brain.sensor.CoreOwnerSensor;
import net.daniel.relipets.entity.brain.sensor.FeelLikeDoingSomethingSensor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrain;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.List;
import java.util.Map;

public abstract class BaseCore extends PathAwareEntity implements GeoEntity, SmartBrainOwner<BaseCore> {

    public static final TrackedData<String> CURRENT_ANIM = DataTracker.registerData(BaseCore.class, TrackedDataHandlerRegistry.STRING);

    public BaseCore(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.dataTracker.startTracking(CURRENT_ANIM, "");
    }

    public String getCurrentAnim(){
        return this.dataTracker.get(CURRENT_ANIM);
    }

    public void setCurrentAnim(String animName){
        this.dataTracker.set(CURRENT_ANIM, animName);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if(!this.getWorld().isClient()) tickBrain(this);
    }

    @Override
    protected Brain.Profile<?> createBrainProfile() {
        return new SmartBrainProvider<BaseCore>(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends BaseCore>> getSensors() {
        return List.of(
                new CoreOwnerSensor<>(),
                new FeelLikeDoingSomethingSensor<>()
                        .withChance(0.5f)
                        .affectsMemory(RelipetsMemoryTypes.SHOULD_TEST)
                        .setScanRate((e) -> (int) (Math.random() * 30 + 20))
        );
    }

    @Override
    public BrainActivityGroup<? extends BaseCore> getCoreTasks() {
        CoreFollowPartyOwner coreFollowPartyOwner = new CoreFollowPartyOwner();

        return BrainActivityGroup.coreTasks(coreFollowPartyOwner);
    }

    @Override
    public BrainActivityGroup<? extends BaseCore> getIdleTasks() {
        return BrainActivityGroup.empty();
    }

    @Override
    public BrainActivityGroup<? extends BaseCore> getFightTasks() {
        return BrainActivityGroup.empty();
    }

    @Override
    public Map<Activity, BrainActivityGroup<? extends BaseCore>> getAdditionalTasks() {

        BrainActivityGroup<BaseCore> testActivity = new BrainActivityGroup<BaseCore>(CoreCustomActivities.TEST)
                .requireAndWipeMemoriesOnUse(RelipetsMemoryTypes.SHOULD_TEST)
                .priority(20)
                .behaviours( new DoSomethingOnce().cooldownFor((e)-> (int)(Math.random() * 200 + 180)));


        /*
        BrainActivityGroup<BaseCore> testActivity1 = new BrainActivityGroup<BaseCore>(CoreCustomActivities.TEST)
                .requireAndWipeMemoriesOnUse(RelipetsMemoryTypes.SHOULD_TEST)
                .priority(20)
                .behaviours(
                        new OneRandomBehaviour<BaseCore>(
                                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 200), 5),
                                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 50), 20),
                                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 70), 10),
                                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 70), 10)
                        )
                );
        */

        return Map.of(CoreCustomActivities.TEST, testActivity);
    }

    @Override
    public List<Activity> getActivityPriorities() {
        return List.of(CoreCustomActivities.TEST);
    }

    /*

    How activities and behaviors work:

    A behavior (ExtendedBehavior) will be an action that the entity performs.

    These actions are grouped using an Activity (BrainActivityGroup).

    At any given time, there will only be two activities running: the Core activity and some other one.

    The other activity that will run alongside Core will be decided by the activity priority list returned by
    getActivityPriorities().

    The lib will iterate through the list of activities and try to start their behaviors. The first behavior that
    successfully starts will set the activity that the behavior belongs to as the current activity.

    Apart from just trying to start the behaviors and seeing it they actually start, there's also a pre-check
    for a given activity that is done using getActivityStartMemoryConditions().

    By using getActivityStartMemoryConditions() you don't need to ensure interlocking of behaviors from different
    activities. All you need to do is ensure interlocking of the activities themselves by using the memories. A simple
    way of achieving that would be a "CURRENT_STATE" memory that defines what should be the current activity and then
    you can just use that as a memory condition in getActivityStartMemoryConditions().

    How does it set what activity should be running?

    every tick, it resets the possible activities. This reset is based on whether the entity can perform that
    activity or not. The first activity that the entity can perform becomes the activity that is active at the moment

    So if at tick 1 i was able to perform activity A, but in the next tick i can perform activity B and not A, then it
    will disable activity A and start activity B, without any transition. The activities dont wait for their behavior
    to "complete" before being disabled.

    An important part of deciding which activity will be active at a given time is getActivityPriorities(). What is
    returned from this method will be used to define what are the candidates for being the current activity.
    If the list returns a custom activity, it will try to start that activity. If the activity doesnt have any
    memory requirement, then it will just start it and that activity will always be active. But if it does have a
    memory requirement, then it will check if the requirement is met and if it is, then it activates the activity, but if
    not then it falls back to the default activity defined by getDefaultActivity();

    Inside an activity you can further enhance the degree of control by setting requirements on the behaviors themselves,
    so let's say you want to implement an entity that can randomly feel like doing something and when it feels like so,
    it can choose between a couple of random things that it can do. Upon choosing that random thing that it can do,
    that things enters a cooldown.

    A way to implement that would be having a DO_SOMETHING activity that has the memory requirement of
    FEELING_LIKE_DOING_SOMETHING. Inside that activity we could have a OneRandomBehavior() and pass to it a list of
    things that it could do, like:

    new OneRandomBehaviour<BaseCore>(
                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 200), 5),
                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 50), 20),
                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 70), 10),
                Pair.of(new DoSomethingOnce().cooldownFor((e)-> 70), 10)
        );

    But what if you wanted to make that list dynamic? So that you could apply some sort of reinforcement learning to
    those behaviors or maybe use information about the environemnt (through memories and sensor) to make a behavior
    more or less likely to happen? In that case, you would use a FirstApplicableBehavior() and inside the tryStarting()
    of each behavior you would read data from the memories or the entity itself to determine the chance of it happening
    or not.

    ================ The behavior system ===============================

    So, the behavior system will be based on a map that the entity will store. The map will be String, float. String
    will be the name of the behavior and float will be the chance of that behavior happening. Inside the behavior that
    corresponds to each behavior in the list, they will fetch the chance and use it to determine if it should happen
    or not. If a behavior does occur, the entity will save which behavior it last executed. If you feed the entity a
    treat it will increase the chance of the last behavior happening again.

    The behaviors will be implemented using a FirstApplicationBehaviour() that will be contained within a BEHAVIOR
    activity. This activity should be triggered by the WANTS_TO_BEHAVIOR memory that will be set using the
    FeelLikeBehavingSensor. This sensor will have a random scan rate and a random chance of setting the memory to true.
    There should be a decent amount of time between the trigger of behaviors so that the pet doesnt spam them and also
    the owner has enough time to give the treat to the pet if they want to reinforce that behavior.

    ============== The ability system ==================

    Each ability will have a cooldown. Abilities will be triggered by the pets themselves and the trigger will be based
    on combat-related memories.

    There will be combat abilities and utility abilities.

    Combat abilities should be triggered by combat-related memories, while utility abilities should be triggered by some
    player actions and utility-related memories. There should be sensors for setting those memories.

    Abilities should be usable alongside basic attacks.

     */

    //TODO: add behavior
        // follow owner //DONE
        // stroll around when near owner
        // attack owner's target
        // avenge owner

    //TODO: add leveling capability

    //TODO: add natural spawning

}

/*

Comments from the creator of SmartBrainLib

=========== About what activities are =========

mm no
activities are a layer of complexity that don't have an equal in goals
basically
with goals you basically just list out every possible thing the entity might wanna do, in approximate order
then slap some conditions and priorities on em (mostly)
when you want your entity to stop doing things, you kinda just do a bunch of checks in every goal to make sure it should or shouldn't be doing the thing
super inefficient
with brains comes the concept of activities
activities are groups of behaviours that run together, bundled into.. well.. activities
you're already using activities with SBL by default, SBL just makes them so easy to use you don't realise you're doing it
the way it works is a brain can only run TWO activities at any given time, and one of those will ALWAYS be the CORE activity
which is to say that you have the core activity running, and you have have ONE additional activity running at any given time
usually this is a flip-flop between the FIGHT and IDLE activities
with IDLE running when no other activities are, and FIGHT running when the entity has a target, thne stopping when the target disappears
but you can add additional activities if you want
which supercede fight or whatever
when an activity is running, no behaviours from any other activity (besides CORE) will try to run, and will be automatically stopped

======= About how to add a custom activity =============

just like you override getFightTasks or getIdleTasks
you should override getAdditionalTasks
which allows you to define more activities and their behaviours
I'd recommend using the requireAndWipeMemoriesOnUse method, which defines what memory is used as the starting trigger for when that activity should be active, and auto-wiping it when it stops


 */



/*
Useful sources:

Library for building GUIs

https://docs.wispforest.io/owo/ui/getting-started/

 */

/*

Ideas

What makes a good pet?

    - Being customizable
    - Being able to have it progress alongside you
    - Being visually pleasing
    - Not being annoying
    - Not having to worry about losing it (like it dying permanently or being lost in an unloaded chunk)
    - Having personality (such as behavior, or a consistent visual style)
    - Having some sort of utility, even if minor. If you are using the pet, you should feel like
        you are using the pet, and that it is not just a cosmetic.


======= Pet customization ==============

What should be customizable?

    - Should the player be able to customize the pet model, by adding / removing parts?
    - Should the player be able to customize the colors of the pet model?
    - Should the player be able to customize the size of pet?

====== Pet progression =================

What sort of progression should a player see when playing alongside a pet?

    - The pet should level up with time and depending on some events
    - Leveling up would make the pet stronger
        - More health
        - Less downtime when it "dies"
        - Make their abilities more powerful
    - Pets should be modular and be able to "evolve".
        - Pets are modular creatures.
        - Every pet has a Core. The core is what defines the type of the pet.
        - The core is one of the many parts that a pet can have. You can add parts to the core such as
            legs, arms, wings (hats, maybe?). Those are the part types.

            Initially supported part types:

                - Core: this is the main part of the pet. It defines which type of pet it is. For example, some cores
                    might allow the pet to be rideable.

                - Wings: these are wings that you can add to the pet. The main function is to provide a way for
                    the pet to fly / hover. Allows the player to fly with the pet if they are rideable

                - Legs: defines how the pet will move through the world.

                - Arms: defines what actions the pet can perform by itself or when a player is riding them. For
                    quadruped pets, it doesn't do much.

                - Hats: mostly cosmetic, to help with the fashion.

        - Parts should be craftable and also obtainable as loot

            - crafted parts should have lower attributes, as a way to incentivise exploration while not making it a
                strict requirement.

           - parts give attributes to the pet, on top of adding their special effects. Some parts should be rarer
           than others, but their attributes should not be tied to the pet part, so as not to limit customization if
           a player is looking for min-max.

    - Emotional feedback

        - Pets should have a predefined set of base behaviors. Each behavior should have a way for the player
        to give feedback to the pet about the behavior. For example: if the pet attacks a nearby enemy and the owner
        gives the pet a treat right after that, that should slowly make that behavior more predominant. In this way,
        the owner can steer the personality of the pet through reinforcement learning.

        - As a result of how the base behaviors have been steered, there should be derivative behaviors. For example,
        a pet that has been "trained" to be aggressive towards monsters should develop a derivative behavior of
        attacking allies once at random. (this should be a 1 dmg attack that occurs very fast, so as not to make it a
        problem).

========= Controlling pets =============

Pets should have independent behavior, but also be player controllable. Players control their pets using a single
pet staff. If a player has multiple pets, you should be able to cycle which pet the staff is currently controlling.
You should also be able to retrieve your pet whenever needed.

    - Pets should listen to basic commands such as sit, follow or wander.

    - You should be able to teleport your pets to your location

    - You should be able to recall your pets



============= Implementation plan ==============

    - Implement the parts system
        - Implement each part type
            - the part type should handle the code for (de)serialization
            - figure out how to handle different variants of a given part type
        - Implement a main class that holds each part type
        - Implement a pet entity that holds their "part configuration"
        - Find a way to synchronize the part type data between client and server. Maybe use a CCA component for that?
        - Create a system for rendering the part variants using render layers

    - Implement the part attribute system
        - Add part specific attributes to each part type
        - Create every part variant instance with randomized part attributes

    - Implement pet staff
        - Create model and texture

    - Implement the pet persistence system
        - Save pet data in a CCA component attached to the player
        - Add pet recall/summon feature to the staff using the persistence system.


===================== Handling Animations =================

Animations are started in the Core and are propagated to the parts.

Which means, whenever a core starts playing a given animation, the parts should play the animation that correspond
to the current core animation.

The way to implement that could be the core calling something in the parts, or just the parts pulling the current animation
data from the core. I think the latter is easier to implement because that way the core doesn't have to keep track of
the parts (which are quite dynamic and might be troublesome to sync). Using the pulling system would not require any
synching as the part itself would be responsible for playing the correct animation depending on the state of the core.

How would the parts handle playing the 'correct' animation?

The core would have a synched variable called current_anim. Each part would then read that variable from the core
that they are attached to and play the corresponding part animation for that given core animation. In some cases,
a part would not have a matching animation for a given core animation. In that case, the part should play the idle
animation by default.

What needs to be done in order for that to work then:

    - Define a way to map core animations to part animations (could just use the name of the animation, but a custom mapping
    would be more interesting)
    - Define a way to handle a non-existent mapping and play the default animation then.
    - On top of matching core animations, the parts should be able to have their own animations played as well (such as
    if there was a part that is a halo, and it had to always be rotating)

 */
