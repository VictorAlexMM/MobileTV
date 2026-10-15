package com.iptvplayer.app.core.network

import com.iptvplayer.app.core.datastore.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NetworkGate(
    connectivity: ConnectivityObserver,
    settings: SettingsRepository,
    scope: CoroutineScope
) {
    /**
     * Define se o aplicativo tem autorização e capacidade de realizar tráfego de rede.
     * Verdadeiro SOMENTE se houver conectividade física E o toggle manual 'Ficar offline' estiver DESLIGADO.
     */
    val isOnlineAllowed: StateFlow<Boolean> =
        combine(connectivity.isConnected, settings.offlineModeEnabled) { net, manualOffline ->
            net && !manualOffline
        }.stateIn(scope, SharingStarted.Eagerly, connectivity.isCurrentlyConnected())

    /**
     * Consulta síncrona/instantânea do estado de permissão de rede.
     */
    fun canAccessNetwork(): Boolean = isOnlineAllowed.value
}
