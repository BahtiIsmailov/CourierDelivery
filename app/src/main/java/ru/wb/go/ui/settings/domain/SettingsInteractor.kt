package ru.wb.go.ui.settings.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkState

interface SettingsInteractor {
      fun observeNetworkConnected(): Flow<NetworkState>
}