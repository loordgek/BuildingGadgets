package com.direwolf20.buildinggadgets.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Arrays;
import java.util.List;

public class CondensedItemStackList extends NonNullList<ItemStack> {
    public static CondensedItemStackList create()
    {
        return new CondensedItemStackList();
    }

    public static CondensedItemStackList withSize(int size) {
        ItemStack[] aobject = new ItemStack[size];
        Arrays.fill(aobject, ItemStack.EMPTY);
        return new CondensedItemStackList(Arrays.asList(aobject));
    }

    protected CondensedItemStackList() {
        super();
    }

    protected CondensedItemStackList(List<ItemStack> delegateIne) {
        super(delegateIne, ItemStack.EMPTY);
    }

    @Override
    public boolean add(ItemStack stack) {
        if (stack.isStackable()){
            for (ItemStack stack1 : this) {
                if (ItemHandlerHelper.canItemStacksStack(stack, stack1)){
                    stack1.grow(stack.getCount());
                    return true;
                }
            }
        }
        return super.add(stack);
    }

}
