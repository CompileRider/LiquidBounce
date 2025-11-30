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

import type {PlayerData, Vec3} from "../integration/types";

export const distanceSq = (pos1: Vec3, pos2: Vec3): number =>
    (pos1.x - pos2.x) ** 2 + (pos1.y - pos2.y) ** 2 + (pos1.z - pos2.z) ** 2;

export const distance = (pos1: Vec3, pos2: Vec3): number =>
    Math.sqrt(distanceSq(pos1, pos2));

/**
 * @param centerX center (zero) X
 * @param centerZ center (zero) Z
 * @param yaw yaw in degrees. positive-Z = 0 deg.
 * @param targetX target (object) X
 * @param targetZ target (object) Z
 * @return angle difference in radians
 */
const calculateRelativeDirection = (centerX: number, centerZ: number, yaw: number, targetX: number, targetZ: number) => {
    const dx = targetX - centerX;
    const dz = targetZ - centerZ;
    const targetAngleRad = Math.atan2(-dx, dz);

    // -\pi to \pi
    let angleDiff = targetAngleRad - yaw * Math.PI / 180;

    // normalize
    angleDiff = (angleDiff + 2 * Math.PI) % (2 * Math.PI);

    return angleDiff;
}

export const calculatePlayerRelativeDirection = (playerData?: PlayerData, position?: Vec3) => {
    if (playerData && position) {
        return calculateRelativeDirection(playerData.position.x, playerData.position.z, playerData.yaw, position.x, position.z);
    } else {
        return undefined;
    }
}
