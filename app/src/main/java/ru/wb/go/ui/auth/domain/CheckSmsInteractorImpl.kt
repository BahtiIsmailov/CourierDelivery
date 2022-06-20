package ru.wb.go.ui.auth.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
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
    val coroutineScope = CoroutineScope(SupervisorJob())

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

    private suspend fun onTimeConfirmCode(tick: Long) {
        withContext(Dispatchers.IO){
            if (tick > durationTime) {
                publishCallState(TimerOverStateImpl())
            } else {
                val counterTick = durationTime - tick.toInt()
                publishCallState(TimerStateImpl(durationTime, counterTick))
            }
        }
    }

    private fun timeConfirmCodeDisposable() {
         coroutineScope.cancel()
    }
    private fun publishCallState(timerState: TimerState) {
        timerStates.tryEmit(timerState)
    }

    override val timer: Flow<TimerState>
        get() = timerStates

    override suspend fun stopTimer() {
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


/*
private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()

    private var timerDisposable: Disposable? = null

    override fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean> {
        return observable.map { it.toString() }
            .distinctUntilChanged()
            .map { it.length >= LENGTH_PASSWORD_MIN }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private var durationTime = 0
    override fun startTimer(durationTime: Int) {
        this.durationTime = durationTime
        if (timerDisposable == null) {
            timerDisposable = Observable.interval(PERIOD, TimeUnit.MILLISECONDS)
                .subscribe({ onTimeConfirmCode(it) }) { }
        }
    }

    private fun onTimeConfirmCode(tick: Long) {
        if (tick > durationTime) {
            timeConfirmCodeDisposable()
            publishCallState(TimerOverStateImpl())
        } else {
            val counterTick = durationTime - tick.toInt()
            publishCallState(TimerStateImpl(durationTime, counterTick))
        }
    }

    private fun publishCallState(timerState: TimerState) {
        timerStates.onNext(timerState)
    }

    private fun timeConfirmCodeDisposable() {
        if (timerDisposable != null) {
            timerDisposable!!.dispose()
            timerDisposable = null
        }
    }

    override val timer: Flowable<TimerState>
        get() = timerStates.compose(rxSchedulerFactory.applyObservableSchedulers())
            .toFlowable(BackpressureStrategy.BUFFER)

    override fun stopTimer() {
        timeConfirmCodeDisposable()
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override suspend fun auth(phone: String, password: String)  {
        withContext(Dispatchers.IO) {
            authRepository.auth(phone, password, true)
        }
    }

    override suspend fun couriersExistAndSavePhone(phone: String) {
        return withContext(Dispatchers.IO){
            authRepository.couriersExistAndSavePhone(phone)
        }
    }

    companion object {
        private const val PERIOD = 1000L
        private const val LENGTH_PASSWORD_MIN = 4
    }
}
*/
