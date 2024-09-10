/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.core;

import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import appeng.client.gui.style.StyleManager;
import appeng.client.guidebook.Guide;
import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.command.GuidebookStructureCommands;
import appeng.client.guidebook.compiler.TagCompiler;
import appeng.client.guidebook.extensions.ConfigValueTagExtension;
import appeng.client.guidebook.hotkey.OpenGuideHotkey;
import appeng.client.guidebook.screen.GlobalInMemoryHistory;
import appeng.client.guidebook.screen.GuideScreen;

/**
 * Client-specific functionality.
 */
@Mod(value = AppEng.MOD_ID, dist = Dist.CLIENT)
public class AppEngClient extends AppEngBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppEngClient.class);

    private static AppEngClient INSTANCE;

    /**
     * This modifier key has to be held to activate mouse wheel items.
     */
    private static final KeyMapping MOUSE_WHEEL_ITEM_MODIFIER = new KeyMapping(
            "key.ae2.mouse_wheel_item_modifier", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT, "key.ae2.category");

    private final Guide guide;

    public AppEngClient(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        modEventBus.addListener(this::registerHotkeys);

        guide = createGuide(modEventBus);
        OpenGuideHotkey.init();

        modEventBus.addListener(this::clientSetup);

        INSTANCE = this;

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ConfigurationScreen(container, parent));
    }

    private Guide createGuide(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent evt) -> {
            var server = evt.getServer();
            var dispatcher = server.getCommands().getDispatcher();
            GuidebookStructureCommands.register(dispatcher);
        });

        return Guide.builder(modEventBus, MOD_ID, "ae2guide")
                .extension(TagCompiler.EXTENSION_POINT, new ConfigValueTagExtension())
                .build();
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    private void registerHotkeys(RegisterKeyMappingsEvent e) {
        e.register(OpenGuideHotkey.getHotkey());
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                postClientSetup(minecraft);
            } catch (Throwable e) {
                LOGGER.error("AE2 failed postClientSetup", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Called when other mods have finished initializing and the client is now available.
     */
    private void postClientSetup(Minecraft minecraft) {
        StyleManager.initialize(minecraft.getResourceManager());
    }

    public boolean shouldAddParticles(RandomSource r) {
        return switch (Minecraft.getInstance().options.particles().get()) {
            case ALL -> true;
            case DECREASED -> r.nextBoolean();
            case MINIMAL -> false;
        };
    }

    @Override
    public HitResult getCurrentMouseOver() {
        return Minecraft.getInstance().hitResult;
    }

    @Override
    public void openGuideAtPreviousPage(ResourceLocation initialPage) {
        try {
            var screen = GuideScreen.openAtPreviousPage(guide, PageAnchor.page(initialPage),
                    GlobalInMemoryHistory.INSTANCE);

            openGuideScreen(screen);
        } catch (Exception e) {
            LOGGER.error("Failed to open guide.", e);
        }
    }

    @Override
    public void openGuideAtAnchor(PageAnchor anchor) {
        try {
            var screen = GuideScreen.openNew(guide, anchor, GlobalInMemoryHistory.INSTANCE);

            openGuideScreen(screen);
        } catch (Exception e) {
            LOGGER.error("Failed to open guide at {}.", anchor, e);
        }
    }

    private static void openGuideScreen(GuideScreen screen) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            screen.setReturnToOnClose(minecraft.screen);
        }

        minecraft.setScreen(screen);
    }

    public Guide getGuide() {
        return guide;
    }
}
