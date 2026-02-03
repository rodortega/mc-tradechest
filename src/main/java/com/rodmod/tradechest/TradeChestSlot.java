package com.rodmod.tradechest;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TradeChestSlot extends Slot {
    private final TradeChestBlockEntity blockEntity;
    private final Player player;

    public TradeChestSlot(Container container, int slot, int x, int y, TradeChestBlockEntity blockEntity, Player player) {
        super(container, slot, x, y);
        this.blockEntity = blockEntity;
        this.player = player;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        // Trigger automatic trade processing when items are placed
        blockEntity.onItemPlaced(this.getContainerSlot());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return blockEntity.canPlaceItem(this.getContainerSlot(), stack, player);
    }

    @Override
    public boolean mayPickup(Player player) {
        return blockEntity.canTakeItem(this.getContainerSlot(), player);
    }
}