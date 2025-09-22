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

package net.ccbluex.liquidbounce.features.command.commands.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.api.core.HttpClient
import net.ccbluex.liquidbounce.api.core.HttpMethod
import net.ccbluex.liquidbounce.api.core.parse
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandException
import net.ccbluex.liquidbounce.features.command.CommandExecutor.suspendHandler
import net.ccbluex.liquidbounce.features.command.builder.CommandBuilder
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.features.command.dsl.addParam
import net.ccbluex.liquidbounce.features.command.dsl.buildCommand
import net.ccbluex.liquidbounce.features.command.dsl.cast
import net.ccbluex.liquidbounce.features.command.dsl.castVararg
import net.ccbluex.liquidbounce.features.command.preset.pagedQuery
import net.ccbluex.liquidbounce.render.FontManager
import net.ccbluex.liquidbounce.utils.client.asText
import net.ccbluex.liquidbounce.utils.client.bold
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.copyable
import net.ccbluex.liquidbounce.utils.client.logger
import net.ccbluex.liquidbounce.utils.client.regular
import net.ccbluex.liquidbounce.utils.client.variable
import net.ccbluex.liquidbounce.utils.client.withColor
import net.minecraft.util.Formatting
import java.io.File
import java.io.InputStream
import java.net.URI

/**
 * Fonts Command
 *
 * Browse and add fonts.
 */
object CommandFonts : Command.Factory {

    override fun createCommand(): Command =
        CommandBuilder.begin("fonts")
            .hub()
            .subcommand(addSubcommand())
            .subcommand(listSubcommand())
            .build()

    private val URI_PREFIX_LIST = listOf("file:///", "http://", "https://")

    private fun addSubcommand() = buildCommand("add") {
        // TODO: add system/file/url xxxx
        val source = addParam("source") {
            verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedFrom { URI_PREFIX_LIST }
                .required()
                .vararg()
        }

        suspendHandler {
            fun loaded(fontFace: FontManager.FontFace) {
                chat("Loaded font: ${fontFace.name}", command)
            }

            suspend fun load(file: File) {
                val fontFace = FontManager.queueFontFromFile(file) ?: throw CommandException("Failed to load font from file ${file.absolutePath}, check log for details".asText())
                loaded(fontFace)
            }

            val source = source.castVararg().joinToString(" ")
            val uri = try {
                URI(source)
            } catch (e: Exception) {
                // Invalid URI -> try as file
                var file = File(source)
                if (!file.isAbsolute) file = file.relativeTo(ConfigSystem.rootFolder)
                load(file)
                return@suspendHandler
            }

            try {
                when (uri.scheme) {
                    "file" -> {
                        val file = File(uri)
                        load(file)
                    }

                    "http", "https" ->
                        withContext(Dispatchers.IO) {
                            HttpClient.request(
                                url = uri.toASCIIString(),
                                method = HttpMethod.GET,
                            ).parse<InputStream>().use { stream ->
                                loaded(FontManager.queueFontFromStream(stream))
                            }
                        }

                    else -> throw CommandException(
                        text = "Unknown font source scheme: ${uri.scheme}, You can use URI starts with: ${
                            URI_PREFIX_LIST.joinToString(
                                ", "
                            )
                        }.".asText(),
                        usageInfo = URI_PREFIX_LIST
                    )
                }
            } catch (e: CommandException) {
                throw e
            } catch (e: Exception) {
                logger.warn("Failed to load font from URL ${uri.toASCIIString()}", e)
                throw CommandException("Failed to load font from URL ${uri.toASCIIString()}, check log for details".asText(), e)
            }
        }
    }

    private fun listSubcommand() = CommandBuilder
        .begin("list")
        .pagedQuery(
            pageSize = 8,
            header = {
                result("fonts").withColor(Formatting.RED).bold(true)
            },
            items = {
                FontManager.fontFaces.values
            },
            eachRow = { _, font ->
                "\u2B25 ".asText()
                    .formatted(Formatting.BLUE)
                    .append(variable(font.name).copyable())
                    .append(regular(" ("))
                    .append(variable(font.size.toString()))
                    .append(regular(")"))

                // TODO: link to file
            }
        )

}
