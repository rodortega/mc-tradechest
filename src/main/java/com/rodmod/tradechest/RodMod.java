package com.rodmod.tradechest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RodMod implements ModInitializer {
	public static final String MOD_ID = "rodmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Register the trade chest block
	public static final Block TRADE_CHEST_BLOCK = Registry.register(
		BuiltInRegistries.BLOCK,
		new ResourceLocation(MOD_ID, "trade_chest"),
		new TradeChestBlock(BlockBehaviour.Properties.copy(Blocks.CHEST))
	);

	// Register the block entity type
	public static final BlockEntityType<TradeChestBlockEntity> TRADE_CHEST_BLOCK_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		new ResourceLocation(MOD_ID, "trade_chest"),
		BlockEntityType.Builder.of(TradeChestBlockEntity::new, TRADE_CHEST_BLOCK).build(null)
	);

	// Register the trade chest item
	public static final Item TRADE_CHEST = Registry.register(
		BuiltInRegistries.ITEM,
		new ResourceLocation(MOD_ID, "trade_chest"),
		new BlockItem(TRADE_CHEST_BLOCK, new Item.Properties())
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		// Add trade chest to the functional items group
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
			content.accept(TRADE_CHEST);
		});
	}
}