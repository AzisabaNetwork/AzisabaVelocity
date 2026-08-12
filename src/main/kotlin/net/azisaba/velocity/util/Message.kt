package net.azisaba.velocity.util

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.`object`.ObjectContents

private val miniMessage: MiniMessage = MiniMessage.builder()
    .editTags { builder ->
        builder.tag("separator", ::createSeparatorTag)
        builder.tag("player", ::createPlayerTag)
    }
    .build()

fun CommandSource.sendRichMessageWithCustomTags(message: String) {
    sendMessage(miniMessage.deserialize(message, this))
}

private fun createSeparatorTag(arguments: ArgumentQueue, context: Context): Tag {
    return Tag.selfClosingInserting(Component.text("-".repeat(53), NamedTextColor.BLUE, TextDecoration.STRIKETHROUGH))
}

private fun createPlayerTag(arguments: ArgumentQueue, context: Context): Tag {
    val name = arguments.pop().value()
    return Tag.selfClosingInserting(
        Component.`object`()
            .contents(ObjectContents.playerHead(name))
            .color(NamedTextColor.WHITE)
            .appendSpace()
            .append(Component.text(name, NamedTextColor.AQUA))
            .clickEvent(ClickEvent.runCommand("/profile $name"))
            .hoverEvent(
                HoverEvent.showText(
                    Component.`object`()
                        .contents(ObjectContents.playerHead(name))
                        .appendSpace()
                        .append(Component.text(name, NamedTextColor.GRAY))
                        .appendNewline()
                        .append(Component.translatable("azisaba.player.tooltip", NamedTextColor.YELLOW))
                )
            )
            .build()
    )
}
