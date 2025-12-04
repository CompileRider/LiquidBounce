<!--
  - This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
  -
  - Copyright (c) 2015 - 2025 CCBlueX
  -
  - LiquidBounce is free software: you can redistribute it and/or modify
  - it under the terms of the GNU General Public License as published by
  - the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - LiquidBounce is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  - GNU General Public License for more details.
  -
  - You should have received a copy of the GNU General Public License
  - along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
  -->

<script lang="ts">
    import {listen} from "../../../integration/ws";
    import type {SoundListenerTransform, SubtitlesHudEntry} from "../../../integration/types";
    import {fade} from "svelte/transition";
    import {flip} from "svelte/animate";
    import TextComponent from "../../menu/common/TextComponent.svelte";
    import {crossProduct, dotProduct, minus, normalize} from "../../../util/math_utils";

    let soundListenerTransform: SoundListenerTransform = $state({
        position: {x: 0, y: 0, z: 0},
        forward: {x: 0, y: 0, z: 0},
        up: {x: 0, y: 0, z: 0},
    });
    let audibleEntries: SubtitlesHudEntry[] = $state([]);

    // SoundListenerTransform#right
    const right = (soundListenerTransform: SoundListenerTransform) => crossProduct(soundListenerTransform.forward, soundListenerTransform.up);

    // Handle sound update
    listen("subtitlesHudEntries", (event) => {
        soundListenerTransform = event.soundListenerTransform;
        audibleEntries = event.audibleEntries;
    });
</script>

{#if audibleEntries.length}
    <div class="subtitles-container" transition:fade={{duration: 200}}>
        {#each audibleEntries as entry (entry.text)}
            {@const directions = entry.sounds.map(sound =>
                dotProduct(normalize(minus(sound.location, soundListenerTransform.position)), right(soundListenerTransform)))}
            <div class="subtitles-entry" transition:fade={{duration: 200}} animate:flip={{duration: 200}}>
                {#if directions.some(direction => direction && direction > 0)}
                    <span class="direction">&lt;</span>
                {/if}
                <TextComponent textComponent={entry.text} fontSize={14}/>
                {#if directions.some(direction => direction && direction < 0)}
                    <span class="direction">&gt;</span>
                {/if}
            </div>
        {/each}
    </div>
{/if}

<style lang="scss">
  @use "../../../colors.scss" as *;

  .subtitles-container {
    background-color: $subtitles-base-color;
    border-radius: 5px;
    padding: 10px;
    font-size: 14px;
    display: flex;
    flex-direction: column;
    gap: 2px;
    align-items: center;
    justify-content: center;
  }

  .subtitles-entry {
    display: inline-flex;
    gap: 4px;
    align-items: center;
    justify-content: center;

    .direction {
      color: $subtitles-direction-color;
    }
  }
</style>
