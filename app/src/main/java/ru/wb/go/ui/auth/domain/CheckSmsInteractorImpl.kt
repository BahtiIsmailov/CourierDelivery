package ru.wb.go.ui.auth.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.auth.signup.TimerOverStateImpl
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateImpl
import ru.wb.go.utils.CoroutineExtension
import java.util.concurrent.TimeUnit

class CheckSmsInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRepository: AuthRemoteRepository,
) : CheckSmsInteractor {

    private val timerStates = MutableSharedFlow<TimerState>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    private var coroutineScope:CoroutineScope? = null


    override fun remindPasswordChanges(observable: Flow<CharSequence>): Flow<Boolean> {
        return observable
            .map { it.toString() }
            .distinctUntilChanged()
            .map { it.length >= LENGTH_PASSWORD_MIN }

    }


    private var durationTime = 0
    override suspend fun startTimer(durationTime: Int) {
        this.durationTime = durationTime
        coroutineScope {
            CoroutineExtension.interval(PERIOD,TimeUnit.MILLISECONDS)
                .onEach {
                    onTimeConfirmCode(it)
                }
                .launchIn(this)
        }
    }

    private fun onTimeConfirmCode(tick: Long) {
        if (tick > durationTime) {
            publishCallState(TimerOverStateImpl())
        } else {
            val counterTick = durationTime - tick.toInt()
            publishCallState(TimerStateImpl(durationTime, counterTick))
        }

    }

    private fun timeConfirmCodeDisposable() {
        if (coroutineScope!= null) {
            coroutineScope?.cancel()
            coroutineScope = null
        }
    }

    private fun publishCallState(timerState: TimerState) {
        timerStates.tryEmit(timerState)
    }

    override val timer: Flow<TimerState>
        get() = timerStates

    override fun stopTimer() {
        timeConfirmCodeDisposable()
    }

    override fun observeNetworkConnected(): Flow<NetworkState> {
        return networkMonitorRepository.networkConnected()

    }

    override suspend fun auth(phone: String, password: String) {
        authRepository.auth(phone, password, true)
    }

    override suspend fun couriersExistAndSavePhone(phone: String) {
        authRepository.couriersExistAndSavePhone(phone)
    }

    companion object {
        private const val PERIOD = 1000L
        private const val LENGTH_PASSWORD_MIN = 4
    }
}


