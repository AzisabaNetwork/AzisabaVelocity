package net.azisaba.velocity.commands

import com.github.shynixn.mccoroutine.velocity.executesSuspend
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import kotlinx.coroutines.future.await
import net.azisaba.velocity.Main
import net.azisaba.velocity.util.getPlayerByUsernameOrMessage
import net.azisaba.velocity.util.runCatchingApiException
import net.azisaba.velocity.util.sendRichMessageWithCustomTags

class FriendCommand(private val plugin: Main) {
    fun build(): LiteralCommandNode<CommandSource> {
        return BrigadierCommand.literalArgumentBuilder("friend")
            .requires { it is Player }
            .then(
                BrigadierCommand.literalArgumentBuilder("add")
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                            .executesSuspend(plugin) { context ->
                                addFriend(
                                    context.source as Player,
                                    StringArgumentType.getString(context, "player"),
                                )
                            }
                    )
            )
            .then(
                BrigadierCommand.literalArgumentBuilder("remove")
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                            .executesSuspend(plugin) { context ->
                                removeFriend(
                                    context.source as Player,
                                    StringArgumentType.getString(context, "player"),
                                )
                            }
                    )
            )
            .then(
                BrigadierCommand.literalArgumentBuilder("accept")
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                            .executesSuspend(plugin) { context ->
                                acceptFriendRequest(
                                    context.source as Player,
                                    StringArgumentType.getString(context, "player"),
                                )
                            }
                    )
            )
            .then(
                BrigadierCommand.literalArgumentBuilder("deny")
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                            .executesSuspend(plugin) { context ->
                                rejectFriendRequest(
                                    context.source as Player,
                                    StringArgumentType.getString(context, "player"),
                                )
                            }
                    )
            )
            .build()
    }

    private suspend fun addFriend(source: Player, username: String): Int {
        val receiver = plugin.playersApi.getPlayerByUsernameOrMessage(username, source).await() ?: return 0

        if (plugin.playersApi.listPlayerFriendRequests(
                receiver.id,
                1,
                null,
                source.uniqueId,
            ).await().items.any()
        ) {
            source.sendRichMessage("<red><lang:azisaba.command.friend.add.already-requested>")
            return 0
        }

        runCatchingApiException(source, plugin.logger) {
            plugin.playersApi.addPlayerFriendRequest(receiver.id, source.uniqueId).await()
            println("done!")
        }.getOrNull() ?: return 0

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.add.requested:'<player:${username}>'><newline><separator>")

        return Command.SINGLE_SUCCESS
    }

    private suspend fun removeFriend(source: Player, username: String): Int {
        val friend = plugin.playersApi.getPlayerByUsernameOrMessage(username, source).await() ?: return 0

        runCatchingApiException(source, plugin.logger) {
            plugin.playersApi.removePlayerFriend(source.uniqueId, friend.id).await()
            println("done")
        }.getOrNull() ?: return 0

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.remove.removed:'<player:${username}>'><newline><separator>")

        return Command.SINGLE_SUCCESS
    }

    private suspend fun acceptFriendRequest(source: Player, username: String): Int {
        val sender = plugin.playersApi.getPlayerByUsernameOrMessage(username, source).await() ?: return 0

        if (plugin.playersApi.listPlayerFriendRequests(
                source.uniqueId,
                1,
                null,
                sender.id,
            ).await().items.none()
        ) {
            source.sendRichMessage("<red><lang:azisaba.command.friend.accept.request-not-found")
            return 0
        }

        runCatchingApiException(source, plugin.logger) {
            plugin.playersApi.acceptPlayerFriendRequest(source.uniqueId, sender.id).await()
        }.getOrNull() ?: return 0

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.accept.accepted:'<player:${username}>'><newline><separator>")

        return Command.SINGLE_SUCCESS
    }

    private suspend fun rejectFriendRequest(source: Player, username: String): Int {
        val sender = plugin.playersApi.getPlayerByUsernameOrMessage(username, source).await() ?: return 0

        if (plugin.playersApi.listPlayerFriendRequests(
                source.uniqueId,
                1,
                null,
                sender.id,
            ).await().items.none()
        ) {
            source.sendRichMessage("<red><lang:azisaba.command.friend.reject.request-not-found>")
            return 0
        }

        runCatchingApiException(source, plugin.logger) {
            plugin.playersApi.rejectPlayerFriendRequest(source.uniqueId, sender.id).await()
        }.getOrNull() ?: return 0

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.reject.rejected:'<player:${username}>'><newline><separator>")

        return Command.SINGLE_SUCCESS
    }
}
