package net.daniel.relipets.entity;

public class BasePet {
}

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

 */
