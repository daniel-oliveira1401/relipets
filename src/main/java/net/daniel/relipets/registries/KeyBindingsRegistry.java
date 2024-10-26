package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindingsRegistry {

    private static final KeyBinding toggleCurrentPetSummonStateKeyBinding = KeyBindingHelper.registerKeyBinding(
            new DebouncedKeyBinding(
                    KeyBindingsRegistry.formatKeyBindingLangKey("toggle_current_summon"),
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_C,
                    getKeyCategory()

            )
    );

    private static String formatKeyBindingLangKey(String key){
        return "key." + Relipets.MOD_ID + "." + key;
    }

    private static String getKeyCategory(){
        return "Relipets";
    }

    public static void onInitialize(){

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(toggleCurrentPetSummonStateKeyBinding.wasPressed()){
                System.out.println("Pressed toggle key");

                ClientPlayNetworking.send(C2SPacketHandlers.TOGGLE_SUMMON_PET, PacketByteBufs.empty());
            }
        });

    }

    public static class DebouncedKeyBinding extends KeyBinding{

        private final int baseDebounce = 10;
        private int debounceCooldown = 0;

        public DebouncedKeyBinding(String translationKey, int code, String category) {
            super(translationKey, code, category);
        }

        public DebouncedKeyBinding(String translationKey, InputUtil.Type type, int code, String category) {
            super(translationKey, type, code, category);
        }

        public void tickDebounce(){
            this.debounceCooldown = this.debounceCooldown > 0? this.debounceCooldown - 1 : this.debounceCooldown;
        }

        @Override
        public boolean wasPressed() {

            this.tickDebounce();

            boolean pressed = super.wasPressed();

            if(pressed && this.debounceCooldown > 0){
                System.out.println("Debounce is in cooldown. Wait " + this.debounceCooldown);
                return false;
            }

            if(pressed)
                this.debounceCooldown = baseDebounce;

            return pressed;
        }
    }

}
