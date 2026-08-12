package net.azisaba.velocity.commands.suggestion

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.CompletableFuture

class OnlinePlayerSuggestionProvider(private val server: ProxyServer) : SuggestionProvider<CommandSource> {
    override fun getSuggestions(
        context: CommandContext<CommandSource>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val player = context.source as? Player

        server.allPlayers
            .filter { player == null || it != player }
            .map { it.username }
            .filter { it.startsWith(builder.remaining, ignoreCase = true) }
            .forEach(builder::suggest)

        return builder.buildFuture()
    }
}
