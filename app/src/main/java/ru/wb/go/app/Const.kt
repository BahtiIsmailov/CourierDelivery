package ru.wb.go.app

const val DATABASE_NAME = "logistics.database"

const val APP_JSON = "application/json"

const val VERSION_API = "/api/v1"
const val VERSION_DEMO_API = "/api/v2"

const val VERSION_DATABASE = 38

const val EXPORT_SCHEMA_DATABASE = true

const val NEED_SEND_COURIER_DOCUMENTS = "NEED_SEND_COURIER_DOCUMENTS" //не заполнил данные
const val INTERNAL_SERVER_ERROR_COURIER_DOCUMENTS = "INTERNAL_SERVER_ERROR_COURIER_DOCUMENTS" //ошибка при обработке данных на сервере
const val NEED_APPROVE_COURIER_DOCUMENTS =
    "NEED_APPROVE_COURIER_DOCUMENTS" //ожидание проверки
const val NEED_CORRECT_COURIER_DOCUMENTS =
    "NEED_CORRECT_COURIER_DOCUMENTS" // требуется исправить данные

const val DELAY_NETWORK_REQUEST_MS = 400L

const val COURIER_COMPANY_ID = "318"

const val COURIER_ROLE = "COURIER"

const val PREFIX_BOX_QR_CODE_V1 = "\$1:1:"
const val PREFIX_BOX_QR_CODE_SPLITTER_V1 = ":"

const val PREFIX_QR_OFFICE_CODE_OLD = "PHX"
const val PREFIX_QR_OFFICE_CODE_V1 = "o:"

const val DEFAULT_ARRIVAL_TIME_COURIER_MIN = 20

const val YANDEX_METRIC_KEY = "54c7eead-ce27-4d1b-96f9-eca8c638f86b"
const val YANDEX_METRIC_DEBUG_KEY = "21b0a81e-cac2-4160-a81b-9d437c67450d"

const val TELEGRAM_SUPPORT_LINK = "+n-lLgR0i-HxkZmMy"
const val TELEGRAM_SUPPORT_ID = ""






