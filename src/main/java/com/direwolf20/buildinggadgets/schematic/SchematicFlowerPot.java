package com.direwolf20.buildinggadgets.schematic;

import com.direwolf20.buildinggadgets.api.schematic.ISchematic;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class SchematicFlowerPot implements ISchematic {
    @Nonnull
    private ItemStack flowerStack = ItemStack.EMPTY;

    @Override
    public void init(World world, BlockPos pos, IBlockState state, TileEntity tileEntity) {
        this.flowerStack = ((TileEntityFlowerPot) tileEntity).getFlowerItemStack();
    }

    @Override
    public boolean canPlaceBlockAt(IBlockAccess world, BlockPos pos) {
        IBlockState downState = world.getBlockState(pos.down());
        return (downState.isSideSolid(world, pos, EnumFacing.UP) || downState.getBlockFaceShape(world, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID);
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> computeRequiredItems() {
        NonNullList<ItemStack> stacks = NonNullList.create();
        stacks.add(new ItemStack(Items.FLOWER_POT));

        if (!flowerStack.isEmpty())
            stacks.add(flowerStack);

        return stacks;
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        IBlockState blockState = Blocks.FLOWER_POT.getDefaultState();
        world.setBlockState(blockPos, blockState);
        TileEntityFlowerPot flowerPot = (TileEntityFlowerPot) world.getTileEntity(blockPos);
        if (flowerPot != null && (!flowerStack.isEmpty())) {
            flowerPot.setItemStack(flowerStack);
            flowerPot.markDirty();
        }
        world.notifyBlockUpdate(blockPos, blockState, blockState, 3);
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT(NBTTagCompound compound) {
        if (!flowerStack.isEmpty())
            flowerStack.writeToNBT(compound);

        return compound;
    }

    @Override
    public ISchematic deserializeNBT(NBTTagCompound compound) {
        if (compound.hasKey("id")) {
            this.flowerStack = new ItemStack(compound);
        }
        return this;
    }
}
