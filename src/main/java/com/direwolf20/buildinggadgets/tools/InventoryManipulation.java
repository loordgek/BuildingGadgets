package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.ModItems;
import com.direwolf20.buildinggadgets.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.items.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InventoryManipulation {
    private static final Set<IProperty> safeProperties = Stream.of(BlockSlab.HALF, BlockStairs.HALF, BlockLog.LOG_AXIS,
            BlockDirectional.FACING, BlockStairs.FACING, BlockTrapDoor.HALF, BlockTorch.FACING, BlockStairs.SHAPE, BlockLever.FACING, BlockLever.POWERED).collect(Collectors.toSet());

    private static final Set<IProperty> safePropertiesCopyPaste = Stream.of(BlockSlab.HALF, BlockStairs.HALF, BlockLog.LOG_AXIS,
            BlockDirectional.FACING, BlockStairs.FACING, BlockTrapDoor.HALF, BlockTorch.FACING, BlockStairs.SHAPE, BlockRail.SHAPE, BlockRailPowered.SHAPE,
            BlockLever.FACING, BlockLever.POWERED).collect(Collectors.toSet());

    public static boolean giveItem(ItemStack itemStack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        if (itemStack.getItem() instanceof ConstructionPaste) {
            itemStack = addPasteToContainer(player, itemStack);
        }
        if (itemStack.getCount() == 0) {
            return true;
        }
        InventoryPlayer inv = player.inventory;
        ArrayList<IItemHandler> invContainers = findInvContainers(inv);
        if (invContainers.size() > 0) {
            for (IItemHandler container : invContainers) {
                for (int i = 0; i < container.getSlots(); i++) {
                    ItemStack containerItem = container.getStackInSlot(i);
                    ItemStack giveItemStack = itemStack.copy();
                    if (containerItem.getItem() == giveItemStack.getItem() && containerItem.getMetadata() == giveItemStack.getMetadata()) {
                        giveItemStack = container.insertItem(i, giveItemStack, false);
                        if (giveItemStack.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        ItemStack giveItemStack = itemStack.copy();
        boolean success = inv.addItemStackToInventory(giveItemStack);
        return success;
    }

    public static NonNullList<ItemStack> getStacksCondensed(IItemHandler itemHandler) {
        NonNullList<ItemStack> itemStacks = CondensedItemStackList.create();
        int bound = itemHandler.getSlots();
        for (int i = 0; i < bound; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemStacks.add(stack.copy());
            }
        }
        return itemStacks;
    }

    public static int containsSets(NonNullList<ItemStack> set, NonNullList<ItemStack> stock) {
        int totalSets = 0;

        for (ItemStack req : set) {

            int reqCount = 0;
            for (ItemStack offer : stock) {
                if (ItemHandlerHelper.canItemStacksStackRelaxed(req, offer)) {
                    int stackCount = offer.getCount() / req.getCount();
                    reqCount = Math.max(reqCount, stackCount);
                }
            }

            if (reqCount == 0) {
                return 0;
            } else if (totalSets == 0) {
                totalSets = reqCount;
            } else if (totalSets > reqCount) {
                totalSets = reqCount;
            }
        }

        return totalSets;
    }

    public static IItemHandler getCombinedContainers(EntityPlayer player){
        List<IItemHandler> handlers = findInvContainers(player.inventory);
        handlers.add(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN));
        return new CombinedInvWrapper(handlers.toArray(new IItemHandler[handlers.size()]));
    }

    public static int removeSets(IItemHandler handler, int min, int max, NonNullList<ItemStack> set, boolean simulate) {
        NonNullList<ItemStack> stock = getStacksCondensed(handler);
        if (!simulate) {
            NonNullList<ItemStack> removed = removeSets(handler, stock, min, max, set);
            return !removed.isEmpty() ? removed.get(0).getCount() / set.get(0).getCount() : 0;
        } else {
            return containsSets(set, stock);
        }
    }

    @Nonnull
    public static NonNullList<ItemStack> removeSets(IItemHandler handler, NonNullList<ItemStack> stock, int min, int max, NonNullList<ItemStack> set) {
        NonNullList<ItemStack> removed = CondensedItemStackList.create();
        int containedSets = containsSets(set, stock);
        if (containedSets < min) {
            return removed;
        }
        for (ItemStack stack : set) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack removedStack = removeStack(handler, itemStack -> ItemHandlerHelper.canItemStacksStack(stack, itemStack), stack.getCount() * Math.min(containedSets, max));
            removed.add(removedStack);
        }
        return removed;
    }

    @Nonnull
    private static ItemStack removeStack(IItemHandler handler, Predicate<ItemStack> filter, int amount) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if (!stackInSlot.isEmpty() && filter.test(stackInSlot)){
                ItemStack removed = handler.extractItem(i, amount, false);
                amount -= removed.getCount();
                if (amount == 0) {
                    return removed;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean useItem(ItemStack itemStack, EntityPlayer player, int count) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        InventoryPlayer inv = player.inventory;

        ArrayList<Integer> slots = findItem(itemStack.getItem(), itemStack.getMetadata(), inv);
        ArrayList<IItemHandler> invContainers = findInvContainers(inv);

        if (invContainers.size() > 0) {
            for (IItemHandler container : invContainers) {
                for (int i = 0; i < container.getSlots(); i++) {
                    ItemStack containerItem = container.getStackInSlot(i);
                    if (containerItem.getItem() == itemStack.getItem() && containerItem.getMetadata() == itemStack.getMetadata() && containerItem.getCount() >= count) {
                        container.extractItem(i, count, false);
                        return true;
                    }
                }
            }
        }
        if (slots.size() == 0) {
            return false;
        }
        int slot = slots.get(0);
        ItemStack stackInSlot = inv.getStackInSlot(slot);
        if (stackInSlot.getCount() < count) {
            return false;
        }
        stackInSlot.shrink(count);
        stackInSlot = stackInSlot;
        return true;
    }

    public static int countItem(ItemStack itemStack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return 10000;
        }
        int count = 0;
        InventoryPlayer inv = player.inventory;
        ArrayList<Integer> slots = findItem(itemStack.getItem(), itemStack.getMetadata(), inv);
        ArrayList<IItemHandler> invContainers = findInvContainers(inv);
        if (slots.size() == 0 && invContainers.size() == 0) {
            return 0;
        }
        if (invContainers.size() > 0) {
            for (IItemHandler container : invContainers) {
                count += countInContainer(container, itemStack.getItem(), itemStack.getMetadata());
            }
        }

        for (int slot : slots) {
            ItemStack stackInSlot = inv.getStackInSlot(slot);
            count += stackInSlot.getCount();
        }
        return count;
    }

    public static int countPaste(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return 10000;
        }
        int count = 0;
        InventoryPlayer inv = player.inventory;
        Item item = ModItems.constructionPaste;
        ArrayList<Integer> slots = findItem(item, 0, inv);
        if (slots.size() > 0) {
            for (int slot : slots) {
                ItemStack stackInSlot = inv.getStackInSlot(slot);
                count += stackInSlot.getCount();
            }
        }
        ArrayList<Integer> containerSlots = findItem(ModItems.constructionPasteContainer, 0, inv);
        if (containerSlots.size() > 0) {
            for (int slot : containerSlots) {
                ItemStack stackInSlot = inv.getStackInSlot(slot);
                if (stackInSlot.getItem() instanceof ConstructionPasteContainer) {
                    count = count + ConstructionPasteContainer.getPasteAmount(stackInSlot);
                }
            }
        }
        return count;
    }

    public static ItemStack addPasteToContainer(EntityPlayer player, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ConstructionPaste)) {
            return itemStack;
        }
        InventoryPlayer inv = player.inventory;
        ArrayList<Integer> slots = findItem(ModItems.constructionPasteContainer, 0, inv);
        if (slots.size() == 0) {
            return itemStack;
        }

        Map<Integer, Integer> slotMap = new HashMap<Integer, Integer>();
        for (int slot : slots) {
            slotMap.put(slot, ConstructionPasteContainer.getPasteAmount(inv.getStackInSlot(slot)));
        }
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(slotMap.entrySet());
        Comparator<Map.Entry<Integer, Integer>> comparator = Comparator.comparing(entry -> entry.getValue());
        comparator = comparator.reversed();
        list.sort(comparator);


        for (Map.Entry<Integer, Integer> entry : list) {
            ItemStack containerStack = inv.getStackInSlot(entry.getKey());
            int pasteInContainer = ConstructionPasteContainer.getPasteAmount(containerStack);
            int freeSpace = ConstructionPasteContainer.maxAmount - pasteInContainer;
            int stackSize = itemStack.getCount();
            int remainingPaste = stackSize - freeSpace;
            if (remainingPaste < 0) {
                remainingPaste = 0;
            }
            int usedPaste = Math.abs(stackSize - remainingPaste);
            itemStack.setCount(remainingPaste);
            ConstructionPasteContainer.setPasteAmount(containerStack, pasteInContainer + usedPaste);
        }
        return itemStack;
    }

    public static boolean usePaste(EntityPlayer player, int count) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        InventoryPlayer inv = player.inventory;
        ArrayList<Integer> slots = findItem(ModItems.constructionPaste, 0, inv);
        if (slots.size() > 0) {
            for (int slot : slots) {
                ItemStack pasteStack = inv.getStackInSlot(slot);
                if (pasteStack.getCount() >= count) {
                    pasteStack.shrink(count);
                    pasteStack = pasteStack;
                    return true;
                }
            }
        }
        ArrayList<Integer> containerSlots = findItem(ModItems.constructionPasteContainer, 0, inv);
        if (containerSlots.size() > 0) {
            for (int slot : containerSlots) {
                ItemStack containerStack = inv.getStackInSlot(slot);
                if (containerStack.getItem() instanceof ConstructionPasteContainer) {
                    int pasteAmt = ConstructionPasteContainer.getPasteAmount(containerStack);
                    if (pasteAmt >= count) {
                        ConstructionPasteContainer.setPasteAmount(containerStack, pasteAmt - count);
                        return true;
                    }

                }
            }
        }
        return false;
    }

    public static ArrayList<IItemHandler> findInvContainers(InventoryPlayer inv) {
        ArrayList<IItemHandler> containers = new ArrayList<IItemHandler>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                containers.add(stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
            }
        }
        return containers;
    }

    public static int countInContainer(IItemHandler container, Item item, int meta) {
        int count = 0;
        ItemStack tempItem;
        for (int i = 0; i < container.getSlots(); ++i) {
            tempItem = container.getStackInSlot(i);
            if (tempItem.getItem() == item && tempItem.getMetadata() == meta) {
                count += tempItem.getCount();
            }
        }
        return count;
    }

    public static ArrayList<Integer> findItem(Item item, int meta, InventoryPlayer inv) {
        ArrayList<Integer> slots = new ArrayList<Integer>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item && meta == stack.getMetadata()) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static ItemStack getSilkTouchDrop(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        int i = 0;
        if (item.getHasSubtypes()) {
            i = state.getBlock().damageDropped(state);
        }
        return new ItemStack(item, 1, i);
    }

    public static IBlockState getSpecificStates(IBlockState originalState, World world, EntityPlayer player, BlockPos pos, ItemStack tool) {
        IBlockState placeState = Blocks.AIR.getDefaultState();
        Block block = originalState.getBlock();
        ItemStack item = block.getPickBlock(originalState, null, world, pos, player);
        int meta = item.getMetadata();
        try {
            placeState = originalState.getBlock().getStateForPlacement(world, pos, EnumFacing.UP, 0, 0, 0, meta, player, EnumHand.MAIN_HAND);
        } catch (Exception var8) {
            placeState = originalState.getBlock().getDefaultState();
        }
        for (IProperty prop : placeState.getPropertyKeys()) {
            if (tool.getItem() instanceof CopyPasteTool) {
                if (safePropertiesCopyPaste.contains(prop)) {
                    placeState = placeState.withProperty(prop, originalState.getValue(prop));
                }
            } else {
                if (safeProperties.contains(prop)) {
                    placeState = placeState.withProperty(prop, originalState.getValue(prop));
                }
            }
        }
        return placeState;

    }

    public static IBlockState getBaseState(IBlockState originalState, World world, EntityPlayer player, BlockPos pos) {
        IBlockState placeState = Blocks.AIR.getDefaultState();
        Block block = originalState.getBlock();
        placeState = originalState.getBlock().getDefaultState();
        return placeState;

    }
}
