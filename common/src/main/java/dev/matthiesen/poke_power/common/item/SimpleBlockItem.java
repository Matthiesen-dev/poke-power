package dev.matthiesen.poke_power.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class SimpleBlockItem extends BlockItem {
    public SimpleBlockItem(Block block) {
        super(block, new Properties());
    }
}
