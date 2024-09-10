/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.ids.AECreativeTabIds;
import appeng.api.ids.AEItemIds;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.MainCreativeTab;
import appeng.items.misc.MissingContentItem;
import appeng.items.misc.WrappedGenericStack;
import appeng.items.tools.GuideItem;

/**
 * Internal implementation for the API items
 */
public final class AEItems {
    public static final DeferredRegister.Items DR = DeferredRegister.createItems(AppEng.MOD_ID);

    // spotless:off
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    public static final ItemDefinition<Item> MISSING_CONTENT = item("Missing Content", AEItemIds.MISSING_CONTENT, MissingContentItem::new, null);

    public static final ItemDefinition<Item> TABLET = item("Guide", AEItemIds.GUIDE, p -> new GuideItem(p.stacksTo(1)));

    public static final ItemDefinition<WrappedGenericStack> WRAPPED_GENERIC_STACK = item("Wrapped Generic Stack", AEItemIds.WRAPPED_GENERIC_STACK, WrappedGenericStack::new);

    // spotless:on

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    private static <T extends Item> ColoredItemDefinition<T> createColoredItems(String name,
            Map<AEColor, ResourceLocation> ids,
            BiFunction<Item.Properties, AEColor, T> factory) {
        var colors = new ColoredItemDefinition<T>();
        for (var entry : ids.entrySet()) {
            String fullName;
            if (entry.getKey() == AEColor.TRANSPARENT) {
                fullName = name;
            } else {
                fullName = entry.getKey().getEnglishName() + " " + name;
            }
            colors.add(entry.getKey(), entry.getValue(),
                    item(fullName, entry.getValue(), p -> factory.apply(p, entry.getKey())));
        }
        return colors;
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
            Function<Item.Properties, T> factory) {
        return item(name, id, factory, AECreativeTabIds.MAIN);
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
            Function<Item.Properties, T> factory,
            @Nullable ResourceKey<CreativeModeTab> group) {

        Item.Properties p = new Item.Properties();

        Preconditions.checkArgument(id.getNamespace().equals(AppEng.MOD_ID), "Can only register for AE2");
        var definition = new ItemDefinition<>(name, DR.registerItem(id.getPath(), factory));

        if (Objects.equals(group, AECreativeTabIds.MAIN)) {
            MainCreativeTab.add(definition);
        } else if (group != null) {
            MainCreativeTab.addExternal(group, definition);
        }

        ITEMS.add(definition);

        return definition;
    }
}
