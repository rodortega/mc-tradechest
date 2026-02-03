package com.rodmod.tradechest;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TradeChestBlockEntity extends BlockEntity implements Container {
    private final ItemStack[] inventory = new ItemStack[27];
    private String ownerName = "";
    private String buyItem = "";
    private int buyAmount = 0;
    private String sellItem = "";
    private int sellAmount = 0;
    private boolean tradeConfigured = false;

    public TradeChestBlockEntity(BlockPos pos, BlockState state) {
        super(RodMod.TRADE_CHEST_BLOCK_ENTITY, pos, state);
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    public void checkForConfiguration() {
        // Check slot 9 for owner paper
        ItemStack ownerItem = inventory[9];
        if (ownerItem.getItem() == Items.PAPER && ownerItem.hasCustomHoverName()) {
            String newOwnerName = ownerItem.getHoverName().getString();
            if (!newOwnerName.equals(ownerName)) {
                RodMod.LOGGER.info("Trade Chest: Owner set to '{}'", newOwnerName);
                ownerName = newOwnerName;
            }
        } else {
            if (!ownerName.isEmpty()) {
                RodMod.LOGGER.info("Trade Chest: Owner cleared");
                ownerName = "";
            }
        }
        
        // Check slot 10 for buy paper
        ItemStack buyItem = inventory[10];
        if (buyItem.getItem() == Items.PAPER && buyItem.hasCustomHoverName()) {
            String buyText = buyItem.getHoverName().getString();
            parseBuyConfig(buyText);
        } else {
            this.buyItem = "";
            this.buyAmount = 0;
        }
        
        // Check slot 11 for sell paper
        ItemStack sellItem = inventory[11];
        if (sellItem.getItem() == Items.PAPER && sellItem.hasCustomHoverName()) {
            String sellText = sellItem.getHoverName().getString();
            parseSellConfig(sellText);
        } else {
            this.sellItem = "";
            this.sellAmount = 0;
        }
        
        tradeConfigured = !ownerName.isEmpty();
        setChanged();
    }

    private void parseBuyConfig(String text) {
        // Format: "10 minecraft:iron_ingot" or "B:10 minecraft:iron_ingot"
        String buyText = text.startsWith("B:") ? text.substring(2).trim() : text;
        String[] parts = buyText.split(" ", 2);
        if (parts.length == 2) {
            try {
                buyAmount = Integer.parseInt(parts[0]);
                buyItem = parts[1];
            } catch (NumberFormatException e) {
                buyAmount = 0;
                buyItem = "";
            }
        }
    }

    private void parseSellConfig(String text) {
        // Format: "5 minecraft:diamond" or "S:5 minecraft:diamond"
        String sellText = text.startsWith("S:") ? text.substring(2).trim() : text;
        String[] parts = sellText.split(" ", 2);
        if (parts.length == 2) {
            try {
                sellAmount = Integer.parseInt(parts[0]);
                sellItem = parts[1];
            } catch (NumberFormatException e) {
                sellAmount = 0;
                sellItem = "";
            }
        }
    }

    private void parseTradeConfig(String text) {
        String[] lines = text.split("\\n");
        if (lines.length >= 3) {
            ownerName = lines[0].trim();
            
            // Parse buy line: "B:10 minecraft:iron_ore"
            if (lines[1].startsWith("B:")) {
                String[] buyParts = lines[1].substring(2).trim().split(" ", 2);
                if (buyParts.length == 2) {
                    try {
                        buyAmount = Integer.parseInt(buyParts[0]);
                        buyItem = buyParts[1];
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
            }
            
            // Parse sell line: "S:5 minecraft:diamond_ore"
            if (lines[2].startsWith("S:")) {
                String[] sellParts = lines[2].substring(2).trim().split(" ", 2);
                if (sellParts.length == 2) {
                    try {
                        sellAmount = Integer.parseInt(sellParts[0]);
                        sellItem = sellParts[1];
                        tradeConfigured = true;
                        setChanged();
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
            }
        }
    }

    public boolean isTradeConfigured() {
        return tradeConfigured;
    }

    public boolean isOwner(Player player) {
        return player.getName().getString().equals(ownerName);
    }

    public String getBuyItem() {
        return buyItem;
    }

    public int getBuyAmount() {
        return buyAmount;
    }

    public String getSellItem() {
        return sellItem;
    }

    public int getSellAmount() {
        return sellAmount;
    }

    public boolean canPlaceItem(int slot, ItemStack stack, Player player) {
        RodMod.LOGGER.info("Trade Chest canPlaceItem: Player '{}' trying to place '{}' in slot {}, Owner: '{}', Configured: {}", 
            player.getName().getString(), stack.getHoverName().getString(), slot, ownerName, tradeConfigured);
            
        // If no owner is set, only allow placing owner paper in slot 18
        if (!tradeConfigured) {
            if (slot == 18) {
                return stack.getItem() == Items.PAPER && inventory[18].isEmpty();
            }
            return false; // Cannot use as normal chest if not configured
        }
        
        // Slots 0-8: Buy items input - restricted to buy item type
        if (slot >= 0 && slot <= 8) {
            if (!buyItem.isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (!itemId.equals(buyItem)) {
                    return false;
                }
            }
            return true;
        }
        
        // Slots 9-17: Pickup zone - cannot place items here
        if (slot >= 9 && slot <= 17) {
            return false;
        }
        
        // Slot 18: Owner paper - only owner can replace
        if (slot == 18) {
            return isOwner(player) && stack.getItem() == Items.PAPER;
        }
        
        // Slots 19-20: Buy/Sell papers - only owner can place
        if (slot == 19 || slot == 20) {
            return isOwner(player) && stack.getItem() == Items.PAPER;
        }
        
        // Slots 21-26: Third row remaining slots - locked
        if (slot >= 21 && slot <= 26) {
            return false;
        }
        
        // Slots 27-35: Owner collection area - only owner can place
        if (slot >= 27 && slot <= 35) {
            return isOwner(player);
        }
        
        // Slots 36-44: Sell items storage - only owner can place sell items
        if (slot >= 36 && slot <= 44) {
            if (!isOwner(player)) {
                return false;
            }
            if (!sellItem.isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                return itemId.equals(sellItem);
            }
            return true;
        }
        
        return false;
    }
    
    private boolean canAcceptMoreBuyItems(int addingCount) {
        if (buyAmount <= 0 || sellAmount <= 0) {
            return true; // No trade configured
        }
        
        // Count current buy items
        int currentBuyItems = 0;
        for (int i = 0; i < 9; i++) {
            if (!inventory[i].isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inventory[i].getItem()).toString();
                if (itemId.equals(buyItem)) {
                    currentBuyItems += inventory[i].getCount();
                }
            }
        }
        
        // Count available sell items
        int availableSellItems = 0;
        for (int i = 18; i < 27; i++) {
            if (!inventory[i].isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inventory[i].getItem()).toString();
                if (itemId.equals(sellItem)) {
                    availableSellItems += inventory[i].getCount();
                }
            }
        }
        
        // Calculate how many trades are possible with available sell items
        int maxPossibleTrades = availableSellItems / sellAmount;
        int maxBuyItemsNeeded = maxPossibleTrades * buyAmount;
        
        // Check if adding these items would exceed what we can trade for
        return (currentBuyItems + addingCount) <= maxBuyItemsNeeded;
    }

    public boolean canTakeItem(int slot, Player player) {
        RodMod.LOGGER.info("Trade Chest canTakeItem: Player '{}' trying to take from slot {}, Owner: '{}'", 
            player.getName().getString(), slot, ownerName);
            
        // If no owner is set, cannot take anything
        if (!tradeConfigured) {
            return false;
        }
        
        // Owner can always take from any slot
        if (isOwner(player)) {
            return true;
        }
        
        // Slots 0-8: Buy items input - buyers can take back their excess buy items
        if (slot >= 0 && slot <= 8) {
            if (!inventory[slot].isEmpty() && !buyItem.isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inventory[slot].getItem()).toString();
                return itemId.equals(buyItem);
            }
            return true;
        }
        
        // Slots 9-17: Pickup zone - anyone can pick up items here
        if (slot >= 9 && slot <= 17) {
            return true;
        }
        
        // Slots 18-20: Configuration papers - only owner can remove
        if (slot >= 18 && slot <= 20) {
            return false;
        }
        
        // Slots 21-26: Third row remaining - locked
        if (slot >= 21 && slot <= 26) {
            return false;
        }
        
        // Slots 27-35: Owner collection area - only owner can take
        if (slot >= 27 && slot <= 35) {
            return false;
        }
        
        // Slots 36-44: Sell items storage - only owner can take
        if (slot >= 36 && slot <= 44) {
            return false;
        }
        
        return false;
    }
    
    // Called after an item is placed to check for automatic trades
    public void onItemPlaced(int slot) {
        if (!tradeConfigured || slot < 0 || slot >= 9) {
            return;
        }
        
        processAutomaticTrades();
    }
    
    private void processAutomaticTrades() {
        if (buyAmount <= 0 || sellAmount <= 0 || buyItem.isEmpty() || sellItem.isEmpty()) {
            return;
        }
        
        // Count buy items in slots 0-8
        int totalBuyItems = 0;
        for (int i = 0; i < 9; i++) {
            if (!inventory[i].isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inventory[i].getItem()).toString();
                if (itemId.equals(buyItem)) {
                    totalBuyItems += inventory[i].getCount();
                }
            }
        }
        
        // Calculate how many complete trades we can make
        int possibleTrades = totalBuyItems / buyAmount;
        if (possibleTrades <= 0) {
            return;
        }
        
        // Try to fulfill trades by moving sell items to pickup zone
        int tradesCompleted = 0;
        for (int trade = 0; trade < possibleTrades; trade++) {
            if (moveSellItemsToPickupZone(sellAmount)) {
                moveBuyItemsToOwnerCollection(buyAmount);
                tradesCompleted++;
                RodMod.LOGGER.info("Completed trade {}: moved {} buy items to owner collection, moved {} sell items to pickup zone", 
                    trade + 1, buyAmount, sellAmount);
            } else {
                break; // No more sell items or pickup zone is full
            }
        }
        
        if (tradesCompleted > 0) {
            setChanged();
        }
    }
    
    private boolean moveSellItemsToPickupZone(int amount) {
        // Find sell items in slots 36-44 (row 5)
        int remaining = amount;
        
        // First, collect sell items from storage
        for (int i = 36; i < 45 && remaining > 0; i++) {
            if (!inventory[i].isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inventory[i].getItem()).toString();
                if (itemId.equals(sellItem)) {
                    int available = inventory[i].getCount();
                    int toTake = Math.min(remaining, available);
                    
                    // Try to place in pickup zone (slots 9-17)
                    if (addToPickupZone(inventory[i].getItem(), toTake)) {
                        inventory[i].shrink(toTake);
                        remaining -= toTake;
                    }
                }
            }
        }
        
        return remaining == 0;
    }
    
    private boolean addToPickupZone(net.minecraft.world.item.Item item, int amount) {
        int remaining = amount;
        
        // Try to add to existing stacks in pickup zone first (slots 9-17)
        for (int i = 9; i < 18 && remaining > 0; i++) {
            if (!inventory[i].isEmpty() && inventory[i].getItem() == item) {
                int canAdd = Math.min(remaining, inventory[i].getMaxStackSize() - inventory[i].getCount());
                if (canAdd > 0) {
                    inventory[i].grow(canAdd);
                    remaining -= canAdd;
                }
            }
        }
        
        // Then try empty slots in pickup zone
        for (int i = 9; i < 18 && remaining > 0; i++) {
            if (inventory[i].isEmpty()) {
                int toPlace = Math.min(remaining, item.getDefaultInstance().getMaxStackSize());
                inventory[i] = new ItemStack(item, toPlace);
                remaining -= toPlace;
            }
        }
        
        return remaining == 0;
    }
    
    private void moveBuyItemsToOwnerCollection(int amount) {
        int remaining = amount;
        
        // Move buy items from slots 0-8 to owner collection slots 27-35
        for (int i = 0; i < 9 && remaining > 0; i++) {
            if (!inventory[i].isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inventory[i].getItem()).toString();
                if (itemId.equals(buyItem)) {
                    int takeFromSlot = Math.min(remaining, inventory[i].getCount());
                    ItemStack movingStack = inventory[i].split(takeFromSlot);
                    
                    // Try to add to owner collection area
                    addToOwnerCollection(movingStack);
                    remaining -= takeFromSlot;
                }
            }
        }
    }
    
    private void addToOwnerCollection(ItemStack stack) {
        int remaining = stack.getCount();
        
        // Try to add to existing stacks in owner collection (slots 27-35)
        for (int i = 27; i < 36 && remaining > 0; i++) {
            if (!inventory[i].isEmpty() && inventory[i].getItem() == stack.getItem()) {
                int canAdd = Math.min(remaining, inventory[i].getMaxStackSize() - inventory[i].getCount());
                if (canAdd > 0) {
                    inventory[i].grow(canAdd);
                    remaining -= canAdd;
                }
            }
        }
        
        // Then try empty slots
        for (int i = 27; i < 36 && remaining > 0; i++) {
            if (inventory[i].isEmpty()) {
                int toPlace = Math.min(remaining, stack.getMaxStackSize());
                inventory[i] = new ItemStack(stack.getItem(), toPlace);
                remaining -= toPlace;
            }
        }
    }

    private boolean canAffordTrade() {
        int totalBuyItems = 0;
        for (int i = 1; i <= 17; i++) {
            ItemStack stack = inventory[i];
            if (!stack.isEmpty()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (itemId.equals(buyItem)) {
                    totalBuyItems += stack.getCount();
                }
            }
        }
        return totalBuyItems >= buyAmount;
    }

    @Override
    public int getContainerSize() {
        return 45;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < inventory.length ? inventory[slot] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot >= 0 && slot < inventory.length && !inventory[slot].isEmpty() && amount > 0) {
            ItemStack result = inventory[slot].split(amount);
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot >= 0 && slot < inventory.length) {
            ItemStack stack = inventory[slot];
            inventory[slot] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.length) {
            inventory[slot] = stack;
            setChanged();
            if (slot >= 9 && slot <= 11) {
                checkForConfiguration();
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this && 
               player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < inventory.length; i++) {
            if (tag.contains("Item" + i)) {
                inventory[i] = ItemStack.of(tag.getCompound("Item" + i));
            }
        }
        ownerName = tag.getString("Owner");
        buyItem = tag.getString("BuyItem");
        buyAmount = tag.getInt("BuyAmount");
        sellItem = tag.getString("SellItem");
        sellAmount = tag.getInt("SellAmount");
        tradeConfigured = tag.getBoolean("TradeConfigured");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < inventory.length; i++) {
            if (!inventory[i].isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                inventory[i].save(itemTag);
                tag.put("Item" + i, itemTag);
            }
        }
        tag.putString("Owner", ownerName);
        tag.putString("BuyItem", buyItem);
        tag.putInt("BuyAmount", buyAmount);
        tag.putString("SellItem", sellItem);
        tag.putInt("SellAmount", sellAmount);
        tag.putBoolean("TradeConfigured", tradeConfigured);
    }
}
