package net.azisaba.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.PlayerSettingsChangedEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import kotlinx.coroutines.future.await
import net.azisaba.graph.api.PlayersApi
import net.azisaba.graph.model.UpdatePlayerByIdRequest

class PlayerStateSyncListener(private val playersApi: PlayersApi) {
    @Subscribe
    suspend fun onPostLogin(event: PostLoginEvent) {
        playersApi.updatePlayerById(
            event.player.uniqueId,
            UpdatePlayerByIdRequest()
                .status(UpdatePlayerByIdRequest.StatusEnum.ONLINE)
                .currentLocale(event.player.effectiveLocale?.toLanguageTag())
                .currentClientVersion(event.player.protocolVersion.versionIntroducedIn)
        ).await()
    }

    @Subscribe
    suspend fun onDisconnect(event: DisconnectEvent) {
        playersApi.updatePlayerById(
            event.player.uniqueId,
            UpdatePlayerByIdRequest()
                .status(UpdatePlayerByIdRequest.StatusEnum.OFFLINE)
                .currentServer(null)
                .currentLocale(null)
                .currentClientVersion(null)
        ).await()
    }

    @Subscribe
    suspend fun onPlayerSettingsChanged(event: PlayerSettingsChangedEvent) {
        playersApi.updatePlayerById(
            event.player.uniqueId,
            UpdatePlayerByIdRequest()
                .currentLocale(event.player.effectiveLocale?.toLanguageTag())
        ).await()
    }

    @Subscribe
    suspend fun onServerPostConnect(event: ServerPostConnectEvent) {
        playersApi.updatePlayerById(
            event.player.uniqueId,
            UpdatePlayerByIdRequest()
                .currentServer(event.player.currentServer.orElse(null)?.serverInfo?.name)
        ).await()
    }
}
