package net.daniel.relipets.mixin;

import net.daniel.relipets.items.Petificator;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseScrollMixin {
	@Inject(at = @At("HEAD"), method = "onMouseScroll", cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
		// This code is injected into the start of MinecraftServer.loadWorld()V

		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if(player != null && player.isSneaking() && player.getMainHandStack() != null && player.getMainHandStack().getItem() instanceof Petificator){

			//-1 -> scroll down (should go to the right)
			//1 -> scroll up (should go to the left)
			System.out.println("Mouse scrolled from mixin :3" + horizontal + "," + vertical);
			PacketByteBuf data = PacketByteBufs.create();
			data.writeInt((int)vertical);
			ClientPlayNetworking.send(C2SPacketHandlers.CYCLE_PET_SLOT, data);

			info.cancel();

		}

	}
}