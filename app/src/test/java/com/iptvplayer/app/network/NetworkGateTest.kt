package com.iptvplayer.app.network

import com.iptvplayer.app.core.network.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkGateTest {

    private class FakeConnectivityObserver(initial: Boolean) : ConnectivityObserver {
        val flow = MutableStateFlow(initial)
        override val isConnected: Flow<Boolean> = flow.asStateFlow()
        override fun isCurrentlyConnected(): Boolean = flow.value
    }

    private class NetworkGateTester(
        private val connectivity: ConnectivityObserver,
        private val offlineModeState: StateFlow<Boolean>
    ) {
        fun canAccessNetwork(): Boolean {
            return connectivity.isCurrentlyConnected() && !offlineModeState.value
        }
    }

    @Test
    fun testNetworkGateLogic() = runTest {
        // Cenário 1: Conectado à internet e toggle manual desligado -> Online Permitido
        val netOnline = FakeConnectivityObserver(true)
        val offlineSettingDisabled = MutableStateFlow(false)
        val gate1 = NetworkGateTester(netOnline, offlineSettingDisabled.asStateFlow())
        assertTrue("Deveria permitir rede quando conectado e toggle desligado", gate1.canAccessNetwork())

        // Cenário 2: Conectado à internet mas toggle 'Ficar offline' ligado -> Online BLOQUEADO
        val offlineSettingEnabled = MutableStateFlow(true)
        val gate2 = NetworkGateTester(netOnline, offlineSettingEnabled.asStateFlow())
        assertFalse("Deveria bloquear rede quando toggle 'Ficar offline' está ativo", gate2.canAccessNetwork())

        // Cenário 3: Sem internet física e toggle desligado -> Online BLOQUEADO (automático)
        val netOffline = FakeConnectivityObserver(false)
        val gate3 = NetworkGateTester(netOffline, offlineSettingDisabled.asStateFlow())
        assertFalse("Deveria bloquear rede quando não há conexão física", gate3.canAccessNetwork())

        // Cenário 4: Sem internet e toggle ligado -> Online BLOQUEADO
        val gate4 = NetworkGateTester(netOffline, offlineSettingEnabled.asStateFlow())
        assertFalse("Deveria bloquear rede sem conexão física e com toggle ligado", gate4.canAccessNetwork())
    }
}
