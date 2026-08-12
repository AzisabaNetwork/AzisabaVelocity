package net.azisaba.velocity.commands

import com.github.shynixn.mccoroutine.velocity.executesSuspend
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import kotlinx.coroutines.future.await
import net.azisaba.velocity.Main
import net.azisaba.velocity.commands.suggestion.FriendRequestSuggestionProvider
import net.azisaba.velocity.commands.suggestion.FriendSuggestionProvider
import net.azisaba.velocity.commands.suggestion.OnlinePlayerSuggestionProvider
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
                            .suggests(OnlinePlayerSuggestionProvider(plugin.server))
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
                            .suggests(FriendSuggestionProvider(plugin.playersApi))
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
                            .suggests(FriendRequestSuggestionProvider(plugin.playersApi))
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
                            .suggests(FriendRequestSuggestionProvider(plugin.playersApi))
                    )
            )
            .then(
                BrigadierCommand.literalArgumentBuilder("list")
                    .executesSuspend(plugin) { context ->
                        listFriends(context.source as Player, 1)
                    }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("page", IntegerArgumentType.integer(1))
                            .executesSuspend(plugin) { context ->
                                listFriends(
                                    context.source as Player,
                                    IntegerArgumentType.getInteger(context, "page"),
                                )
                            }
                    )
            )
            .build()
    }

    private suspend fun addFriend(source: Player, username: String): Int {
        source.sendRichMessage("<gray><lang:azisaba.command.friend.add.requesting>")

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
        }.getOrElse {
            return 0
        }

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.add.requested:'<player:${username}>'><newline><separator>")

        return Command.SINGLE_SUCCESS
    }

    private suspend fun removeFriend(source: Player, username: String): Int {
        source.sendRichMessage("<gray><lang:azisaba.command.friend.remove.removing>")

        val friend = plugin.playersApi.getPlayerByUsernameOrMessage(username, source).await() ?: return 0

        if (plugin.playersApi.listPlayerFriends(
                source.uniqueId,
                1,
                null,
                friend.id,
            ).await().items.none()
        ) {
            source.sendRichMessage("<red><lang:azisaba.command.friend.remove.not-friend>")
            return 0
        }


        runCatchingApiException(source, plugin.logger) {
            plugin.playersApi.removePlayerFriend(source.uniqueId, friend.id).await()
        }.getOrElse {
            return 0
        }

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.remove.removed:'<player:${username}>'><newline><separator>")

        return Command.SINGLE_SUCCESS
    }

    private suspend fun acceptFriendRequest(source: Player, username: String): Int {
        source.sendRichMessage("<gray><lang:azisaba.command.friend.accept.accepting>")

        val sender = plugin.playersApi.getPlayerByUsernameOrMessage(username, source).await() ?: return 0

        if (plugin.playersApi.listPlayerFriendRequests(
                source.uniqueId,
                1,
                null,
                sender.id,
            ).await().items.none()
        ) {
            source.sendRichMessage("<red><lang:azisaba.command.friend.accept.request-not-found>")
            return 0
        }

        runCatchingApiException(source, plugin.logger) {
            plugin.playersApi.acceptPlayerFriendRequest(source.uniqueId, sender.id).await()
        }.getOrElse {
            return 0
        }

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.accept.accepted:'<player:${username}>'><newline><separator>")

        return Command.SINGLE_SUCCESS
    }

    private suspend fun rejectFriendRequest(source: Player, username: String): Int {
        source.sendRichMessage("<gray><lang:azisaba.command.friend.reject.rejecting>")

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
        }.getOrElse {
            return 0
        }

        source.sendRichMessageWithCustomTags("<separator><newline><green><lang:azisaba.command.friend.reject.rejected:'<player:${username}>'></green><newline><separator>")

        return Command.SINGLE_SUCCESS
    }

    private suspend fun listFriends(source: Player, page: Int, friendsPerPage: Int = 8): Int {
        source.sendRichMessage("<gray><lang:azisaba.command.friend.list.loading>")

        require(page > 0) { "page must be positive" }
        require(friendsPerPage > 0) { "friendsPerPage must be positive" }

        var cursor: String?
        var response = runCatchingApiException {
            plugin.playersApi.listPlayerFriends(source.uniqueId, friendsPerPage, null, null).await()
        }.getOrElse {
            return 0
        }

        repeat(page - 1) {
            cursor = response.nextCursor ?: return 0
            response = plugin.playersApi.listPlayerFriends(source.uniqueId, friendsPerPage, cursor, null).await()
        }

        source.sendRichMessageWithCustomTags(buildString {
            append("<separator><newline>")
            append("<lang:azisaba.command.friend.list.title:'${page}'><newline>")

            val friends = response.items + List(friendsPerPage - response.items.size) { null }
            friends.forEach { friend ->
                if (friend != null) {
                    append("<player:${friend.username}>")
                }
                append("<newline>")
            }

            val hasPrevious = page > 1
            val hasNext = response.nextCursor != null

            if (hasPrevious || hasNext) {
                if (hasPrevious) {
                    append("<click:run_command:/friend list ${page - 1}><yellow>\\<\\< <lang:azisaba.command.friend.list.previous></yellow></click>")
                }

                if (hasPrevious && hasNext) {
                    append("<dark_gray> - </dark_gray>")
                }

                if (hasNext) {
                    append("<click:run_command:/friend list ${page + 1}><yellow><lang:azisaba.command.friend.list.next> >></yellow></click>")
                }

                append("<newline>")
            }

            append("<separator>")
        })

        return Command.SINGLE_SUCCESS
    }
}
