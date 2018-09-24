package com.direwolf20.buildinggadgets.api.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SchematicFactory extends IForgeRegistryEntry.Impl<SchematicFactory> {
    public static IForgeRegistry<SchematicFactory> FACTORYS;
    private final Supplier<ISchematic> schematicSupplier;

    public SchematicFactory(Supplier<ISchematic> schematicSupplier) {
        this.schematicSupplier = schematicSupplier;
    }

    public Supplier<ISchematic> getSchematicSupplier() {
        return schematicSupplier;
    }

    @Nullable
    public ISchematic get(){
        return getSchematicSupplier().get();
    }

    @Nonnull
    public static ISchematic getSchematic(Block block){
        SchematicFactory factory = FACTORYS.getValue(block.getRegistryName());
        return factory != null ? factory.get() : EmptySchematic.EMPTY;
    }

    @Nonnull
    public static ISchematic getSchematic(IBlockState state){
        return getSchematic(state.getBlock());
    }
}
