package ru.wb.perevozka.utils.reader

import ru.wb.perevozka.ui.config.data.ConfigDao

interface ConfigReader {
    fun build() : ConfigDao
}