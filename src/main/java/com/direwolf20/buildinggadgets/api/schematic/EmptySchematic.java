package com.direwolf20.buildinggadgets.api.schematic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class EmptySchematic implements ISchematic {
    public static final EmptySchematic EMPTY = new EmptySchematic();

    @Override
    public void init(World world, BlockPos pos, IBlockState state, TileEntity tileEntity) { }

    @Override
    public boolean canPlaceBlockAt(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> computeRequiredItems() {
        return NonNullList.create();
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public NBTTagCompound serializeNBT(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public ISchematic deserializeNBT(NBTTagCompound compound) {
        return this;
    }
}
