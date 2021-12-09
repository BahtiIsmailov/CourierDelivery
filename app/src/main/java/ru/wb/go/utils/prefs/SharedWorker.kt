package ru.wb.go.utils.prefs

interface SharedWorker {
    fun load(key: String, defValue: Boolean): Boolean
    fun save(key: String, value: Boolean)
    fun load(key: String, defValue: String): String
    fun save(key: String, value: String)
    fun load(key: String, defValue: Long): Long
    fun save(key: String, value: Long)
    fun load(key: String, defValue: Int): Int
    fun save(key: String, value: Int)
    fun <T : Any> save(key: String, value: T)
    fun <T> load(key: String, serializeClass: Class<T>): T?
    fun isAllExists(vararg keys: String): Boolean
    fun delete(vararg keys: String)
}