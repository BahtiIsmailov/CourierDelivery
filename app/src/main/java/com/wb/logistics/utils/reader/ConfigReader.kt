package com.wb.logistics.utils.reader

import com.wb.logistics.ui.config.dao.ConfigDao

interface ConfigReader {
    fun build() : ConfigDao
}