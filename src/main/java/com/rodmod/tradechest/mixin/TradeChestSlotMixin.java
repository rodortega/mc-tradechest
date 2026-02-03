package com.rodmod.tradechest.mixin;

import com.rodmod.tradechest.TradeChestBlockEntity;
import com.rodmod.tradechest.TradeChestMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class TradeChestSlotMixin {
    @Shadow @Final public Container container;

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void onMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (container instanceof TradeChestBlockEntity blockEntity) {
            Slot slot = (Slot) (Object) this;
            // We need to get the player somehow - for now we'll allow placement and check on pickup
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void onMayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (container instanceof TradeChestBlockEntity blockEntity) {
            Slot slot = (Slot) (Object) this;
            if (!blockEntity.canTakeItem(slot.getContainerSlot(), player)) {
                cir.setReturnValue(false);
            }
        }
    }
}
