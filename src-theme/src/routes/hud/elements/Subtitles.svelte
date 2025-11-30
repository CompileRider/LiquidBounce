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
    import type {PlayerData, TextComponent as TTextComponent, Vec3} from "../../../integration/types";
    import {fade} from "svelte/transition";
    import {flip} from "svelte/animate";
    import TextComponent from "../../menu/common/TextComponent.svelte";
    import {onMount} from "svelte";
    import {getPlayerData} from "../../../integration/rest";

    let audibleEntries: {
        text: TTextComponent | string;
        sounds: {
            location: Vec3;
            time: number;
        }[];
    }[] = [];

    listen("subtitlesHudEntries", ({entries}) => {
        audibleEntries = [];
        audibleEntries.push(...entries.filter(it => it.sounds.length));
    });

    let playerData: PlayerData | null = null;

    listen("clientPlayerData", ({playerData}) => {
        playerData = playerData;
    });

    onMount(() => getPlayerData().then(data => playerData = data));
</script>

{#if audibleEntries.length}
    <div class="subtitles-container">
        {#each audibleEntries as entry (entry.text)}
            <div class="subtitles-entry" transition:fade={{duration: 200}} animate:flip={{duration: 200}}>
                <TextComponent textComponent={entry.text} fontSize={14}/>
            </div>
        {/each}
    </div>
{/if}

<style lang="scss">
  .subtitles-container {
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 5px;
    padding: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .subtitles-entry {
    display: inline-flex;
    gap: 2px;
    align-items: center;
    justify-content: center;
  }
</style>
