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
import java.util.List;

public interface ISchematic {
    void init(World world, BlockPos pos, IBlockState state, TileEntity tileEntity);

    default boolean isEmpty(){
        return this instanceof EmptySchematic;
    }

    boolean canPlaceBlockAt(IBlockAccess world, BlockPos pos);

    @Nonnull
    NonNullList<ItemStack> computeRequiredItems();

    boolean build(World world, BlockPos blockPos);

    NBTTagCompound serializeNBT(NBTTagCompound compound);

    ISchematic deserializeNBT(NBTTagCompound compound);
}
