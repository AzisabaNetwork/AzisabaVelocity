package src.azisaba.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(id = AzisabaVelocity.NAMESPACE)
class Main @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val dataDirectory: Path,
    suspendingPluginContainer: SuspendingPluginContainer,
)
