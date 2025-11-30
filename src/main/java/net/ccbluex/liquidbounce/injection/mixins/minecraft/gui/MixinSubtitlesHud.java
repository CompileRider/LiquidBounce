/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */

package net.ccbluex.liquidbounce.injection.mixins.minecraft.gui;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.SubtitlesHudEntriesEvent;
import net.ccbluex.liquidbounce.injection.mixins.minecraft.gui.custom.MixinSubtitlesHudEntryAccessor;
import net.ccbluex.liquidbounce.integration.theme.component.ComponentManager;
import net.ccbluex.liquidbounce.integration.theme.component.ComponentTweak;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SubtitlesHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(SubtitlesHud.class)
public abstract class MixinSubtitlesHud {

    @Shadow
    @Final
    private List<SubtitlesHud.SubtitleEntry> entries;

    @Inject(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;createNewRootLayer()V", shift = At.Shift.BEFORE),
        cancellable = true
    )
    private void applyTweak(DrawContext context, CallbackInfo ci) {
        if (ComponentManager.isTweakEnabled(ComponentTweak.DISABLE_SUBTITLES_HUD)) {
            ci.cancel();
        }
    }

    @Inject(
        method = "onSoundPlayed",
        at = @At("RETURN")
    )
    private void onSoundPlayed(CallbackInfo ci) {
        if (this.entries.isEmpty()) {
            EventManager.INSTANCE.callEvent(new SubtitlesHudEntriesEvent(List.of()));
            return;
        }

        var array = this.entries.toArray(); // Prevent concurrent modification
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            var it = array[i];
            var subtitleEntry = (SubtitlesHud.SubtitleEntry & MixinSubtitlesHudEntryAccessor) it;
            var sounds = subtitleEntry.getSounds();
            List<SubtitlesHudEntriesEvent.SoundEntry> innerList;
            if (sounds.isEmpty()) {
                innerList = List.of();
            } else {
                var innerArray = sounds.toArray();
                for (int j = 0, innerArrayLength = innerArray.length; j < innerArrayLength; j++) {
                    innerArray[j] = new SubtitlesHudEntriesEvent.SoundEntry(
                        ((SubtitlesHud.SoundEntry) innerArray[j]).location(),
                        ((SubtitlesHud.SoundEntry) innerArray[j]).time()
                    );
                }
                innerList = (List<SubtitlesHudEntriesEvent.SoundEntry>) (List) Arrays.asList(innerArray);
            }
            array[i] = new SubtitlesHudEntriesEvent.Entry(
                subtitleEntry.getText(),
                subtitleEntry.getRange(),
                innerList
            );
        }

        EventManager.INSTANCE.callEvent(
            new SubtitlesHudEntriesEvent((List<SubtitlesHudEntriesEvent.Entry>) (List) Arrays.asList(array))
        );
    }

}
