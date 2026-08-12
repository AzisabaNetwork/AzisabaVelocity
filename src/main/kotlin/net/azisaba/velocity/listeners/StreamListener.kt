package net.azisaba.velocity.listeners

import kotlinx.coroutines.future.await
import net.azisaba.graph.api.StreamApi
import net.azisaba.graph.model.FriendAddedEvent
import net.azisaba.graph.model.FriendRequestAddedEvent
import net.azisaba.velocity.Main
import net.azisaba.velocity.util.sendRichMessageWithCustomTags

suspend fun listenStreamEvents(plugin: Main, streamApi: StreamApi = plugin.streamApi) {
    streamApi.streamEvents().await().use { events ->
        val iterator = events.iterator()

        while (iterator.hasNext()) {
            val event = iterator.next()

            when (val value = event.actualInstance) {
                is FriendAddedEvent -> onFriendAdded(value, plugin)
                is FriendRequestAddedEvent -> onFriendRequestAdded(value, plugin)
            }
        }
    }
}

private fun onFriendAdded(event: FriendAddedEvent, plugin: Main) {
    plugin.server.getPlayer(event.data.player.id)?.ifPresent { player ->
        player.sendRichMessageWithCustomTags("<green><lang:azisaba.friend.added:'<player:${event.data.friend.username}>'>")
    }
}

private fun onFriendRequestAdded(event: FriendRequestAddedEvent, plugin: Main) {
    plugin.server.getPlayer(event.data.receiver.id).ifPresent { receiver ->
        val senderName = event.data.sender.username

        receiver.sendRichMessageWithCustomTags(
            "<separator><newline>" +
                    "<lang:azisaba.friend.request.added:'<player:${senderName}>'><newline>" +
                    "<click:run_command:/friend accept ${senderName}><green>[<lang:azisaba.friend.request.added.accept>]</green></click>" +
                    "<gray> / </gray>" +
                    "<click:run_command:/friend deny ${senderName}><red>[<lang:azisaba.friend.request.added.reject>]</red></click><newline>" +
                    "<separator>"
        )
    }
}
