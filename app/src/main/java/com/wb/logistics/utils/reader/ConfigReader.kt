package com.wb.logistics.utils.reader

import com.wb.logistics.ui.config.data.ConfigDao

interface ConfigReader {
    fun build() : ConfigDao
}