package ru.wb.go.app

object AppConsts {

    const val SERVER_TIMEZONE = "Europe/Moscow"

    const val SERVICE_CODE_OK = 200
    const val SERVICE_CODE_BAD_REQUEST = 400
    const val SERVICE_CODE_UNAUTHORIZED = 401
    const val SERVICE_CODE_FORBIDDEN = 403
    const val SERVICE_CODE_LOCKED = 423

    const val APP_PACKAGE = "package"

    const val PRIVATE_INFO_MIN_DATE_PIKER = "01/01/1930 00:00:00"

    const val REPEAT_SMS_TICK: Long = 1000
    const val REPEAT_SMS_DURATION: Long = 120000

    private const val WAREHOUSE_DISTANCE_KM = 30

    //55.753989, 37.622229
    //56.024877, 37.622229
    //0.270888
    private const val MAP_DISTANCE_LAT_DIVIDER = 8.12664
    const val MAP_WAREHOUSE_LAT_DISTANCE = MAP_DISTANCE_LAT_DIVIDER / WAREHOUSE_DISTANCE_KM

    //55.764366, 37.788672
    //55.764366, 38.276191
    //          0.487519
    private const val MAP_DISTANCE_LON_DIVIDER = 14.1831
    const val MAP_WAREHOUSE_LON_DISTANCE = MAP_DISTANCE_LON_DIVIDER / WAREHOUSE_DISTANCE_KM

//    const val MAP_FACTOR = MAP_DISTANCE_LAT_DIVIDER / MAP_DISTANCE_LON_DIVIDER


}