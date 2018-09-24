package com.direwolf20.buildinggadgets.eventhandlers;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.api.schematic.SchematicFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID)
public class RegistryEventHandler {

    @SubscribeEvent
    public static void onRegistryNewRegistry(RegistryEvent.NewRegistry event) {
        RegistryBuilder<SchematicFactory> builder = new RegistryBuilder<>();
        builder
                .setType(SchematicFactory.class)
                .disableSaving()
                .setName(new ResourceLocation(BuildingGadgets.MODID, "schematic"))
                .disableOverrides()
                .create();
        SchematicFactory.FACTORYS = GameRegistry.findRegistry(SchematicFactory.class);
    }
}
