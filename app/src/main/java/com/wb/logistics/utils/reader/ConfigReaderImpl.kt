package com.wb.logistics.utils.reader

import android.content.Context
import com.google.gson.Gson
import com.wb.logistics.ui.config.dao.ConfigDAO
import java.io.BufferedReader
import java.io.InputStreamReader

class ConfigReaderImpl(
    private val context: Context,
    private val gson: Gson,
    private val fileName: String
) : ConfigReader {

    override fun build(): ConfigDAO {
        val json = read()
        return parse(json)
    }

    private fun read(): String {
        val resultString = StringBuilder()
        val reader = BufferedReader(InputStreamReader(context.assets.open(fileName), CHARSET_NAME))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            resultString.append(line)
        }
        reader.close()
        return resultString.toString()
    }

    private fun parse(json: String): ConfigDAO {
        return gson.fromJson(json, ConfigDAO::class.java)
    }

    companion object {
        private const val CHARSET_NAME = "UTF-8"
    }

}