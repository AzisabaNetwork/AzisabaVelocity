package net.azisaba.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationStore
import org.slf4j.Logger
import java.nio.file.Path
import java.text.MessageFormat
import java.util.*

@Plugin(id = AzisabaVelocity.NAMESPACE)
class Main @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val dataDirectory: Path,
    suspendingPluginContainer: SuspendingPluginContainer,
) {
    private val translationStore: TranslationStore.StringBased<MessageFormat> =
        TranslationStore.messageFormat(Key.key(AzisabaVelocity.NAMESPACE, "translations"))

    init {
        suspendingPluginContainer.initialize(this)

        translationStore.registerAll(Locale.US, ResourceBundle.getBundle("translations/Bundle", Locale.US), true)
        translationStore.registerAll(Locale.JAPAN, ResourceBundle.getBundle("translations/Bundle", Locale.JAPAN), true)
    }

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        GlobalTranslator.translator().addSource(translationStore)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        GlobalTranslator.translator().removeSource(translationStore)
    }
}
