package ru.wb.perevozka.utils.reader

import android.content.Context
import com.google.gson.Gson
import ru.wb.perevozka.ui.config.data.ConfigDao
import java.io.BufferedReader
import java.io.InputStreamReader

class ConfigReaderImpl(
    private val context: Context,
    private val gson: Gson,
    private val fileName: String
) : ConfigReader {

    override fun build(): ConfigDao {
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

    private fun parse(json: String): ConfigDao {
        return gson.fromJson(json, ConfigDao::class.java)
    }

    companion object {
        private const val CHARSET_NAME = "UTF-8"
    }

}