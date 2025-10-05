package net.daniel.relipets.registries;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class C2SPayloads {

    // 1
    public record OpenPartManagementScreen(int selectedSlot) implements CustomPayload {
        public static final Id<OpenPartManagementScreen> ID = new Id<>(C2SPacketHandlers.OPEN_PART_MANAGEMENT_SCREEN);
        public static final PacketCodec<RegistryByteBuf, OpenPartManagementScreen> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.selectedSlot()),
            buf -> new OpenPartManagementScreen(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 2
    public record LoadAreaAroundPet(int slot) implements CustomPayload {
        public static final Id<LoadAreaAroundPet> ID = new Id<>(C2SPacketHandlers.LOAD_AREA_AROUND_PET);
        public static final PacketCodec<RegistryByteBuf, LoadAreaAroundPet> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.slot()),
            buf -> new LoadAreaAroundPet(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 3
    public record GetParty() implements CustomPayload {
        public static final Id<GetParty> ID = new Id<>(C2SPacketHandlers.GET_PARTY);
        public static final PacketCodec<RegistryByteBuf, GetParty> CODEC = PacketCodec.of(
            (payload, buf) -> {},
            buf -> new GetParty()
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 4
    public record SelectPet(int slot) implements CustomPayload {
        public static final Id<SelectPet> ID = new Id<>(C2SPacketHandlers.SELECT_PET);
        public static final PacketCodec<RegistryByteBuf, SelectPet> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.slot()),
            buf -> new SelectPet(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 5
    public record UnloadAreaAroundPet(int slot) implements CustomPayload {
        public static final Id<UnloadAreaAroundPet> ID = new Id<>(C2SPacketHandlers.UNLOAD_AREA_AROUND_PET);
        public static final PacketCodec<RegistryByteBuf, UnloadAreaAroundPet> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.slot()),
            buf -> new UnloadAreaAroundPet(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 6
    public record ReleasePet(int slot) implements CustomPayload {
        public static final Id<ReleasePet> ID = new Id<>(C2SPacketHandlers.RELEASE_PET);
        public static final PacketCodec<RegistryByteBuf, ReleasePet> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.slot()),
            buf -> new ReleasePet(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 7
    public record CycleGroupMoveMode(String stringUuid) implements CustomPayload {
        public static final Id<CycleGroupMoveMode> ID = new Id<>(C2SPacketHandlers.CYCLE_GROUP_MOVE_MODE);
        public static final PacketCodec<RegistryByteBuf, CycleGroupMoveMode> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeString(payload.stringUuid()),
            buf -> new CycleGroupMoveMode(buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 8
    public record ChangeMoveMode(int slot) implements CustomPayload {
        public static final Id<ChangeMoveMode> ID = new Id<>(C2SPacketHandlers.CHANGE_MOVE_MODE);
        public static final PacketCodec<RegistryByteBuf, ChangeMoveMode> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.slot()),
            buf -> new ChangeMoveMode(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 9
    public record RecoverPet(int slot) implements CustomPayload {
        public static final Id<RecoverPet> ID = new Id<>(C2SPacketHandlers.RECOVER_PET);
        public static final PacketCodec<RegistryByteBuf, RecoverPet> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.slot()),
            buf -> new RecoverPet(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 10
    public record RenamePet(int slot, String name) implements CustomPayload {
        public static final Id<RenamePet> ID = new Id<>(C2SPacketHandlers.RENAME_PET);
        public static final PacketCodec<RegistryByteBuf, RenamePet> CODEC = PacketCodec.of(
            (payload, buf) -> { buf.writeInt(payload.slot()); buf.writeString(payload.name()); },
            buf -> new RenamePet(buf.readInt(), buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 11
    public record RecallAllPets() implements CustomPayload {
        public static final Id<RecallAllPets> ID = new Id<>(C2SPacketHandlers.RECALL_ALL_PETS);
        public static final PacketCodec<RegistryByteBuf, RecallAllPets> CODEC = PacketCodec.of(
            (payload, buf) -> {},
            buf -> new RecallAllPets()
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 12
    public record ToggleSummonPet(boolean spawnAtPlayerPos) implements CustomPayload {
        public static final Id<ToggleSummonPet> ID = new Id<>(C2SPacketHandlers.TOGGLE_SUMMON_PET);
        public static final PacketCodec<RegistryByteBuf, ToggleSummonPet> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeBoolean(payload.spawnAtPlayerPos()),
            buf -> new ToggleSummonPet(buf.readBoolean())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 13
    public record StatPointChange(String operation, String stat) implements CustomPayload {
        public static final Id<StatPointChange> ID = new Id<>(C2SPacketHandlers.STAT_POINT_CHANGE);
        public static final PacketCodec<RegistryByteBuf, StatPointChange> CODEC = PacketCodec.of(
            (payload, buf) -> { buf.writeString(payload.operation()); buf.writeString(payload.stat()); },
            buf -> new StatPointChange(buf.readString(), buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 14
    public record CreateGroup() implements CustomPayload {
        public static final Id<CreateGroup> ID = new Id<>(C2SPacketHandlers.CREATE_GROUP);
        public static final PacketCodec<RegistryByteBuf, CreateGroup> CODEC = PacketCodec.of(
            (payload, buf) -> {},
            buf -> new CreateGroup()
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 15
    public record RemoveGroup(String stringUuid) implements CustomPayload {
        public static final Id<RemoveGroup> ID = new Id<>(C2SPacketHandlers.REMOVE_GROUP);
        public static final PacketCodec<RegistryByteBuf, RemoveGroup> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeString(payload.stringUuid()),
            buf -> new RemoveGroup(buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 16
    public record SummonGroup(String stringUuid) implements CustomPayload {
        public static final Id<SummonGroup> ID = new Id<>(C2SPacketHandlers.SUMMON_GROUP);
        public static final PacketCodec<RegistryByteBuf, SummonGroup> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeString(payload.stringUuid()),
            buf -> new SummonGroup(buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 17
    public record RecallGroup(String stringUuid) implements CustomPayload {
        public static final Id<RecallGroup> ID = new Id<>(C2SPacketHandlers.RECALL_GROUP);
        public static final PacketCodec<RegistryByteBuf, RecallGroup> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeString(payload.stringUuid()),
            buf -> new RecallGroup(buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 18
    public record ChangeGroupColor(String stringUuid, int color) implements CustomPayload {
        public static final Id<ChangeGroupColor> ID = new Id<>(C2SPacketHandlers.CHANGE_GROUP_COLOR);
        public static final PacketCodec<RegistryByteBuf, ChangeGroupColor> CODEC = PacketCodec.of(
            (payload, buf) -> { buf.writeString(payload.stringUuid()); buf.writeInt(payload.color()); },
            buf -> new ChangeGroupColor(buf.readString(), buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 19
    public record ChangeGroupName(String stringUuid, String name) implements CustomPayload {
        public static final Id<ChangeGroupName> ID = new Id<>(C2SPacketHandlers.CHANGE_GROUP_NAME);
        public static final PacketCodec<RegistryByteBuf, ChangeGroupName> CODEC = PacketCodec.of(
            (payload, buf) -> { buf.writeString(payload.stringUuid()); buf.writeString(payload.name()); },
            buf -> new ChangeGroupName(buf.readString(), buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 20
    public record AddSlotToGroup(String stringUuid, int slotIndex) implements CustomPayload {
        public static final Id<AddSlotToGroup> ID = new Id<>(C2SPacketHandlers.ADD_SLOT_TO_GROUP);
        public static final PacketCodec<RegistryByteBuf, AddSlotToGroup> CODEC = PacketCodec.of(
            (payload, buf) -> { buf.writeString(payload.stringUuid()); buf.writeInt(payload.slotIndex()); },
            buf -> new AddSlotToGroup(buf.readString(), buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 21
    public record RemoveSlotFromGroup(String stringUuid, int slotIndex) implements CustomPayload {
        public static final Id<RemoveSlotFromGroup> ID = new Id<>(C2SPacketHandlers.REMOVE_SLOT_FROM_GROUP);
        public static final PacketCodec<RegistryByteBuf, RemoveSlotFromGroup> CODEC = PacketCodec.of(
            (payload, buf) -> { buf.writeString(payload.stringUuid()); buf.writeInt(payload.slotIndex()); },
            buf -> new RemoveSlotFromGroup(buf.readString(), buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 22
    public record ReorderPets(int originIndex, int destinationIndex) implements CustomPayload {
        public static final Id<ReorderPets> ID = new Id<>(C2SPacketHandlers.REORDER_PETS);
        public static final PacketCodec<RegistryByteBuf, ReorderPets> CODEC = PacketCodec.of(
            (payload, buf) -> { buf.writeInt(payload.originIndex()); buf.writeInt(payload.destinationIndex()); },
            buf -> new ReorderPets(buf.readInt(), buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 23
    public record CyclePetSlot(int direction) implements CustomPayload {
        public static final Id<CyclePetSlot> ID = new Id<>(C2SPacketHandlers.CYCLE_PET_SLOT);
        public static final PacketCodec<RegistryByteBuf, CyclePetSlot> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeInt(payload.direction()),
            buf -> new CyclePetSlot(buf.readInt())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // 24
    public record BoostPetFlight() implements CustomPayload {
        public static final Id<BoostPetFlight> ID = new Id<>(C2SPacketHandlers.BOOST_PET_FLIGHT);
        public static final PacketCodec<RegistryByteBuf, BoostPetFlight> CODEC = PacketCodec.of(
            (payload, buf) -> {},
            buf -> new BoostPetFlight()
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void onInitialize() {
        PayloadTypeRegistry.playC2S().register(OpenPartManagementScreen.ID, OpenPartManagementScreen.CODEC);
        PayloadTypeRegistry.playC2S().register(LoadAreaAroundPet.ID, LoadAreaAroundPet.CODEC);
        PayloadTypeRegistry.playC2S().register(GetParty.ID, GetParty.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectPet.ID, SelectPet.CODEC);
        PayloadTypeRegistry.playC2S().register(UnloadAreaAroundPet.ID, UnloadAreaAroundPet.CODEC);
        PayloadTypeRegistry.playC2S().register(ReleasePet.ID, ReleasePet.CODEC);
        PayloadTypeRegistry.playC2S().register(CycleGroupMoveMode.ID, CycleGroupMoveMode.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeMoveMode.ID, ChangeMoveMode.CODEC);
        PayloadTypeRegistry.playC2S().register(RecoverPet.ID, RecoverPet.CODEC);
        PayloadTypeRegistry.playC2S().register(RenamePet.ID, RenamePet.CODEC);
        PayloadTypeRegistry.playC2S().register(RecallAllPets.ID, RecallAllPets.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleSummonPet.ID, ToggleSummonPet.CODEC);
        PayloadTypeRegistry.playC2S().register(StatPointChange.ID, StatPointChange.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateGroup.ID, CreateGroup.CODEC);
        PayloadTypeRegistry.playC2S().register(RemoveGroup.ID, RemoveGroup.CODEC);
        PayloadTypeRegistry.playC2S().register(SummonGroup.ID, SummonGroup.CODEC);
        PayloadTypeRegistry.playC2S().register(RecallGroup.ID, RecallGroup.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeGroupColor.ID, ChangeGroupColor.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeGroupName.ID, ChangeGroupName.CODEC);
        PayloadTypeRegistry.playC2S().register(AddSlotToGroup.ID, AddSlotToGroup.CODEC);
        PayloadTypeRegistry.playC2S().register(RemoveSlotFromGroup.ID, RemoveSlotFromGroup.CODEC);
        PayloadTypeRegistry.playC2S().register(ReorderPets.ID, ReorderPets.CODEC);
        PayloadTypeRegistry.playC2S().register(CyclePetSlot.ID, CyclePetSlot.CODEC);
        PayloadTypeRegistry.playC2S().register(BoostPetFlight.ID, BoostPetFlight.CODEC);
    }

}
