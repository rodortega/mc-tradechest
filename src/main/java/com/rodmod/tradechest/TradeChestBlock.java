package com.rodmod.tradechest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TradeChestBlock extends Block implements EntityBlock {
    private static final Component CONTAINER_TITLE = Component.translatable("container.rodmod.trade_chest");

    public TradeChestBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TradeChestBlockEntity tradeChestBE) {
                player.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return CONTAINER_TITLE;
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                        return new TradeChestMenu(syncId, inventory, tradeChestBE, player);
                    }
                });
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TradeChestBlockEntity(pos, state);
    }
}
