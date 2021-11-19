package ru.wb.go.utils.analytics

import com.yandex.metrica.YandexMetrica
import org.json.JSONException
import org.json.JSONObject
import ru.wb.go.network.token.TokenManager
import java.util.*

class YandexMetricManagerImpl(val tokenManager: TokenManager) : YandexMetricManager {

    override fun onTechErrorLog(screen: String, method: String, message: String) {
        sendReportEvent(
            TECH_LOG_EVENT,
            loadAsJson(TECH_ACTION_ERROR, screen, split(method, message)).toString()
        )
    }

    override fun onTechUIEventLog(screen: String, method: String, message: String) {
        sendReportEvent(
            TECH_LOG_EVENT,
            loadAsJson(TECH_ACTION_UI_EVENT, screen, split(method, message)).toString()
        )
    }

    private fun split(method: String, message: String): String {
        return tokenManager.wbUserID() + SPACE + method + SPACE + message
    }

    private fun sendReportEvent(eventName: String) {
        YandexMetrica.reportEvent(eventName)
    }

    private fun sendReportEvent(eventName: String, eventBody: String) {
        YandexMetrica.reportEvent(eventName, eventBody)
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
        private const val SIMPLE_EVENT_PATTERN = "EventName: \"%s\""
        private const val TECH_LOG_EVENT_PATTERN = "{\"%s\":{\"%s\":\"%s\"}}"
        private const val TECH_LOG_EVENT = "tech_log"
        private const val TECH_ACTION_ERROR = "error"
        private const val TECH_ACTION_UI_EVENT = "ui_event"
        private const val SPACE = " "
        private fun formEvent(eventName: String): String {
            return String.format(Locale.getDefault(), SIMPLE_EVENT_PATTERN, eventName)
        }

    }
}