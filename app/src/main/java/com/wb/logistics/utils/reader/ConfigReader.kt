package com.wb.logistics.utils.reader

import com.wb.logistics.ui.config.dao.ConfigDAO

interface ConfigReader {
    fun build() : ConfigDAO
}