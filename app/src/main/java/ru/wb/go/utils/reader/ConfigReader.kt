package ru.wb.go.utils.reader

import ru.wb.go.ui.config.data.ConfigDao

interface ConfigReader {
    fun build() : ConfigDao
}