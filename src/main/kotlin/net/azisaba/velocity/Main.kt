package net.azisaba.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.launch
import com.google.inject.Inject
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import net.azisaba.data.config.ConfigHolder
import net.azisaba.graph.ApiClient
import net.azisaba.graph.api.PlayersApi
import net.azisaba.graph.api.StreamApi
import net.azisaba.velocity.commands.FriendCommand
import net.azisaba.velocity.listeners.listenStreamEvents
import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationStore
import org.slf4j.Logger
import java.nio.file.Path
import java.text.MessageFormat
import java.util.*

private val jsonFormat = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

@Plugin(id = AzisabaVelocity.NAMESPACE)
class Main @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @DataDirectory private val dataDirectory: Path,
    private val suspendingPluginContainer: SuspendingPluginContainer,
) {
    val config: ConfigHolder<Config> = ConfigHolder(Config.serializer(), jsonFormat).apply {
        bootstrap(dataDirectory.resolve("config.json"), Config())
    }

    val apiClient: ApiClient = ApiClient().setRequestInterceptor { request ->
        request.header("Authorization", "Bearer ${config.get().graphApiKey}")
    }

    val playersApi: PlayersApi by lazy {
        PlayersApi(apiClient)
    }

    val streamApi: StreamApi by lazy {
        StreamApi(apiClient)
    }

    private val translationStore: TranslationStore.StringBased<MessageFormat> = TranslationStore.messageFormat(
        Key.key(AzisabaVelocity.NAMESPACE, "translations")
    )

    init {
        suspendingPluginContainer.initialize(this)

        translationStore.registerAll(Locale.US, ResourceBundle.getBundle("translations/Bundle", Locale.US), true)
        translationStore.registerAll(Locale.JAPAN, ResourceBundle.getBundle("translations/Bundle", Locale.JAPAN), true)
    }

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        GlobalTranslator.translator().addSource(translationStore)

        registerCommand(FriendCommand(this).build().let(::BrigadierCommand))

        suspendingPluginContainer.pluginContainer.launch(Dispatchers.IO) {
            listenStreamEvents(this@Main)
        }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        GlobalTranslator.translator().removeSource(translationStore)
    }

    private fun registerCommand(command: BrigadierCommand) {
        val commandMeta = server.commandManager.metaBuilder(command)
            .plugin(this)
            .build()

        server.commandManager.register(commandMeta, command)
    }
}
