package ru.wb.go.utils.analytics

//import com.yandex.metrica.YandexMetrica

//class YandexMetricManagerImpl(
//    val deviceManager: DeviceManager,
//    val tokenManager: TokenManager,
//    val timeManager: TimeManager
//) :
//    YandexMetricManager {
//
//    override fun //onTechEventLog(screen: String, method: String, message: String) {
//        sendTechReportEvent(loadAsJson(TECH_ERROR, screen, split(method, message)).toString())
//    }
//
//    override fun //onTechEventLog(screen: String, method: String, message: String) {
//        sendTechReportEvent(loadAsJson(TECH_EVENT, screen, split(method, message)).toString())
//    }
//
//    override fun onTechNetworkLog(method: String, message: String) {
//        sendTechReportEvent(loadAsJson(TECH_NETWORK, method, split(method, message)).toString())
//    }
//
//    private fun split(method: String, message: String): String {
//        return deviceManager.appVersion + SPACE_DIVIDER + SPACE_DIVIDER + method + SPACE_DIVIDER + message + SPACE_DIVIDER + timeManager.getLocalMetricTime()
//    }
//
//    private fun sendTechReportEvent(eventBody: String) {
//        //YandexMetrica.reportEvent(TECH_LOG_EVENT, eventBody)
//    }
//
//    private fun loadAsJson(
//        action: String,
//        screen: String,
//        message: String
//    ): JSONObject {
//        val eventParameters: JSONObject = try {
//            JSONObject(
//                String.format(
//                    Locale.getDefault(),
//                    TECH_LOG_EVENT_PATTERN,
//                    action,
//                    tokenManager.wbUserID(),
//                    screen,
//                    message
//                )
//            )
//        } catch (exception: JSONException) {
//            JSONObject()
//        }
//        return eventParameters
//    }

//    companion object {
//        //        private const val TECH_LOG_EVENT_PATTERN = "{\"%s\":{\"%s\":\"%s\"}}"
//        private const val TECH_LOG_EVENT_PATTERN = "{\"%s\":{\"%s\":{\"%s\":\"%s\"}}}"
//        private const val TECH_LOG_EVENT = "tech_log"
//        private const val TECH_ERROR = "error"
//        private const val TECH_EVENT = "event"
//        private const val TECH_NETWORK = "network"
//        private const val SPACE_DIVIDER = " / "
//    }
//}