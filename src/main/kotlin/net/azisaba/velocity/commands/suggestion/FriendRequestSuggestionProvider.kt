package net.azisaba.velocity.commands.suggestion

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.azisaba.graph.api.PlayersApi
import java.util.concurrent.CompletableFuture

class FriendRequestSuggestionProvider(private val playersApi: PlayersApi) : SuggestionProvider<CommandSource> {
    override fun getSuggestions(
        context: CommandContext<CommandSource>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val player = context.source as? Player ?: return builder.buildFuture()
        return build(builder, player)
    }

    private fun build(
        builder: SuggestionsBuilder,
        player: Player,
        cursor: String? = null,
    ): CompletableFuture<Suggestions> {
        return playersApi.listPlayerFriendRequests(player.uniqueId, 100, cursor, null)
            .thenCompose { response ->
                response.items
                    .map { it.username }
                    .filter { it.startsWith(builder.remaining, ignoreCase = true) }
                    .forEach(builder::suggest)

                if (response.nextCursor != null) {
                    build(builder, player, response.nextCursor)
                } else {
                    builder.buildFuture()
                }
            }
    }
}
