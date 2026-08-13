package net.azisaba.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import kotlinx.coroutines.future.await
import net.azisaba.graph.api.PlayersApi
import net.azisaba.velocity.util.sendRichMessageWithCustomTags

class PendingFriendRequestListener(private val playersApi: PlayersApi) {
    @Subscribe
    suspend fun onServerPostConnect(event: ServerPostConnectEvent) {
        if (event.previousServer != null) {
            return
        }

        val friendRequests = buildList {
            var cursor: String? = null

            do {
                val response = playersApi.listPlayerFriendRequests(
                    event.player.uniqueId,
                    100,
                    cursor,
                    null,
                ).await()

                addAll(response.items)

                cursor = response.nextCursor
            } while (cursor != null)
        }

        if (friendRequests.isEmpty()) {
            return
        }

        event.player.sendRichMessageWithCustomTags(buildString {
            append("<separator><newline>")
            append("<lang:azisaba.friend.request.pending:'<yellow>${friendRequests.size}</yellow>'><newline>")

            friendRequests.forEach { friendRequest ->
                append("<player:${friendRequest.username}>")
                append("<dark_gray> - </dark_gray>")
                append("<click:run_command:/friend accept ${friendRequest.username}><green>[<lang:azisaba.friend.request.pending.accept>]</green></click>")
                append("<gray> / </gray>")
                append("<click:run_command:/friend deny ${friendRequest.username}><red>[<lang:azisaba.friend.request.pending.reject>]</red></click>")
                append("<newline>")
            }

            append("<separator>")
        })
    }
}
