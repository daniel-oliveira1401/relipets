package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.daniel.relipets.registries.S2CPacketHandlers;
import net.daniel.relipets.utils.Utils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PetParty implements ISerializable {

    public static final String SELECTED_PET_INDEX = "selected_pet_index";
    public static final String PET_GROUP_MANAGER = "pet_group_manager";

    private int slotCount = 10;

    @Getter
    int selectedPetIndex = 0;

    @Getter
    private PetSlotManager<PetData> slotManager = new PetSlotManager<>(slotCount, PetData::new);

    PetPartyEventListener onPetPartyModifiedListener;

    @Getter
    private PetGroupManager petGroupManager = new PetGroupManager();

    private PlayerEntity player;
    private static final int baseSlotCount = 10;
    int partyUpdateCooldown = 0;
    int petSummonCooldown = 0;
    int naturalHealingCooldown = 0;

    @Getter
    private SpectatorModeData spectatorModeData;

    @Getter
    private ChunkLoadManager chunkLoadManager = new ChunkLoadManager();

    private final List<PetData> summonedPets = new ArrayList<>();

    private final List<PetSlot<PetData>> slotsWithPets = new ArrayList<>();

    public PetParty(PlayerEntity player){
        this.player = player;
    }

    //int tickCount = 0;
    //long nanos = 0;
    public void tick(ServerWorld world){

        //long startTime = System.nanoTime();

        checkChunkLoadRequests();

        collectSlotsWithPets();

        collectSummonedPets(slotsWithPets);

        tickPets(slotsWithPets, world);

        updateParty(slotsWithPets, world.getServer());

        applyHealingToSummonedPetsIfPossible(summonedPets, world.getServer());

        tickCooldowns();

//        long endTime = System.nanoTime();
//        long duration = endTime - startTime;
//
//        nanos += duration;
//
//        tickCount++;
//
//        if(tickCount > 100){
//            long average = nanos / tickCount;
//            System.out.println("Average tick duration: " + average + " ns (" + (average / 1_000_000.0) + " ms)");
//            nanos = 0;
//            tickCount = 0;
//        }
    }

    private void checkChunkLoadRequests() {

        if(!chunkLoadManager.requests.isEmpty())
            this.chunkLoadManager.processActiveRequestsIfAny();

    }


    private void collectSummonedPets(List<PetSlot<PetData>> slotsWithPets) {
        summonedPets.clear();
        for (PetSlot<PetData> slot : slotsWithPets) {
            PetData data = slot.getContent();
            if (data != null && data.isSummoned()) {
                summonedPets.add(data);
            }
        }
    }

    private void collectSlotsWithPets() {
        slotsWithPets.clear();
        slotsWithPets.addAll(this.getSlotManager().getSlotsWithContent());
    }

    public void tickCooldowns(){
        partyUpdateCooldown = Math.max(partyUpdateCooldown - 1, 0);

        petSummonCooldown = Math.max(petSummonCooldown - 1, 0);

        naturalHealingCooldown = Math.max(naturalHealingCooldown - 1, 0);

    }

    private void updateParty(List<PetSlot<PetData>> slotsWithPets, MinecraftServer server) {
        if(partyUpdateCooldown <= 0){
            for(PetSlot<PetData> slot : slotsWithPets){
                PetData petData = slot.getContent();
                if(petData != null)
                    petData.updateVolatilePetInfoIfPossible(server);
            }
            partyUpdateCooldown = 5;
            onPetPartyModifiedListener.onPetPartyEvent();
        }
    }

    public void tickPets(List<PetSlot<PetData>> slotsWithPets, ServerWorld world){
        for(PetSlot<PetData> slot : slotsWithPets){
            PetData petData = slot.getContent();
            if(petData != null){
                petData.tick(player);
            }

        }
    }

    public void applyHealingToSummonedPetsIfPossible(List<PetData> summonedPets, MinecraftServer server){

        if(naturalHealingCooldown == 0) {
            for(PetData pet : summonedPets){
                if(pet.getPetInfo().getCurrentHealth() < pet.getPetInfo().getMaxHealth()){
                    pet.applyNaturalHealing(server);
                }
            }

            naturalHealingCooldown = 40;
        }
    }

    public void pushChangesToClient(){
        if(player instanceof ServerPlayerEntity serverPlayer){
            this.getSlotManager().getSlotsWithContent().forEach((p)-> {
                PetData petData = p.getContent();
                if(petData != null && petData.isSummoned()){
                    petData.getPetEntityData().saveEntityData(player.getServer());
                }
            });

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeNbt(this.writeToNbt());
            serverPlayer.networkHandler.sendPacket(new CustomPayloadS2CPacket(S2CPacketHandlers.PARTY_UPDATE, buf));
        }
    }

    public void setOnPartyModifiedListener(PetPartyEventListener listener){
        this.onPetPartyModifiedListener = listener;
    }

    private void triggerOnPartyModifiedEvent(){
        if(this.onPetPartyModifiedListener != null)
            this.onPetPartyModifiedListener.onPetPartyEvent();
    }

    public void readFromNbt(NbtCompound nbt){

        this.slotCount = nbt.getInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY);

        this.slotManager = new PetSlotManager<PetData>(this.slotCount, PetData::new);

        this.slotManager.readFromNbt(nbt.getCompound(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY));

        this.selectedPetIndex = nbt.getInt(SELECTED_PET_INDEX);

        this.petGroupManager = new PetGroupManager(nbt.getCompound(PET_GROUP_MANAGER));

        this.spectatorModeData = new SpectatorModeData(nbt.getCompound("spectatorModeData"));
    }

    public NbtCompound writeToNbt(){
        NbtCompound nbt = new NbtCompound();

        NbtCompound slotManagerNbt = slotManager.writeToNbt();

        if(this.slotCount < baseSlotCount) this.slotCount = baseSlotCount;

        nbt.putInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY, this.slotCount);
        nbt.put(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY, slotManagerNbt);
        nbt.putInt(SELECTED_PET_INDEX, this.selectedPetIndex);

        if(this.petGroupManager != null)
            nbt.put(PET_GROUP_MANAGER, this.petGroupManager.writeToNbt());
        if(this.spectatorModeData != null){
            nbt.put("spectatorModeData", this.spectatorModeData.writeToNbt());
        }

        return nbt;
    }

    @Nullable
    public PetData getSelectedPet(){

        return this.getSlotManager().getSlotAt(this.selectedPetIndex).getContent();
    }

    public void setSelectedPetIndex(int index){
        this.selectedPetIndex = index;
        this.onPetPartyModifiedListener.onPetPartyEvent();
        this.pushChangesToClient();
    }

    public void cyclePetSlot(int direction, MinecraftServer server){
        //-1 -> scroll down (should go to the right)
        //1 -> scroll up (should go to the left)

        this.selectedPetIndex -= direction;
        this.selectedPetIndex = Math.min(this.selectedPetIndex, this.getSlotManager().getSlotCount()-1);
        this.selectedPetIndex = Math.max(this.selectedPetIndex, 0);

        Relipets.LOGGER.debug("Selected pet index: " + this.selectedPetIndex);

        if(this.getSelectedPet() != null && this.getSelectedPet().isSummoned()){
            this.getSelectedPet().addHighlight(server);
        }
        this.onPetPartyModifiedListener.onPetPartyEvent();
    }

    public void toggleSummonSelectedPet(ServerWorld world, Vec3d pos, PlayerEntity player){


        PetData selectedPet = this.getSelectedPet();

        if(selectedPet == null){
            Relipets.LOGGER.debug("There is no pet in the selected slot");
            return;
        }

        boolean operationExecuted = false;

        if(selectedPet.isSummoned()){
            selectedPet.recall(this,world, player);
            operationExecuted = true;
        }else if (selectedPet.isRecalled()){
            selectedPet.summon(this,world, pos, player);
            operationExecuted = true;

        }else if(selectedPet.isHealing()){
            Relipets.LOGGER.debug("The selected pet is healing");
            Utils.message(selectedPet.getPetInfo().getPetName() + " is healing. Wait "+ Utils.tickToSecond(selectedPet.getHealingCooldown()) + "s.", player);

        }else{
            Utils.message("Summoned from an unknown state " + selectedPet.getPetInfo().getPetName() + ".", player);
            //create a copy of the pet using the last known state of the pet
            selectedPet.forceSummon(world, pos, player);
        }

        if(operationExecuted){
            player.getItemCooldownManager().set(RelipetsItemRegistry.PETIFICATOR_ITEM.asItem(), 20);
        }

    }

    public void onPetFainted(LivingEntity petEntity, ServerWorld world){
        PetData petData = getPetByEntityUUID(petEntity.getUuidAsString());
        if(petData != null){

            petData.onFaint(this, petEntity, world, player);
            Relipets.LOGGER.debug("Recalled pet that was about to die");
            Utils.message("Pet " + petData.getPetInfo().getPetName() + " fainted! They are healing now...", player);
        }else{
            Relipets.LOGGER.debug("Could not find pet bound to this entity that fainted");
        }
    }

    @Nullable
    public PetData getPetByEntityUUID(String entityUUID){
        for(PetSlot<PetData> slot : this.getSlotManager().getSlotsWithContent()){
            PetData pet = slot.getContent();
            if(pet != null && pet.getPetEntityData().getEntityUUID().equals(entityUUID)){
                return pet;
            }
        }

        return null;
    }

    public void addPetToParty(LivingEntity entity, PlayerEntity player){

        if(this.getSlotManager().getSlotAt(this.selectedPetIndex).isEmpty()){
            PetData newPet = new PetData();
            newPet.fillFromEntity(entity, player);
            this.getSlotManager().getSlotAt(this.selectedPetIndex).setContent(newPet);
            Utils.message("Added " + entity.getDisplayName().getString() + " to party!", player);
            newPet.updateVolatilePetInfoSync(entity);
            newPet.recallEntitySync(entity, newPet, this,(ServerWorld) entity.getWorld(), player);
        }else{

            //search for a slot
            PetSlot<PetData> emptySlot = this.getSlotManager().getFirstEmptySlot();
            if(emptySlot != null){
                PetData newPet = new PetData();
                newPet.fillFromEntity(entity, player);
                emptySlot.setContent(newPet);
                Utils.message("Added " + entity.getDisplayName().getString() + " to party!", player);
                newPet.updateVolatilePetInfoSync(entity);
                newPet.recallEntitySync(entity, newPet, this, (ServerWorld) entity.getWorld(), player);
            }else{
                Utils.message("There's no slot available for this pet. Either craft more slots or free up existing ones.", player);
            }
        }

        Relipets.LOGGER.debug(entity.getDisplayName().getString() + " has been petified!");
        triggerOnPartyModifiedEvent();
        this.pushChangesToClient();

    }

    //TODO: fix this. This is completely broken. It should load the entity and release it (if summoned)
    // if not summoned, it should summon the entity and release it
    public void releasePetFromParty(PetData pet, MinecraftServer server){
        int petIndex = -1;
        int currentIndex = 0;
        for(PetSlot<PetData> slot : this.getSlotManager().getSlots()){
            PetData petData = slot.getContent();
            if(petData != null){
                if(petData.getPetEntityData().getEntityUUID().equals(pet.getPetEntityData().getEntityUUID())){
                    petIndex = currentIndex;
                    break;
                }
            }
            currentIndex++;
        }

        if(petIndex == -1){
            Relipets.LOGGER.debug("Could not find pet in the party. Release failed.");
            return;
        }

        PetData petToBeReleased = this.getSlotManager().getSlotAt(petIndex).getContent();
        if(petToBeReleased != null){
            ServerWorld trackerWorld = petToBeReleased.getPetEntityData().getTracker().getWorld(server);
            //if summoned, load the entity and release it
            if(petToBeReleased.isSummoned()){
                int finalPetIndex = petIndex;
                this.getChunkLoadManager().addRequest(
                        new ChunkLoadRequest(
                                trackerWorld,
                                ChunkLoadRequest.chunkAreaAroundCenterPoint(
                                        petToBeReleased.getPetEntityData().getTracker().getChunkPos().x,
                                        petToBeReleased.getPetEntityData().getTracker().getChunkPos().x,
                                        3
                                ),
                                ()-> {
                                    if(trackerWorld == null) return false;

                                    LivingEntity entity = (LivingEntity) trackerWorld.getEntity(UUID.fromString(petToBeReleased.getPetEntityData().getEntityUUID()));

                                    if(entity != null){

                                        PetMetadataComponent petMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.get(entity);
                                        petMetadata.clearPlayerUUID();

                                        this.getSlotManager().getSlotAt(finalPetIndex).clear();

                                        Utils.message("Released "+ petToBeReleased.getPetInfo().getPetName() + " from party", player);
                                        triggerOnPartyModifiedEvent();
                                        this.pushChangesToClient();

                                        Relipets.LOGGER.debug("Pet released after loading it");

                                        return true;
                                    }

                                    return false;
                                }
                        )
                );
            }
            //if not summoned, summon it and release it
            else{
                petToBeReleased.summonForRelease((ServerWorld) player.getWorld(), player.getPos(), player);
                this.getSlotManager().getSlotAt(petIndex).clear();

                Utils.message("Released "+ petToBeReleased.getPetInfo().getPetName() + " from party", player);
                triggerOnPartyModifiedEvent();
                this.pushChangesToClient();

                Relipets.LOGGER.debug("Pet released");
            }

        }


    }

    public void reorderPets(int originIndex, int destinationIndex) {
        //get the content at origin
        //get the content at destination
        //place the content from origin in destination
        //place the content from destination in origin

        PetData originData = this.getSlotManager().getSlotAt(originIndex).getContent();

        PetData destinationData = this.getSlotManager().getSlotAt(destinationIndex).getContent();

        this.getSlotManager().getSlotAt(destinationIndex).setContent(originData);
        this.getSlotManager().getSlotAt(originIndex).setContent(destinationData);

        this.onPetPartyModifiedListener.onPetPartyEvent();
        this.pushChangesToClient();
    }

    public void addPetSlot() {
        this.slotCount++;
        this.getSlotManager().addSlot();
        this.onPetPartyModifiedListener.onPetPartyEvent();
        this.pushChangesToClient();
    }

    public void recallAllPets(ServerWorld world, PlayerEntity player) {
        this.collectSummonedPets(this.getSlotManager().getSlotsWithContent());
        this.summonedPets.forEach((p)-> p.recall(this, world, player));
    }

    public void summonGroup(UUID uuid, ServerWorld world, Vec3d pos, PlayerEntity player) {
        PetGroup group = this.getPetGroupManager().getGroupById(uuid);
        if(group != null){
            List<Integer> slots = group.getSlots();
            if(!slots.isEmpty()){
                for(int slot : slots){
                    PetData petData = this.getSlotManager().getSlotAt(slot).getContent();

                    if(petData != null){
                        petData.summon(this, world, pos, player);
                    }
                }

                Utils.message("Summoned group "+ group.getName(), player);

            }
        }
    }

    public void recallGroup(UUID uuid, ServerWorld world, Vec3d pos, PlayerEntity player) {
        PetGroup group = this.getPetGroupManager().getGroupById(uuid);
        if(group != null){
            List<Integer> slots = group.getSlots();
            if(!slots.isEmpty()){
                for(int slot : slots){
                    PetData petData = this.getSlotManager().getSlotAt(slot).getContent();

                    if(petData != null){
                        petData.recall(this, world, player);
                    }
                }

                Utils.message("Recalled group "+ group.getName(), player);

            }
        }
    }

    public void renamePet(int slot, String name, MinecraftServer server) {
        PetData pet = this.getSlotManager().getSlotAt(slot).getContent();
        if(pet != null){

            pet.renamePet(name, server);

            this.onPetPartyModifiedListener.onPetPartyEvent();
            this.pushChangesToClient();
        }
    }

    public void cyclePetMoveMode(PetData pet){
        pet.setMoveMode(this.getNextMoveMode(pet.getMoveMode()));
    }

    public void cycleGroupMoveMode(String groupId) {

        PetGroup group = this.petGroupManager.getGroupById(UUID.fromString(groupId));
        if(group != null && !group.getSlots().isEmpty()){
            //get all the slots of the group that have pets on them
            List<PetData> petDataFromGroup = this.petGroupManager.getGroupSlotsWithContent(this, group);
            if(petDataFromGroup.isEmpty()) return;

            //use the first one to determine the current mode
            PetMoveMode currentMode = petDataFromGroup.get(0).getMoveMode();
            PetMoveMode nextMoveMode = this.getNextMoveMode(currentMode);
            //cycle the current mode of all the slots based on the mode of the first one
            petDataFromGroup.forEach((p)-> p.setMoveMode(nextMoveMode));

        }
    }

    public PetMoveMode getNextMoveMode(PetMoveMode currentMoveMode){

        PetMoveMode[] values = PetMoveMode.values();
        int nextIndex = (currentMoveMode.ordinal() == values.length - 1) ? 0 : currentMoveMode.ordinal() + 1;
        return values[nextIndex];

    }

    public void loadAreaAroundPet(int slot, ServerPlayerEntity serverPlayer) {
        PetData petData = this.getSlotManager().getSlotAt(slot).getContent();
        if(petData != null){
            petData.getPetEntityData().loadAreaAroundEntity(serverPlayer.getServer(), this);
            this.spectatorModeData = new SpectatorModeData(
                    serverPlayer.getBlockPos(),
                    serverPlayer.interactionManager.getGameMode(),
                    (ServerWorld) serverPlayer.getWorld(),
                    true,
                    slot
            );
            serverPlayer.changeGameMode(GameMode.SPECTATOR);
            serverPlayer.teleport(
                    petData.getPetEntityData().getTracker().getWorld(serverPlayer.getServer()),
                    petData.getPetEntityData().getTracker().getPosition().getX(),
                    petData.getPetEntityData().getTracker().getPosition().getY(),
                    petData.getPetEntityData().getTracker().getPosition().getZ(),
                    0,0
                    );

            if(!this.spectatorModeData.getOriginalWorld(serverPlayer.getServer()).getRegistryKey().getValue().toString().equals(
                    petData.getPetEntityData().getTracker().getDimension().toString()
            )){
                //player and entity are in different dimensions. Must send a reopen spectator screen packet
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(slot);
                buf.writeNbt(petData.writeToNbt());
                ServerPlayNetworking.send(serverPlayer, S2CPacketHandlers.REOPEN_SPECTATOR_SCREEN, buf);
            }

        }
    }

    public void unloadAreaAroundPet(int slot, ServerPlayerEntity serverPlayer) {

        serverPlayer.teleport(
                this.getSpectatorModeData().getOriginalWorld(serverPlayer.getServer()),
                this.getSpectatorModeData().getOriginalPos().getX(),
                this.getSpectatorModeData().getOriginalPos().getY(),
                this.getSpectatorModeData().getOriginalPos().getZ(),
                0, 0
        );
        serverPlayer.changeGameMode(this.spectatorModeData.getOriginalGameMode());
        this.spectatorModeData.setSpectating(false);

        PetData petData = this.getSlotManager().getSlotAt(slot).getContent();
        if(petData != null){
            petData.getPetEntityData().unloadAreaAroundEntity(serverPlayer.getServer());
        }
    }

    public void recallFollowingPets(ServerWorld world, ServerPlayerEntity player) {
        this.collectSummonedPets(this.getSlotManager().getSlotsWithContent());
        this.summonedPets.stream()
                .filter((p)-> p.getMoveMode() == PetMoveMode.FOLLOWING).forEach((p)-> p.recall(this, world, player));

    }

    public void teleportSelectedPetToBlock(BlockPos blockPos, ServerWorld world) {
        PetData pet = this.getSelectedPet();

        if(pet != null){
            if(pet.isSummoned()){
                pet.getPetEntityData().requestTeleportTo(this, world.getServer(),  blockPos);
            }
        }
    }

    public interface PetPartyEventListener{
        void onPetPartyEvent();
    }

}

/*

What is needed for the pet part management screen:

A way to interact with an entity when they are not summoned.

    - Whenever an interaction with that entity is needed, we create the entity by reading its nbt data.
    - We then perform the interaction needed.
    - Then we finish the interaction by saving the entity to nbt again

 */

