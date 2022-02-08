package ru.wb.go.utils.managers

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import ru.wb.go.app.AppPreffsKeys

class PlayManagerImpl(
    private val context: Context,
    private val settingsManager: SettingsManager
) :
    PlayManager {
    private val player: MediaPlayer = MediaPlayer()

    init {
        val attr = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        player.setAudioAttributes(attr)
    }

    override fun play(resId: Int) {
        if (!settingsManager.getSetting(AppPreffsKeys.SETTING_VOICE_SCAN, true)) return

        val packageName = context.packageName
        val source =
            Uri.parse("android.resource://$packageName/raw/$resId")
        player.reset()
        player.setDataSource(context, source)

        player.prepare()
        player.start()
    }
}