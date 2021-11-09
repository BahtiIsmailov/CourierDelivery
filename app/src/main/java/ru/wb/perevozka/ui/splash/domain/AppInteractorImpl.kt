package ru.wb.perevozka.ui.splash.domain

import io.reactivex.Observable
import io.reactivex.Single
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.auth.AppVersionState
import ru.wb.perevozka.utils.managers.DeviceManager
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.ScreenManagerImpl
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class AppInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val appSharedRepository: AppSharedRepository,
    private val screenManager: ScreenManager,
    private val deviceManager: DeviceManager,
) : AppInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun exitAuth() {
        authRemoteRepository.clearToken()
        appLocalRepository.clearAll()
    }

    override fun observeCountBoxes(): Observable<AppDeliveryResult> {
        return appLocalRepository.observeNavDrawerCountBoxes()
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun onSearchChange(query: String) {
        appSharedRepository.search(query)
    }

    override fun observeUpdatedStatus(): Observable<ScreenManagerImpl.NavigateComplete> {
        return screenManager.observeUpdatedStatus()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun checkUpdateApp(): Single<AppVersionState> {
        return Single.fromCallable { connectFtp() }
            .flatMap { lastVersionSingle(it) }
            .map {
                if (isUpdate(getLocalVersion(), it.version)) it
                else AppVersionState.UpToDate
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun lastVersionSingle(ftpClient: FTPClient) =
        Single.fromCallable { getLastAppVersionByFtp(ftpClient) }
            .doAfterTerminate {
                ftpClient.logout()
                ftpClient.disconnect()
            }

    override fun getUpdateApp(destination: String): Single<AppVersionState> {
        return Single.fromCallable { connectFtp() }
            .map { ftp -> uploadFile(ftp, getLastAppVersionByFtp(ftp).fileName, destination) }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun uploadFile(ftp: FTPClient, fileName: String, destination: String): AppVersionState {
        val path = "${destination}${fileName}"
        val file = File(path)
        if (file.exists()) file.delete()

        val outputStream: OutputStream = BufferedOutputStream(FileOutputStream(file))
        val ftpRemoteFile = "${FTP_BASE_PATH}${fileName}"
        val success = ftp.retrieveFile(ftpRemoteFile, outputStream)
        outputStream.close()

        return if (success) {
            AppVersionState.UpdateComplete(path)
        } else {
            ftp.logout()
            ftp.disconnect()
            AppVersionState.UpdateError
        }
    }

    private fun getLocalVersion(): Int {
        return versionCodeToInt(deviceManager.appVersion)
    }

    private fun versionCodeToInt(code: String): Int {
        return code.replace("\\D+".toRegex(), "").toInt()
    }

    private fun isUpdate(locals: Int, remotes: Int): Boolean {
        return remotes > locals
    }

    private fun getLastAppVersionByFtp(ftpClient: FTPClient) =
        ftpClient.listFiles(FTP_BASE_PATH)
            .filter { it.name.contains(APP_EXTENSION) }
            .map { it.name }
            .map { AppVersionState.Update(it, versionCodeToInt(it)) }
            .maxByOrNull { it.version } ?: AppVersionState.Update(EMPTY_APP_NAME, EMPTY_APP_VERSION)

    private fun connectFtp(): FTPClient {
        val ftpClient = FTPClient()
        ftpClient.connect(FTP_HOST_NAME, FTP_PORT)
        ftpClient.login(FTP_USER_NAME, FTP_USER_PASSWORD)
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
        ftpClient.enterLocalPassiveMode()
        return ftpClient
    }

    companion object {
        private const val FTP_HOST_NAME = "95.217.162.185"
        private const val FTP_PORT = 21
        private const val FTP_USER_NAME = "eugene"
        private const val FTP_USER_PASSWORD = "brigada_ftp"
        private const val FTP_BASE_PATH = "/ftp/files/"
        private const val APP_EXTENSION = ".apk"
        private const val EMPTY_APP_NAME = ""
        private const val EMPTY_APP_VERSION = 0
    }

}

data class AppDeliveryResult(
    val acceptedCount: Int,
    val returnCount: Int,
    val deliveryCount: Int,
    val debtCount: Int,
)