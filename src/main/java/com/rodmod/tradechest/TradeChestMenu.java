package com.rodmod.tradechest;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TradeChestMenu extends AbstractContainerMenu {
    private final TradeChestBlockEntity blockEntity;
    private final Player player;

    public TradeChestMenu(int syncId, Inventory playerInventory, Container inventory, Player player) {
        super(MenuType.GENERIC_9x5, syncId);
        this.blockEntity = inventory instanceof TradeChestBlockEntity ? (TradeChestBlockEntity) inventory : null;
        this.player = player;
        
        // Add chest slots with custom slot logic (9x5 = 45 slots)
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 9; ++j) {
                int slotIndex = j + i * 9;
                this.addSlot(new TradeChestSlot(inventory, slotIndex, 8 + j * 18, 18 + i * 18, blockEntity, player));
            }
        }

        // Add player inventory slots (moved down to accommodate 5 rows)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 120 + i * 18));
            }
        }

        // Add player hotbar slots
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 178));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity != null) {
            return blockEntity.stillValid(player);
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Disable shift-clicking for now to keep it simple
        return ItemStack.EMPTY;
    }
}
