package net.azisaba.velocity.util

import net.azisaba.graph.ApiException
import net.azisaba.graph.api.PlayersApi
import net.azisaba.graph.model.Player
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger
import java.util.concurrent.CompletableFuture

fun PlayersApi.getPlayerByUsername(username: String): CompletableFuture<Player> {
    return listPlayers(1, null, username, null, null).thenApply { response ->
        response.items.singleOrNull()
    }
}

fun PlayersApi.getPlayerByUsernameOrMessage(username: String, audience: Audience): CompletableFuture<Player> {
    return getPlayerByUsername(username).thenApply { player ->
        if (player == null) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize("<red><lang:azisaba.error.player-not-found:'${username}'>"))
        }

        player
    }
}

inline fun <T> runCatchingApiException(
    audience: Audience? = null,
    logger: Logger? = null,
    block: () -> T,
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: ApiException) {
        audience?.sendMessage(MiniMessage.miniMessage().deserialize("<red><lang:azisaba.error.api-error>"))
        logger?.error("API error occurred", e)
        Result.failure(e)
    }
}
