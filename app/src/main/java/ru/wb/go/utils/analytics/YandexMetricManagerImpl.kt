package ru.wb.go.utils.analytics

import com.yandex.metrica.YandexMetrica
import org.json.JSONException
import org.json.JSONObject
import ru.wb.go.network.token.TokenManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager
import java.util.*

class YandexMetricManagerImpl(
    val deviceManager: DeviceManager,
    val tokenManager: TokenManager,
    val timeManager: TimeManager
) :
    YandexMetricManager {

    override fun onTechErrorLog(screen: String, method: String, message: String) {
        sendTechReportEvent(loadAsJson(TECH_ERROR, screen, split(method, message)).toString())
    }

    override fun onTechEventLog(screen: String, method: String, message: String) {
        sendTechReportEvent(loadAsJson(TECH_EVENT, screen, split(method, message)).toString())
    }

    override fun onTechNetworkLog(method: String, message: String) {
        sendTechReportEvent(loadAsJson(TECH_NETWORK, method, split(method, message)).toString())
    }

    private fun split(method: String, message: String): String {
        return deviceManager.appVersion + SPACE_DIVIDER + tokenManager.wbUserID() + SPACE_DIVIDER + method + SPACE_DIVIDER + message + SPACE_DIVIDER + timeManager.getLocalTime()
    }

    private fun sendTechReportEvent(eventBody: String) {
        YandexMetrica.reportEvent(TECH_LOG_EVENT, eventBody)
    }

    private fun loadAsJson(action: String, screen: String, message: String): JSONObject {
        val eventParameters: JSONObject = try {
            JSONObject(
                String.format(
                    Locale.getDefault(),
                    TECH_LOG_EVENT_PATTERN,
                    action,
                    screen,
                    message
                )
            )
        } catch (exception: JSONException) {
            JSONObject()
        }
        return eventParameters
    }

    companion object {
        private const val TECH_LOG_EVENT_PATTERN = "{\"%s\":{\"%s\":\"%s\"}}"
        private const val TECH_LOG_EVENT = "tech_log"
        private const val TECH_ERROR = "error"
        private const val TECH_EVENT = "event"
        private const val TECH_NETWORK = "network"
        private const val SPACE_DIVIDER = " / "
    }
}